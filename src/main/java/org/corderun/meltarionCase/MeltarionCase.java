package org.corderun.meltarionCase;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class MeltarionCase extends JavaPlugin {


    public YamlConfiguration langConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        Objects.requireNonNull(this.getCommand("case")).setExecutor(new CaseCommands(this));
        createLangConfig();
        createCaseFolder();
    }

    public void openCase(Player player, File file) {
        player.sendMessage("Открытие кейса...");
        YamlConfiguration caseFile = YamlConfiguration.loadConfiguration(file);
        List<CaseItem> items = new ArrayList<>();
        double totalChance = 0;

        for (String key : caseFile.getConfigurationSection("Items").getKeys(false)) {
            String path = "Items." + key;
            String type = caseFile.getString(path + ".Type");
            String name = caseFile.getString(path + ".name");
            double chance = caseFile.getDouble(path + ".chance");
            String command = caseFile.getString(path + ".command");

            items.add(new CaseItem(type, name, chance, command));
            totalChance += chance;
        }

        Random random = new Random();
        double randomValue = random.nextDouble() * totalChance;
        double currentChance = 0;

        for (CaseItem item : items) {
            currentChance += item.getChance();
            if (randomValue <= currentChance) {
                player.sendMessage("Вы получили: " + item.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), item.getCommand().replace("%player%", player.getName()));
                break;
            }
        }
    }

    private static class CaseItem {
        private final String type;
        private final String name;
        private final double chance;
        private final String command;

        public CaseItem(String type, String name, double chance, String command) {
            this.type = type;
            this.name = name;
            this.chance = chance;
            this.command = command;
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

        public String getCommand() {
            return command;
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
        // Plugin shutdown logic
    }
}
