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

/** 플레이 중 발생하는 공격 피해 및 도구 내구도 이벤트를 처리합니다. */
public final class EnhancementListener implements Listener {
    private final EnhancementService service;

    public EnhancementListener(EnhancementService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMeleeDamage(EntityDamageByEntityEvent event) {
        // 활/삼지창/기타 간접 공격에는 강화 공격력 보너스를 적용하지 않습니다.
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
        // Paper API의 아이템 내구도 소모 이벤트를 이용합니다.
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
