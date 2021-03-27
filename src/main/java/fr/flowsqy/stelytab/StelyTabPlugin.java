package fr.flowsqy.stelytab;

import fr.flowsqy.teampacketmanager.TeamPacketManager;
import fr.flowsqy.teampacketmanager.TeamPacketManagerPlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class StelyTabPlugin extends JavaPlugin {

    private Permission permission;
    private TeamPacketManager teamPacketManager;

    @Override
    public void onEnable() {
        final RegisteredServiceProvider<Permission> service = Bukkit.getServicesManager().getRegistration(Permission.class);
        if(service == null) {
            Bukkit.getConsoleSender().sendMessage("Pas de permission enregistré, plugin desactivé");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        permission = service.getProvider();

        final TeamPacketManagerPlugin teamPacketManagerPlugin = JavaPlugin.getPlugin(TeamPacketManagerPlugin.class);
        teamPacketManager = teamPacketManagerPlugin.getTeamPacketManager();
    }

    public Permission getPermission() {
        return permission;
    }

    public TeamPacketManager getTeamPacketManager() {
        return teamPacketManager;
    }
}
