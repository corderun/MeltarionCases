package org.corderun.meltarionCase;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class MeltarionCase extends JavaPlugin {


    public YamlConfiguration langConfig;
    public List<String> caseOpened;
    public Connection connection;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CasePlaceholders(this).register();
        }
        CaseDB.initClass(this);
        CaseDB.connectToDatabase();
        CaseDB.createTable();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        Objects.requireNonNull(this.getCommand("case")).setExecutor(new CaseCommands(this));
        createLangConfig();
        createCaseFolder();
        caseOpened = new ArrayList<>();
        connection = CaseDB.getConnection();
    }

    public Connection getConnection() {
        return CaseDB.getConnection();
    }

    public void openCase(Player player, File file) {
        if(caseOpened.contains(file.getName())){
            player.sendMessage(Objects.requireNonNull(langConfig.getString("case.already-used")).replace("&", "§"));
            return;
        }
        player.sendMessage(Objects.requireNonNull(langConfig.getString("case.use.successful")).replace("&", "§"));
        caseOpened.add(file.getName());

        try (Connection connection = getConnection()) {
            String selectSql = "SELECT keys_count FROM mcase_keys WHERE player = ? AND case_name = ?";
            try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql)) {
                selectPstmt.setString(1, player.getName());
                selectPstmt.setString(2, file.getName().replace(".yml", ""));
                ResultSet resultSet = selectPstmt.executeQuery();
                resultSet.next();
                    int currentKeyCount = resultSet.getInt("keys_count");
                    int newKeyCount = currentKeyCount - 1;
                    String updateSql = "UPDATE mcase_keys SET keys_count = ? WHERE player = ? AND case_name = ?";
                    try (PreparedStatement updatePstmt = connection.prepareStatement(updateSql)) {
                        updatePstmt.setInt(1, newKeyCount);
                        updatePstmt.setString(2, player.getName());
                        updatePstmt.setString(3, file.getName().replace(".yml", ""));
                        updatePstmt.executeUpdate();
                    }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        YamlConfiguration caseFile = YamlConfiguration.loadConfiguration(file);
        List<CaseItem> items = new ArrayList<>();
        double totalChance = 0;

        for (String key : caseFile.getConfigurationSection("Items").getKeys(false)) {
            String path = "Items." + key;
            String type = caseFile.getString(path + ".Type");
            String name = caseFile.getString(path + ".name");
            double chance = caseFile.getDouble(path + ".chance");
            String broadcast = caseFile.getString(path + ".broadcast");
            List<String> commands = caseFile.getStringList(path + ".commands");

            items.add(new CaseItem(type, name, chance, broadcast, commands));
            totalChance += chance;
        }

        Random random = new Random();
        double randomValue = random.nextDouble() * totalChance;
        double currentChance = 0;

        for (CaseItem item : items) {
            currentChance += item.getChance();
            if (randomValue <= currentChance) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(!item.getBroadcast().equalsIgnoreCase("")){
                            for(Player players : Bukkit.getOnlinePlayers()){
                                players.sendMessage(item.getBroadcast().replace("&", "§").replace("%player%", player.getName()));
                            }
                        }
                        for (String command : item.getCommands()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()).replace("%uuid%", player.getUniqueId().toString()));
                        }
                        caseOpened.remove(file.getName());
                    }
                }.runTaskLater(this, caseFile.getInt("Delay")*20L);
                break;
            }
        }
    }

    private static class CaseItem {
        private final String type;
        private final String name;
        private final double chance;
        private final String broadcast;
        private final List<String> commands;

        public CaseItem(String type, String name, double chance, String broadcast, List<String> commands) {
            this.type = type;
            this.name = name;
            this.chance = chance;
            this.broadcast = broadcast;
            this.commands = commands;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public double getChance() {
            return chance;
        }

        public String getBroadcast(){
            return broadcast;
        }

        public List<String> getCommands() {
            return commands;
        }
    }

    private void createLangConfig() {
        File file = new File(getDataFolder(), "lang.yml");
        if (!file.exists()) {
            saveResource("lang.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(file);
    }

    private void createCaseFolder(){
        File casesDirectory = new File(getDataFolder(), "cases");
        if (!casesDirectory.exists()) {
            casesDirectory.mkdirs();
            // Создает example.yml только если нет директории cases
            InputStream inpStream = getResource("cases/example.yml");
            if (inpStream != null) {
                File exampleYmlFile = new File(casesDirectory, "example.yml");
                try {
                    Files.copy(inpStream, exampleYmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDisable() {
        CaseDB.disconnectFromDatabase();
    }
}
