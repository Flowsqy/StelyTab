package fr.flowsqy.stelytab;

import fr.flowsqy.stelytab.io.Messages;
import fr.flowsqy.stelytab.io.NameManager;
import fr.flowsqy.teampacketmanager.TeamPacketManager;
import fr.flowsqy.teampacketmanager.TeamPacketManagerPlugin;
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
    private TeamPacketManager teamPacketManager;
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

        this.messages = new Messages(initFile(dataFolder, "messages.yml", true));

        final RegisteredServiceProvider<Permission> service = Bukkit.getServicesManager().getRegistration(Permission.class);
        if(service == null) {
            messages.sendMessage(Bukkit.getConsoleSender(), "util.no-perm-plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        permission = service.getProvider();

        if(!permission.hasGroupSupport()) {
            messages.sendMessage(Bukkit.getConsoleSender(), "util.group-not-supported");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        final TeamPacketManagerPlugin teamPacketManagerPlugin = JavaPlugin.getPlugin(TeamPacketManagerPlugin.class);
        teamPacketManager = teamPacketManagerPlugin.getTeamPacketManager();

        nameManager = new NameManager(this, initFile(dataFolder, "names.yml", false));
    }

    private boolean checkDataFolder(File dataFolder) {
        if (dataFolder.exists())
            return dataFolder.canWrite();
        return dataFolder.mkdirs();
    }

    private YamlConfiguration initFile(File dataFolder, String fileName, boolean copy) {
        final File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            try {
                if(copy)
                    Files.copy(Objects.requireNonNull(getResource(fileName)), file.toPath());
                else
                    if(!file.createNewFile())
                        throw new IOException("Can not create the file: " + file);
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

    public TeamPacketManager getTeamPacketManager() {
        return teamPacketManager;
    }

    public NameManager getNameManager() {
        return nameManager;
    }
}
