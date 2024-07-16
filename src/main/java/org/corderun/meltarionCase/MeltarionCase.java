package org.corderun.meltarionCase;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MeltarionCase extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    public void openCase(Player player){
        player.sendMessage("Открытие кейса...");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
