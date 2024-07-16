package org.corderun.meltarionCase;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener {

    MeltarionCase plugin;

    public EventListener(MeltarionCase plugin) {
        super();
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractCase(PlayerInteractEvent e){
        Player player = e.getPlayer();
        Block caseBlock = e.getClickedBlock();
        if (e.getClickedBlock() == null || !e.getAction().name().contains("RIGHT_CLICK_BLOCK")) {
            return;
        }
        if(e.getClickedBlock().getType().equals(Material.DIAMOND_BLOCK)){
            plugin.openCase(player);
        }
    }

}
