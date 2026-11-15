package com.example.toolenhance;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/** Paper 26.3 플러그인의 진입점. */
public final class ToolEnhancePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // plugins/ToolEnhance/config.yml이 없으면 기본 설정 파일을 생성합니다.
        saveDefaultConfig();

        EnhancementService enhancementService = new EnhancementService(this);

        // Paper 26.3이 권장하는 BasicCommand 등록 방식입니다.
        registerCommand(
                "enhance",
                "손에 든 도구를 최대 +15까지 강화합니다",
                List.of("ganghwa", "upgrade"),
                new EnhanceCommand(this, enhancementService)
        );

        // 공격력 및 내구도 확률 효과를 실제 게임 이벤트에 연결합니다.
        getServer().getPluginManager().registerEvents(
                new EnhancementListener(enhancementService), this
        );

        getLogger().info("ToolEnhance 1.0.0 enabled. /enhance");
    }
}
