package fr.flowsqy.stelytab.commands;

import fr.flowsqy.stelytab.StelyTabPlugin;
import fr.flowsqy.stelytab.io.Messages;
import fr.flowsqy.stelytab.io.NameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StelyTabCommand implements TabExecutor {

    private final Messages messages;
    private final NameManager nameManager;

    public StelyTabCommand(StelyTabPlugin plugin) {
        this.messages = plugin.getMessages();
        this.nameManager = plugin.getNameManager();
        final PluginCommand stelytabCommand = plugin.getCommand("stelytab");
        assert stelytabCommand != null;
        stelytabCommand.setPermissionMessage(messages.getMessage("command.no-perm"));
        stelytabCommand.setExecutor(this);
        stelytabCommand.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
