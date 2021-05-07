package fr.flowsqy.stelytab.io;

import fr.flowsqy.stelytab.StelyTabPlugin;
import fr.flowsqy.stelytab.team.Name;
import fr.flowsqy.stelytab.team.TeamPacketSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NameManager implements Listener {

    private final StelyTabPlugin plugin;
    private final YamlConfiguration configuration;
    private final File file;
    private final Map<String, Name> nameData;
    private final List<String> activeId;

    /**
     * Constructor for NameManager
     *
     * @param plugin        The StelyTabPlugin instance
     * @param configuration YamlConfiguration of the name data
     * @param file          The file of the yaml configuration
     */
    public NameManager(StelyTabPlugin plugin, YamlConfiguration configuration, File file) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.file = file;
        this.nameData = new HashMap<>();
        this.activeId = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Fill given list with all prefix
     *
     * @param prefixes The prefix list to fill
     * @param current  The current prefix for the prefix
     * @param length   The length of the prefix string
     * @param limit    The limit of prefix count
     */
    private static void getAllPrefixes(List<String> prefixes, String current, int length, int limit) {
        for (int i = 32; i < 127; i++) {
            if (prefixes.size() >= limit)
                return;
            final String prefix = current + (char) i;
            if (prefix.length() < length)
                getAllPrefixes(prefixes, prefix, length, limit);
            else
                prefixes.add(prefix);
        }
    }

    /**
     * Get prefix length
     *
     * @param prefixesCount the number of prefixes
     * @return The length of the prefix
     */
    private static int getPrefixLength(int prefixesCount) {
        int prefixLength = 0;
        int j = 1;
        for (int i = 1; i < prefixesCount; i++) {
            if ((j *= 95) >= prefixesCount) {
                prefixLength = i;
                break;
            }
        }
        return prefixLength;
    }

    /**
     * Load the name data map
     */
    public void load() {
        fillNameData();
    }

    /**
     * Reload the config and refill the name data map
     */
    public void reload() {
        this.nameData.clear();
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Can not read the names.yml", e);
        }
        fillNameData();
    }

    /**
     * Refresh all names
     */
    public void refresh() {
        refresh(null);
    }

    /**
     * Refresh all names
     *
     * @param exclude The player to exclude from remove packet
     */
    public void refresh(Set<Player> exclude) {
        Stream<? extends Player> players = Bukkit.getOnlinePlayers().stream();
        final List<Object> excludeConnections;
        if (exclude != null) {
            excludeConnections = new ArrayList<>(exclude.size());
            for (Player player : exclude) {
                excludeConnections.add(TeamPacketSender.getConnection(player));
            }
            players = players.filter(player -> !exclude.contains(player));
        } else {
            excludeConnections = Collections.emptyList();
        }
        final List<Object> connections = players.map(TeamPacketSender::getConnection).collect(Collectors.toList());
        for (String id : activeId) {
            final Object removePacket = TeamPacketSender.getRemove(id);
            TeamPacketSender.send(connections, removePacket);
        }
        activeId.clear();
        connections.addAll(excludeConnections);
        final Map<Name, List<String>> groupPlayers = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            final String group = plugin.getPermission().getPrimaryGroup(player);
            final Name name = nameData.get(group);
            if (name == null)
                continue;
            final List<String> playerList = groupPlayers.computeIfAbsent(name, k -> new ArrayList<>());
            playerList.add(player.getName());
        }
        for (Map.Entry<Name, List<String>> entry : groupPlayers.entrySet()) {
            final Object createPacket = TeamPacketSender.getCreate(entry.getKey(), entry.getValue());
            TeamPacketSender.send(connections, createPacket);
            activeId.add(entry.getKey().getId());
        }
    }

    /**
     * Fill the name data map
     */
    private void fillNameData() {
        final Map<Integer, Set<Name>> tempData = new HashMap<>();
        final Logger logger = plugin.getLogger();
        final String awkwardKeyMessage = ChatColor.stripColor(plugin.getMessages().getMessage("config.awkward-key"));
        // Register all data by priority
        for (String key : configuration.getKeys(false)) {
            final ConfigurationSection configurationSection = configuration.getConfigurationSection(key);
            if (configurationSection == null) {
                if (awkwardKeyMessage != null)
                    logger.log(Level.WARNING, awkwardKeyMessage.replace("%key%", key));
                continue;
            }
            final int priority = Math.max(configurationSection.getInt("priority", 0), 0);
            final String colorName = configurationSection.getString("color");
            final ChatColor color = colorName == null || colorName.length() < 1 ? null : ChatColor.getByChar(colorName);
            String prefix = configurationSection.getString("prefix");
            if (prefix != null)
                prefix = ChatColor.translateAlternateColorCodes('&', prefix);
            String suffix = configurationSection.getString("suffix");
            if (suffix != null)
                suffix = ChatColor.translateAlternateColorCodes('&', suffix);
            final Name name = new Name(key, color, prefix, suffix);
            final Set<Name> dataSet = tempData.computeIfAbsent(priority, k -> new HashSet<>());
            dataSet.add(name);
        }

        if (tempData.isEmpty())
            return;

        // Generate all prefixes
        final List<String> priorityPrefixes = new ArrayList<>();
        final int prefixesCount = tempData.values().stream().mapToInt(Set::size).sum();
        final int prefixLength = getPrefixLength(prefixesCount);

        getAllPrefixes(priorityPrefixes, "", prefixLength, prefixesCount);

        final Comparator<Map.Entry<Integer, Set<Name>>> comparator = Comparator.comparingInt(Map.Entry::getKey);

        // Register all correctly
        tempData.entrySet().stream()
                .sorted(comparator.reversed())
                .forEachOrdered(new Consumer<Map.Entry<Integer, Set<Name>>>() {

                    final Iterator<String> characterIterator = priorityPrefixes.iterator();

                    @Override
                    public void accept(Map.Entry<Integer, Set<Name>> entry) {
                        for (Name name : entry.getValue()) {
                            // Save group
                            final String group = name.getId();
                            // Set id
                            name.setId(characterIterator.next());
                            // Register the name for the group
                            nameData.put(group, name);
                        }
                    }
                });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        refresh(Collections.singleton(e.getPlayer()));
    }

}
