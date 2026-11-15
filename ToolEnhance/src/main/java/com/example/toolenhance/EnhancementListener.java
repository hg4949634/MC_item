package com.example.toolenhance;

import io.papermc.paper.event.entity.EntityDamageItemEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;


public final class EnhancementListener implements Listener {
    private final EnhancementService service;

    public EnhancementListener(EnhancementService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMeleeDamage(EntityDamageByEntityEvent event) {
        
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!service.isWeapon(held.getType())) {
            return;
        }
        int level = service.getLevel(held);
        if (level > 0) {
            event.setDamage(event.getDamage() + service.getBonusDamage(level));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemDurabilityDamage(EntityDamageItemEvent event) {
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        ItemStack damagedItem = event.getItem();
        if (!service.isSupported(damagedItem.getType())) {
            return;
        }
        int level = service.getLevel(damagedItem);
        if (level <= 0) {
            return;
        }
        double chance = service.getDurabilitySaveRate(level);
        if (ThreadLocalRandom.current().nextDouble(100.0) < chance) {
            event.setCancelled(true);
        }
    }
}
