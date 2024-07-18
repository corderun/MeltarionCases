package org.corderun.meltarionCase;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        if(args[0].equalsIgnoreCase("givekey")){
            if(!sender.hasPermission("meltarioncase.givekey")){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("no-perm")).replace("&", "§"));
                return true;
            }
            if(args.length == 1 || args.length == 2 || args.length == 3){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.givekey.usage")).replace("&", "§"));
                return true;
            }
            String nick = args[1];
            String caseName = args[2];
            int keyCount;
            try {
                keyCount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("invalid-number")).replace("&", "§"));
                return true;
            }
            try (Connection connection = plugin.getConnection()) {
                String selectSql = "SELECT keys_count FROM mcase_keys WHERE player = ? AND case_name = ?";
                try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql)) {
                    selectPstmt.setString(1, nick);
                    selectPstmt.setString(2, caseName);
                    ResultSet resultSet = selectPstmt.executeQuery();

                    if (resultSet.next()) {
                        int currentKeyCount = resultSet.getInt("keys_count");
                        int newKeyCount = currentKeyCount + keyCount;
                        String updateSql = "UPDATE mcase_keys SET keys_count = ? WHERE player = ? AND case_name = ?";
                        try (PreparedStatement updatePstmt = connection.prepareStatement(updateSql)) {
                            updatePstmt.setInt(1, newKeyCount);
                            updatePstmt.setString(2, nick);
                            updatePstmt.setString(3, caseName);
                            updatePstmt.executeUpdate();
                        }
                        sender.sendMessage(plugin.langConfig.getString("case.givekey.successful").replace("&", "§").replace("%player%", nick).replace("%case%", caseName).replace("%amount%", String.valueOf(keyCount)));
                    } else {
                        String insertSql = "INSERT INTO mcase_keys (player, case_name, keys_count) VALUES (?, ?, ?)";
                        try (PreparedStatement insertPstmt = connection.prepareStatement(insertSql)) {
                            insertPstmt.setString(1, nick);
                            insertPstmt.setString(2, caseName);
                            insertPstmt.setInt(3, keyCount);
                            insertPstmt.executeUpdate();
                        }
                        sender.sendMessage(plugin.langConfig.getString("case.givekey.successful").replace("&", "§").replace("%player%", nick).replace("%case%", caseName).replace("%amount%", String.valueOf(keyCount)));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Не удалось записать данные: " + e.getMessage());
                sender.sendMessage("Произошла ошибка, пожалуйста свяжитесь с администрацией сервера");
            }
            return true;
        }
        if(args[0].equalsIgnoreCase("setkey")){
            if(!sender.hasPermission("meltarioncase.setkey")){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("no-perm")).replace("&", "§"));
                return true;
            }
            if(args.length == 1 || args.length == 2 || args.length == 3){
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.setkey.usage")).replace("&", "§"));
                return true;
            }
            String nick = args[1];
            String caseName = args[2];
            int keyCount;
            try {
                keyCount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("invalid-number")).replace("&", "§"));
                return true;
            }
            try (Connection connection = plugin.getConnection()) {
                String selectSql = "SELECT keys_count FROM mcase_keys WHERE player = ? AND case_name = ?";
                try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql)) {
                    selectPstmt.setString(1, nick);
                    selectPstmt.setString(2, caseName);
                    ResultSet resultSet = selectPstmt.executeQuery();

                    if (resultSet.next()) {
                        String updateSql = "UPDATE mcase_keys SET keys_count = ? WHERE player = ? AND case_name = ?";
                        try (PreparedStatement updatePstmt = connection.prepareStatement(updateSql)) {
                            updatePstmt.setInt(1, keyCount);
                            updatePstmt.setString(2, nick);
                            updatePstmt.setString(3, caseName);
                            updatePstmt.executeUpdate();
                        }
                        sender.sendMessage(plugin.langConfig.getString("case.setkey.successful").replace("&", "§").replace("%player%", nick).replace("%case%", caseName).replace("%amount%", String.valueOf(keyCount)));
                    } else {
                        String insertSql = "INSERT INTO mcase_keys (player, case_name, keys_count) VALUES (?, ?, ?)";
                        try (PreparedStatement insertPstmt = connection.prepareStatement(insertSql)) {
                            insertPstmt.setString(1, nick);
                            insertPstmt.setString(2, caseName);
                            insertPstmt.setInt(3, keyCount);
                            insertPstmt.executeUpdate();
                        }
                        sender.sendMessage(plugin.langConfig.getString("case.setkey.successful").replace("&", "§").replace("%player%", nick).replace("%case%", caseName).replace("%amount%", String.valueOf(keyCount)));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Не удалось записать данные: " + e.getMessage());
                sender.sendMessage("Произошла ошибка, пожалуйста свяжитесь с администрацией сервера");
            }
            try(Connection connection = plugin.getConnection()) {
                if (connection.isClosed()) {
                    plugin.connection = CaseDB.reconnectToDatabase();
                }
            }catch (SQLException e) {
                plugin.getLogger().severe("Не удалось записать данные: " + e.getMessage());
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