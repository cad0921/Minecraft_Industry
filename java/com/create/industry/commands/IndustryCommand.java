package com.create.industry.commands;

import com.create.industry.IndustryPlugin;
import com.create.industry.util.ItemUtil;
import com.create.industry.util.MachineMaterials;
import com.create.industry.util.PdcUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IndustryCommand implements CommandExecutor, TabCompleter {
    private final PdcUtil pdc = new PdcUtil(IndustryPlugin.inst());
    private final IndustryPlugin plugin;

    private static final List<String> SUBS = Arrays.asList("give", "reload");
    private static final List<String> MACHINES = Arrays.asList(
            "FUEL_GEN", "SOLAR_GEN", "WIND_GEN", "HYDRO_GEN",
            "BATTERY_BASIC", "BATTERY_ADV",
            "AUTO_MINER", "MACERATOR", "COMPRESSOR", "ASSEMBLER",
            "MAT_CONVERTER"
    );

    public IndustryCommand(IndustryPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("industry.admin")) return true;

        if (args.length == 0) {
            p.sendMessage(Component.text("/industry give <ID> [tier] | /industry reload"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 2) {
                    p.sendMessage(Component.text("用法: /industry give <ID> [tier]"));
                    return true;
                }
                String id = args[1].toUpperCase();

                int tier = 1;
                if (args.length >= 3) {
                    try {
                        tier = Math.max(1, Integer.parseInt(args[2])); // 低於1就當1
                    } catch (NumberFormatException ex) {
                        p.sendMessage(Component.text("等級必須是數字，已設為 1。"));
                    }
                }

                Material disp = MachineMaterials.displayOf(id);
                ItemStack is = ItemUtil.named(disp, id);

                // ✅ 只需要 tag 一次（你原本多呼叫了一次）
                pdc.tagMachine(is, id, tier);

                p.getInventory().addItem(is);
                p.sendMessage(Component.text("已給予機器: " + id + " (等級 " + tier + ")"));
                return true;
            }
            case "reload" -> {
                plugin.machinesConfig().reload();
                plugin.recipes().reload(); // 確保 RecipeRegistry 有實作 reload()
                p.sendMessage(Component.text("Industry 配置已重載"));
                return true;
            }
            default -> {
                p.sendMessage(Component.text("/industry give <ID> [tier] | /industry reload"));
                return true;
            }
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("industry.admin")) return Collections.emptyList();
        if (args.length == 1) {
            return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return MACHINES.stream().filter(s -> s.startsWith(args[1].toUpperCase())).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("1","2","3","4","5");
        }
        return new ArrayList<>();
    }
}
