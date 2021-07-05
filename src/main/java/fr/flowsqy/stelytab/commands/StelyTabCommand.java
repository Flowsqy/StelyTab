package fr.flowsqy.stelytab.commands;

import fr.flowsqy.stelytab.StelyTabPlugin;
import fr.flowsqy.stelytab.io.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.*;

public class StelyTabCommand implements TabExecutor {

    private final static String PERMISSION_PREFIX = "stelytab.command.";
    private final static String RELOAD = "reload";
    private final static String REFRESH = "refresh";

    private final StelyTabPlugin plugin;

    public StelyTabCommand(StelyTabPlugin plugin) {
        this.plugin = plugin;
        final PluginCommand stelytabCommand = plugin.getCommand("stelytab");
        assert stelytabCommand != null;
        stelytabCommand.setPermissionMessage(plugin.getMessages().getMessage("command.no-perm"));
        stelytabCommand.setExecutor(this);
        stelytabCommand.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return sendHelp(sender);
        }
        final String arg = args[0].toLowerCase(Locale.ROOT);
        final Messages messages = plugin.getMessages();
        if (arg.equals("rl") || arg.equals(RELOAD)) {
            plugin.getNameLoader().load();
            messages.sendMessage(sender, "command.success." + RELOAD);
            return true;
        }
        if (arg.equals("rf") || arg.equals(REFRESH)) {
            plugin.getNameManager().refresh();
            messages.sendMessage(sender, "command.success." + REFRESH);
            return true;
        }
        return sendHelp(sender);
    }

    private boolean sendHelp(CommandSender sender) {
        final Messages messages = plugin.getMessages();
        if (sender.hasPermission(PERMISSION_PREFIX + RELOAD))
            messages.sendMessage(sender, "command.help." + RELOAD);
        if (sender.hasPermission(PERMISSION_PREFIX + REFRESH))
            messages.sendMessage(sender, "command.help." + REFRESH);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        final String arg = args[0].toLowerCase(Locale.ROOT);

        final List<String> possibilities;
        if (arg.isEmpty()) {
            possibilities = new ArrayList<>(Arrays.asList(RELOAD, REFRESH));
        } else {
            possibilities = new ArrayList<>(2);
            if (RELOAD.startsWith(arg)) {
                possibilities.add(RELOAD);
            }
            if (REFRESH.startsWith(arg)) {
                possibilities.add(REFRESH);
            }
        }
        if (possibilities.isEmpty())
            return Collections.emptyList();

        final List<String> completions = new ArrayList<>(2);
        for (String subCommand : possibilities) {
            if (sender.hasPermission(PERMISSION_PREFIX + subCommand))
                completions.add(subCommand);
        }
        return completions;
    }
}