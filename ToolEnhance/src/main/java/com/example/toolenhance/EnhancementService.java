package com.example.toolenhance;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;


public final class EnhancementService {
    public static final int MAX_LEVEL = 15;
    private static final String LORE_PREFIX = "[ToolEnhance] ";

    private final ToolEnhancePlugin plugin;
    private final NamespacedKey levelKey;

    public EnhancementService(ToolEnhancePlugin plugin) {
        this.plugin = plugin;
        this.levelKey = new NamespacedKey(plugin, "enhancement_level");
    }

    public boolean isSupported(Material type) {
        String name = type.name();
        return name.endsWith("_SWORD") || name.endsWith("_PICKAXE")
                || name.endsWith("_AXE") || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE");
    }

    public boolean isWeapon(Material type) {
        String name = type.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }

    public boolean isMiningTool(Material type) {
        String name = type.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE")
                || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
    }

    /** 아이템의 PersistentDataContainer(PDC)에 저장된 단계를 읽습니다. */
    public int getLevel(ItemStack item) {
        if (item == null || !isSupported(item.getType())) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0;
        }
        Integer savedLevel = meta.getPersistentDataContainer()
                .get(levelKey, PersistentDataType.INTEGER);
        return savedLevel == null ? 0 : Math.max(0, Math.min(savedLevel, MAX_LEVEL));
    }

    public double getRate(int targetLevel) {
        double rate = plugin.getConfig().getDouble("success-rates." + targetLevel, 0.0);
        return Math.max(0.0, Math.min(100.0, rate));
    }

    public int getCost(int targetLevel) {
        return Math.max(0, plugin.getConfig().getInt("xp-costs." + targetLevel, 0));
    }

    public double getBonusDamage(int level) {
        return Math.max(0, plugin.getConfig().getDouble("bonus-damage-per-level", 0.5)) * level;
    }

    public double getDurabilitySaveRate(int level) {
        double percentPerLevel = plugin.getConfig()
                .getDouble("durability-save-percent-per-level", 2.0);
        return Math.max(0, Math.min(100, percentPerLevel * level));
    }

    
    public void applyUpgrade(ItemStack item, int newLevel) {
        if (!isSupported(item.getType()) || newLevel < 1 || newLevel > MAX_LEVEL) {
            throw new IllegalArgumentException("Invalid item type or enhancement level");
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Item metadata unavailable");
        }

        
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, newLevel);

        
        List<Component> lore = meta.lore() == null
                ? new ArrayList<>() : new ArrayList<>(meta.lore());
        lore.removeIf(line -> PlainTextComponentSerializer.plainText().serialize(line)
                .startsWith(LORE_PREFIX));
        lore.add(Component.text(LORE_PREFIX + "+" + newLevel, NamedTextColor.GOLD));
        if (isWeapon(item.getType())) {
            lore.add(Component.text(LORE_PREFIX + "추가 근접 피해 +"
                    + String.format(java.util.Locale.ROOT, "%.1f", getBonusDamage(newLevel)),
                    NamedTextColor.RED));
        }
        lore.add(Component.text(LORE_PREFIX + "내구도 소모 방지 "
                + String.format(java.util.Locale.ROOT, "%.0f%%", getDurabilitySaveRate(newLevel)),
                NamedTextColor.GREEN));

        
        if (isMiningTool(item.getType())) {
            int interval = Math.max(1, plugin.getConfig()
                    .getInt("mining-efficiency-every-levels", 3));
            int efficiencyLevel = Math.min(5, newLevel / interval);
            if (efficiencyLevel > meta.getEnchantLevel(Enchantment.EFFICIENCY)) {
                meta.addEnchant(Enchantment.EFFICIENCY, efficiencyLevel, true);
            }
            lore.add(Component.text(LORE_PREFIX + "채굴 효율 보너스 (최대 효율 V)",
                    NamedTextColor.AQUA));
        }

        meta.lore(lore);
        
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
    }
}
