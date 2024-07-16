package org.corderun.meltarionCase;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MeltarionCase extends JavaPlugin {


    public YamlConfiguration langConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        this.getCommand("case").setExecutor(new CaseCommands(this));
        createLangConfig();
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

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
