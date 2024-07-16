package org.corderun.meltarionCase;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.Objects;

public class EventListener implements Listener {

    MeltarionCase plugin;
    private boolean cdCase = false;

    public EventListener(MeltarionCase plugin) {
        super();
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractCase(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null || !e.getAction().name().contains("RIGHT_CLICK_BLOCK")) {
            return;
        }
        File directory = new File("plugins/MeltarionCase/cases");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        YamlConfiguration caseFile = YamlConfiguration.loadConfiguration(file);
                        Location caseLoc = caseFile.getLocation("Location");
                        if (caseLoc != null && caseLoc.equals(e.getClickedBlock().getLocation())) {
                            if(!cdCase){
                                cdCase = true;
                                plugin.openCase(player);
                                e.setCancelled(true);
                                plugin.getServer().getScheduler().runTaskLater(plugin, () -> cdCase = false, 20L);
                            }
                        }
                    }
                }
            }
        }

    }
}
