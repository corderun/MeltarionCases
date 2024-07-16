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
import java.util.Objects;

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

    public void openCase(Player player){
        player.sendMessage("Открытие кейса...");
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
        }
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

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
