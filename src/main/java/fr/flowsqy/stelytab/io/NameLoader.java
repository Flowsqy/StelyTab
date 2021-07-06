package fr.flowsqy.stelytab.io;

import fr.flowsqy.stelytab.StelyTabPlugin;
import fr.flowsqy.stelytab.name.Name;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NameLoader {

    private final static String FILE_NAME = "names.yml";

    private final StelyTabPlugin plugin;
    private BukkitTask loadTask;

    public NameLoader(StelyTabPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (loadTask != null) {
            return;
        }
        loadTask = new BukkitRunnable() {
            @Override
            public void run() {
                final YamlConfiguration configuration = plugin.initFile(plugin.getDataFolder(), FILE_NAME);
                plugin.getNameManager().setup(fillGroupData(configuration));

                loadTask = null;
            }
        }.runTaskAsynchronously(plugin);
    }

    private HashMap<String, PrioritizedName> fillGroupData(YamlConfiguration configuration) {
        final HashMap<String, PrioritizedName> nameForGroup = new HashMap<>();
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

            nameForGroup.put(key, new PrioritizedName(priority, new Name(color, prefix, suffix, displayName)));
        }

        return nameForGroup;
    }

    public record PrioritizedName(int priority, Name name) {
    }

}