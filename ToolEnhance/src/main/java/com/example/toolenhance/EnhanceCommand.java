package com.example.toolenhance;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/** /enhance, /enhance info, /enhance rates, /enhance reload  */
public final class EnhanceCommand implements BasicCommand {
    private final ToolEnhancePlugin plugin;
    private final EnhancementService service;

    public EnhanceCommand(ToolEnhancePlugin plugin, EnhancementService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("toolenhance.admin")) {
                send(sender, "설정을 다시 불러올 권한이 없습니다.", NamedTextColor.RED);
                return;
            }
            plugin.reloadConfig();
            send(sender, "강화 설정을 다시 불러왔습니다.", NamedTextColor.GREEN);
            return;
        }

        if (!sender.hasPermission("toolenhance.use")) {
            send(sender, "강화할 권한이 없습니다.", NamedTextColor.RED);
            return;
        }
        if (!(sender instanceof Player player)) {
            send(sender, "게임 안에서 플레이어가 사용해야 합니다.", NamedTextColor.RED);
            return;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("rates")) {
            send(player, "===== 강화 확률 / 비용 =====", NamedTextColor.GOLD);
            for (int level = 1; level <= EnhancementService.MAX_LEVEL; level++) {
                showNextLevel(player, level);
            }
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!service.isSupported(item.getType())) {
            send(player, "검, 곡괭이, 도끼, 삽, 괭이 중 하나를 손에 들어주세요.", NamedTextColor.RED);
            return;
        }

        int currentLevel = service.getLevel(item);
        if (args.length > 0) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "info" -> {
                    send(player, "현재 강화 단계: +" + currentLevel, NamedTextColor.AQUA);
                    if (currentLevel < EnhancementService.MAX_LEVEL) {
                        showNextLevel(player, currentLevel + 1);
                    } else {
                        send(player, "최대 강화 단계입니다!", NamedTextColor.GOLD);
                    }
                }
                default -> send(player,
                        "사용법: /enhance, /enhance info, /enhance rates, /enhance reload",
                        NamedTextColor.YELLOW);
            }
            return;
        }

        if (currentLevel >= EnhancementService.MAX_LEVEL) {
            send(player, "이미 +15까지 강화된 도구입니다.", NamedTextColor.GOLD);
            return;
        }

        int targetLevel = currentLevel + 1;
        int cost = service.getCost(targetLevel);
        double rate = service.getRate(targetLevel);

        
        if (player.getLevel() < cost) {
            send(player, "경험치 레벨이 부족합니다. 필요한 레벨: " + cost,
                    NamedTextColor.RED);
            return;
        }
        player.setLevel(player.getLevel() - cost);

        boolean success = ThreadLocalRandom.current().nextDouble(100.0) < rate;
        if (success) {
            service.applyUpgrade(item, targetLevel);
            player.getInventory().setItemInMainHand(item);
            send(player, "강화 성공! +" + currentLevel + " → +" + targetLevel
                    + " (소모: " + cost + "레벨)", NamedTextColor.GREEN);
        } else {
            send(player, "강화 실패! +" + currentLevel + " 유지 (소모: "
                    + cost + "레벨)", NamedTextColor.RED);
        }
    }

    private void showNextLevel(CommandSender sender, int level) {
        String rate = String.format(Locale.ROOT, "%.1f", service.getRate(level));
        send(sender, "+" + level + " | 성공률 " + rate + "% | 비용 "
                + service.getCost(level) + "레벨", NamedTextColor.YELLOW);
    }

    private void send(CommandSender sender, String message, NamedTextColor color) {
        sender.sendMessage(Component.text("[ToolEnhance] " + message, color));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length > 1) {
            return List.of();
        }
        List<String> options = source.getSender().hasPermission("toolenhance.admin")
                ? List.of("info", "rates", "reload") : List.of("info", "rates");
        String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.startsWith(prefix)).toList();
    }
}
