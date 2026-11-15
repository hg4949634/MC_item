package com.example.toolenhance;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public final class ToolEnhancePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        
        saveDefaultConfig();

        EnhancementService enhancementService = new EnhancementService(this);

       
        registerCommand(
                "enhance",
                "손에 든 도구를 최대 +15까지 강화합니다",
                List.of("ganghwa", "upgrade"),
                new EnhanceCommand(this, enhancementService)
        );

        
        getServer().getPluginManager().registerEvents(
                new EnhancementListener(enhancementService), this
        );

        getLogger().info("ToolEnhance 1.0.0 enabled. /enhance");
    }
}
