package org.corderun.meltarionCase;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
            if(args.length == 1){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.create.usage")).replace("&", "§"));
                return true;
            }
            String caseName = args[1];
            // Тут хранятся все кейсы
            File directory = new File("plugins/MeltarionCase/cases");
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            String fileName = file.getName();
                            // Тут кароч проверяется название кейса через названия файлов
                            if (fileName.equalsIgnoreCase(caseName + ".yml")) {
                                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.create.already-used")).replace("&", "§"));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
