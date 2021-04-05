package fr.flowsqy.stelytab.commands;

import fr.flowsqy.stelytab.StelyTabPlugin;
import fr.flowsqy.stelytab.io.Messages;
import fr.flowsqy.stelytab.io.NameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.*;

public class StelyTabCommand implements TabExecutor {

    private final static String RELOAD = "reload";
    private final static String REFRESH = "refresh";

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
            if (sender.hasPermission("stelytab.command." + subCommand))
                completions.add(subCommand);
        }
        return completions;
    }
}
