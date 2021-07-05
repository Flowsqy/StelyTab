package fr.flowsqy.stelytab.io;

import fr.flowsqy.stelytab.StelyTabPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NameLoader {

    private final static String FILE_NAME = "names.yml";

    private final StelyTabPlugin plugin;
    private BukkitTask loadTask;

    public NameLoader(StelyTabPlugin plugin) {
        this.plugin = plugin;
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
        if (loadTask != null) {
            return;
        }
        loadTask = new BukkitRunnable() {
            @Override
            public void run() {
                final YamlConfiguration configuration = plugin.initFile(plugin.getDataFolder(), FILE_NAME);
                fillGroupData(configuration);

                loadTask = null;
            }
        }.runTaskAsynchronously(plugin);
    }

    private void fillGroupData(YamlConfiguration configuration) {
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

            // TODO Store all data
        }
    }

}