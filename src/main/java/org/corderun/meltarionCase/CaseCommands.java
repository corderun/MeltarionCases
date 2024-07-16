package org.corderun.meltarionCase;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CaseCommands implements CommandExecutor {

    MeltarionCase plugin;

    public CaseCommands(MeltarionCase plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0){
            sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.usage")).replace("&", "§"));
            return true;
        }
        if(args[0].equalsIgnoreCase("create")){
            if(!sender.hasPermission("meltarioncase.create")){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("no-perm")).replace("&", "§"));
                return true;
            }

        }
        return true;
    }
}
