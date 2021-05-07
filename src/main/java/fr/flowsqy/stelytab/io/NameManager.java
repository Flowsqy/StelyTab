package fr.flowsqy.stelytab.io;

import fr.flowsqy.stelytab.StelyTabPlugin;
import fr.flowsqy.teampacketmanager.commons.ApplicableTeamData;
import fr.flowsqy.teampacketmanager.commons.TeamData;
import fr.flowsqy.teampacketmanager.exception.TeamIdException;
import net.milkbowl.vault.permission.Permission;
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

import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NameManager implements Listener {

    private final StelyTabPlugin plugin;
    private final YamlConfiguration configuration;
    private final File file;
    private final Map<Integer, List<GroupData>> priorityData;
    private final Map<String, Integer> groupPriority;

    public NameManager(StelyTabPlugin plugin, YamlConfiguration configuration, File file) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.file = file;
        this.priorityData = new HashMap<>();
        this.groupPriority = new HashMap<>();
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
        this.priorityData.clear();
        this.groupPriority.clear();
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Can not read the names.yml", e);
        }
        fillGroupData();
    }

    public void refresh() {
        for (List<GroupData> groupDataList : priorityData.values()) {
            updatePriority(groupDataList);
        }
    }

    private void fillGroupData() {
        final Map<Integer, Set<GroupData>> tempData = new HashMap<>();
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
            String displayName = configurationSection.getString("displayname");
            if (displayName != null)
                displayName = ChatColor.translateAlternateColorCodes('&', displayName);
            final TeamData data = new TeamData.Builder()
                    .id(TeamData.DEFAULT_TEAM_ID)
                    .color(color)
                    .prefix(prefix)
                    .suffix(suffix)
                    .displayName(displayName)
                    .create();
            final GroupData groupData = new GroupData(key, data);
            final Set<GroupData> dataSet = tempData.computeIfAbsent(priority, k -> new HashSet<>());
            dataSet.add(groupData);
        }

        if (tempData.isEmpty())
            return;

        // Generate all prefixes
        final List<String> priorityPrefixes = new ArrayList<>();
        final int prefixesCount = tempData.size();
        final int prefixLength = getPrefixLength(prefixesCount);

        getAllPrefixes(priorityPrefixes, "", prefixLength, prefixesCount);

        final Comparator<Map.Entry<Integer, Set<GroupData>>> comparator = Comparator.comparingInt(Map.Entry::getKey);

        // Register all correctly
        tempData.entrySet().stream()
                .sorted(comparator.reversed())
                .forEachOrdered(new Consumer<Map.Entry<Integer, Set<GroupData>>>() {

                    final Iterator<String> characterIterator = priorityPrefixes.iterator();

                    @Override
                    public void accept(Map.Entry<Integer, Set<GroupData>> entry) {
                        // Group priority prefix
                        final String idPrefix = characterIterator.next();
                        // Group data list for this priority
                        final List<GroupData> groupDataList = new ArrayList<>();
                        priorityData.put(entry.getKey(), groupDataList);
                        // For all data with same priority prefix
                        for (GroupData data : entry.getValue()) {
                            // Set id
                            data.getTeamData().setId(idPrefix);
                            // Register priority for the group name
                            groupPriority.put(data.getGroupName(), entry.getKey());
                            // Register groupdata for the priority
                            groupDataList.add(data);
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
        final Integer priority = groupPriority.get(group);
        if (priority == null)
            return;
        final List<GroupData> teamDataList = priorityData.get(priority);
        if (teamDataList == null)
            return;
        updatePriority(teamDataList);
    }

    private void updatePriority(List<GroupData> groupDataList) {
        final Map<Player, TeamData> players = new HashMap<>();
        final Permission permission = plugin.getPermission();
        // For all groups of the priority
        for (GroupData groupData : groupDataList) {
            // Check for all players if there are in the group
            final String group = groupData.getGroupName();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (group.equals(permission.getPrimaryGroup(onlinePlayer))) {
                    players.put(onlinePlayer, groupData.getTeamData());
                }
            }
        }
        // Generate all prefixes for alphabetical order
        final List<String> priorityPrefixes = new ArrayList<>();
        final int prefixesCount = players.size();
        final int prefixLength = getPrefixLength(prefixesCount);

        getAllPrefixes(priorityPrefixes, "", prefixLength, prefixesCount);

        final Iterator<String> prefixIterator = priorityPrefixes.iterator();

        final List<ApplicableTeamData> applicableList = new ArrayList<>(players.size());
        // Generate all team info with prefix for all players
        players.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                .forEachOrdered(entry -> {
                    final Player player = entry.getKey();
                    final TeamData teamData = entry.getValue();
                    final TeamData data = new TeamData.Builder()
                            .id(teamData.getId() + prefixIterator.next())
                            .color(teamData.getColor())
                            .displayName(teamData.getDisplayName() == null ?
                                    null : teamData.getDisplayName().replace("%player%", player.getName()))
                            .prefix(teamData.getPrefix())
                            .suffix(teamData.getSuffix())
                            .create();
                    applicableList.add(new ApplicableTeamData(player, data));
                });
        try {
            plugin.getTeamPacketManager().applyTeamData(applicableList);
        } catch (TeamIdException e) {
            // Error logging
            final File dataFolder = plugin.getDataFolder();
            final File errorFolder = new File(dataFolder, "error");
            if (!errorFolder.exists()) {
                if (!errorFolder.mkdir()) {
                    throw new RuntimeException("Can not create the error folder");
                }
            }
            final File errorFile = new File(errorFolder, UUID.randomUUID() + ".txt");
            final PrintWriter printWriter;
            try {
                final BufferedWriter writer = new BufferedWriter(new FileWriter(errorFile));
                printWriter = new PrintWriter(writer);
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }

            printWriter.println(Timestamp.from(Instant.now()));
            printWriter.println(e.getMessage());
            e.printStackTrace(printWriter);
            final StringBuilder builder = new StringBuilder();
            for (ApplicableTeamData applicable : applicableList) {
                builder.append("\n").append(applicable.getPlayer().getName()).append(":").append(applicable.getTeamData());
            }
            printWriter.println("Id list of priority:" + builder);
            final StringBuilder playerGrades = new StringBuilder();
            for (final Player player : Bukkit.getOnlinePlayers()) {
                playerGrades.append("\n").append(player.getName()).append(":").append(permission.getPrimaryGroup(player));
            }
            printWriter.println("Group id and connected players : " + playerGrades);
            printWriter.close();

            // Clean send
            plugin.getTeamPacketManager().cleanData();
            plugin.getTeamPacketManager().applyTeamData(applicableList);
        }
    }

    private final static class GroupData {

        private final String groupName;
        private final TeamData teamData;

        public GroupData(String groupName, TeamData teamData) {
            this.groupName = groupName;
            this.teamData = teamData;
        }

        public String getGroupName() {
            return groupName;
        }

        public TeamData getTeamData() {
            return teamData;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupData groupData = (GroupData) o;
            return Objects.equals(groupName, groupData.groupName) && Objects.equals(teamData, groupData.teamData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupName, teamData);
        }

        @Override
        public String toString() {
            return "GroupData{" +
                    "groupName='" + groupName + '\'' +
                    ", teamData=" + teamData +
                    '}';
        }
    }

}
