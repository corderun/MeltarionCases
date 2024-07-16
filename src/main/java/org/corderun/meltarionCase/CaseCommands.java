package org.corderun.meltarionCase;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
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
            // Здесь получается уже, что такого файла нет, так что можно создавать кейс
            Player player = (Player) sender;
            Location caseLoc = player.getTargetBlock(null, 100).getLocation();
            createCase(args[1], caseLoc);
            sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.create.successful")).replace("&", "§").replace("%name%", args[1]));
            return true;
        }
        if(args[0].equalsIgnoreCase("remove")){
            if(!sender.hasPermission("meltarioncase.remove")){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("no-perm")).replace("&", "§"));
                return true;
            }
            if(args.length == 1){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.remove.usage")).replace("&", "§"));
                return true;
            }
            String caseName = args[1];

            try {
                Files.delete(Path.of("plugins/MeltarionCase/cases/" + caseName + ".yml"));
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.remove.successful")).replace("&", "§").replace("%name%", args[1]));
            } catch (IOException e) {
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.remove.not-found")).replace("&", "§"));
            }
            return true;
        }
        if(args[0].equalsIgnoreCase("list")){
            if(!sender.hasPermission("meltarioncase.list")){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("no-perm")).replace("&", "§"));
                return true;
            }
            sender.sendMessage(plugin.langConfig.getString("case.list.header").replace("&", "§"));
            File directory = new File("plugins/MeltarionCase/cases");
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            String fileName = file.getName();
                            sender.sendMessage(fileName.replace(".yml", ""));
                        }
                    }
                }
            }
            return true;
        }
        return true;
    }

    private void createCase(String name, Location loc){
        File casesDirectory = new File(plugin.getDataFolder(), "cases");
        InputStream inpStream = plugin.getResource("cases/example.yml");
        if (inpStream != null) {
            File newCaseFile = new File(casesDirectory, name + ".yml");
            try {
                Files.copy(inpStream, newCaseFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                updateLocationInCaseFile(newCaseFile, loc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateLocationInCaseFile(File caseFile, Location loc) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(caseFile);
        config.set("Location", loc);
        try {
            config.save(caseFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
