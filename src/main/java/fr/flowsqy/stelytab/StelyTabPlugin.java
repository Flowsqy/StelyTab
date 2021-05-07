package fr.flowsqy.stelytab;

import fr.flowsqy.stelytab.commands.StelyTabCommand;
import fr.flowsqy.stelytab.io.Messages;
import fr.flowsqy.stelytab.io.NameManager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StelyTabPlugin extends JavaPlugin {

    private Messages messages;
    private Permission permission;
    private NameManager nameManager;

    @Override
    public void onEnable() {
        final Logger logger = getLogger();
        final File dataFolder = getDataFolder();

        if (!checkDataFolder(dataFolder)) {
            logger.log(Level.WARNING, "Can not write in the directory : " + dataFolder.getAbsolutePath());
            logger.log(Level.WARNING, "Disable the plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.messages = new Messages(initFile(dataFolder, "messages.yml"));

        final RegisteredServiceProvider<Permission> service = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (service == null) {
            messages.sendMessage(Bukkit.getConsoleSender(), "plugin.no-perm-plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        permission = service.getProvider();

        if (!permission.hasGroupSupport()) {
            messages.sendMessage(Bukkit.getConsoleSender(), "plugin.group-not-supported");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        nameManager = new NameManager(this, initFile(dataFolder, "names.yml"), new File(dataFolder, "names.yml"));
        nameManager.load();

        new StelyTabCommand(this);
    }

    private boolean checkDataFolder(File dataFolder) {
        if (dataFolder.exists())
            return dataFolder.canWrite();
        return dataFolder.mkdirs();
    }

    private YamlConfiguration initFile(File dataFolder, String fileName) {
        final File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            try {
                Files.copy(Objects.requireNonNull(getResource(fileName)), file.toPath());
            } catch (IOException ignored) {
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public Messages getMessages() {
        return messages;
    }

    public Permission getPermission() {
        return permission;
    }

    public NameManager getNameManager() {
        return nameManager;
    }
}
