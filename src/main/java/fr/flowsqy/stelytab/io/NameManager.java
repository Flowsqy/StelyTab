package fr.flowsqy.stelytab.io;

import fr.flowsqy.stelytab.StelyTabPlugin;
import fr.flowsqy.teampacketmanager.commons.ApplicableTeamData;
import fr.flowsqy.teampacketmanager.commons.TeamData;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
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

public class NameManager implements Listener {

    private final StelyTabPlugin plugin;
    private final YamlConfiguration configuration;
    private final File file;
    private final Map<String, TeamData> groupData;

    public NameManager(StelyTabPlugin plugin, YamlConfiguration configuration, File file) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.file = file;
        this.groupData = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

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

    public void load() {
        fillGroupData();
    }

    public void reload() {
        this.groupData.clear();
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Can not read the names.yml", e);
        }
        fillGroupData();
    }

    public void refresh() {
        for(Map.Entry<String, TeamData> entry : groupData.entrySet()){
            updateGroup(entry.getKey(), entry.getValue());
        }
    }

    private void fillGroupData() {
        final Map<Integer, Set<TeamData>> tempData = new HashMap<>();
        final Logger logger = plugin.getLogger();
        final String awkwardKeyMessage = ChatColor.stripColor(plugin.getMessages().getMessage("config.awkward-key"));
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
            String displayName = configurationSection.getString("displayname");
            if (displayName != null)
                displayName = ChatColor.translateAlternateColorCodes('&', displayName);
            final TeamData data = new TeamData.Builder()
                    .id(key)
                    .color(color)
                    .prefix(prefix)
                    .suffix(suffix)
                    .displayName(displayName)
                    .create();
            final Set<TeamData> dataSet = tempData.computeIfAbsent(priority, k -> new HashSet<>());
            dataSet.add(data);
        }

        if (tempData.isEmpty())
            return;

        final List<String> priorityPrefixes = new ArrayList<>();
        final int prefixesCount = tempData.size();
        final int prefixLength = getPrefixLength(prefixesCount);

        getAllPrefixes(priorityPrefixes, "", prefixLength, prefixesCount);

        final Comparator<Map.Entry<Integer, Set<TeamData>>> comparator = Comparator.comparingInt(Map.Entry::getKey);

        tempData.entrySet().stream()
                .sorted(comparator.reversed())
                .forEachOrdered(new Consumer<Map.Entry<Integer, Set<TeamData>>>() {

                    final Iterator<String> characterIterator = priorityPrefixes.iterator();

                    @Override
                    public void accept(Map.Entry<Integer, Set<TeamData>> integerSetEntry) {
                        final String idPrefix = characterIterator.next();
                        for (TeamData data : integerSetEntry.getValue()) {
                            final String groupName = data.getId();
                            data.setId(idPrefix);
                            groupData.put(groupName, data);
                        }
                    }
                });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String group = plugin.getPermission().getPrimaryGroup(player);
        if (group == null)
            return;
        final TeamData teamData = groupData.get(group);
        if (teamData == null)
            return;
        updateGroup(group, teamData);
    }

    private void updateGroup(String group, TeamData teamData) {
        final List<Player> players = new ArrayList<>();
        final Permission permission = plugin.getPermission();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (group.equals(permission.getPrimaryGroup(onlinePlayer))) {
                players.add(onlinePlayer);
            }
        }
        final List<String> priorityPrefixes = new ArrayList<>();
        final int prefixesCount = players.size();
        final int prefixLength = getPrefixLength(prefixesCount);

        getAllPrefixes(priorityPrefixes, "", prefixLength, prefixesCount);

        final Iterator<String> prefixIterator = priorityPrefixes.iterator();

        final List<ApplicableTeamData> applicableList = new ArrayList<>(players.size());
        for (Player onlinePlayer : players.stream().sorted(Comparator.comparing(HumanEntity::getName)).collect(Collectors.toList())) {
            final TeamData data = new TeamData.Builder()
                    .id(teamData.getId() + prefixIterator.next())
                    .color(teamData.getColor())
                    .displayName(teamData.getDisplayName() == null ?
                            null : teamData.getDisplayName().replace("%player%", onlinePlayer.getName()))
                    .prefix(teamData.getPrefix())
                    .suffix(teamData.getSuffix())
                    .create();
            applicableList.add(new ApplicableTeamData(onlinePlayer, data));
        }
        plugin.getTeamPacketManager().applyTeamData(applicableList);
    }

}
