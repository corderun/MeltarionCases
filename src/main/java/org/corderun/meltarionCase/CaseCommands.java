package org.corderun.meltarionCase;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
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
            Location caseLoc = player.getEyeLocation();
            createCase(args[1], caseLoc);
            sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.create.successful")).replace("&", "§"));
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
                updateLocationInYaml(newCaseFile, loc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateLocationInYaml(File yamlFile, Location loc) throws IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> data = new HashMap<>();

        try (InputStream inputStream = new FileInputStream(yamlFile)) {
            data = yaml.load(inputStream);
        }

        if (data == null) {
            data = new HashMap<>();
        }

        Map<String, Object> locationSection = new HashMap<>();
        locationSection.put("world", loc.getWorld().getName());
        locationSection.put("x", loc.getX());
        locationSection.put("y", loc.getY());
        locationSection.put("z", loc.getZ());
        locationSection.put("yaw", loc.getYaw());
        locationSection.put("pitch", loc.getPitch());

        data.put("Location", locationSection);

        try (OutputStream outputStream = new FileOutputStream(yamlFile)) {
            yaml.dump(data, new OutputStreamWriter(outputStream));
        }
    }
}
