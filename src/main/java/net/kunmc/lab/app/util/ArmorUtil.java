package net.kunmc.lab.app.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;

public class ArmorUtil {

    private static final Map<String, Integer> COLOR_RGB = new HashMap<>();
    private static final Map<String, ChatColor> COLOR_CHAT = new HashMap<>();

    static {
        register("BLACK", 0x1D1D21, ChatColor.BLACK);
        register("DARK_BLUE", 0x3C44AA, ChatColor.DARK_BLUE);
        register("DARK_GREEN", 0x5E7C16, ChatColor.DARK_GREEN);
        register("DARK_AQUA", 0x169C9C, ChatColor.DARK_AQUA);
        register("DARK_RED", 0xB02E26, ChatColor.DARK_RED);
        register("DARK_PURPLE", 0x8932B8, ChatColor.DARK_PURPLE);
        register("GOLD", 0xF9801D, ChatColor.GOLD);
        register("GRAY", 0x9D9D97, ChatColor.GRAY);
        register("DARK_GRAY", 0x474F52, ChatColor.DARK_GRAY);
        register("BLUE", 0x3AB3DA, ChatColor.BLUE);
        register("GREEN", 0x80C71F, ChatColor.GREEN);
        register("AQUA", 0x3AB3DA, ChatColor.AQUA);
        register("RED", 0xB02E26, ChatColor.RED);
        register("LIGHT_PURPLE", 0xC74EBD, ChatColor.LIGHT_PURPLE);
        register("YELLOW", 0xFED83D, ChatColor.YELLOW);
        register("WHITE", 0xF9FFFE, ChatColor.WHITE);
    }

    private static void register(String name, int rgb, ChatColor chatColor) {
        COLOR_RGB.put(name.toUpperCase(), rgb);
        COLOR_CHAT.put(name.toUpperCase(), chatColor);
    }

    public static ItemStack createColoredChestplate(int rgb) {
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.fromRGB(rgb));
            chestplate.setItemMeta(meta);
        }
        return chestplate;
    }

    /**
     * Minecraft標準色名からRGB整数値を取得。不明な場合はWHITEを返す。
     */
    public static int colorNameToRGB(String colorName) {
        if (colorName == null) return COLOR_RGB.get("WHITE");
        Integer rgb = COLOR_RGB.get(colorName.toUpperCase());
        return rgb != null ? rgb : COLOR_RGB.get("WHITE");
    }

    /**
     * Minecraft標準色名からChatColorを取得。不明な場合はWHITEを返す。
     */
    public static ChatColor colorNameToChatColor(String colorName) {
        if (colorName == null) return ChatColor.WHITE;
        ChatColor cc = COLOR_CHAT.get(colorName.toUpperCase());
        return cc != null ? cc : ChatColor.WHITE;
    }
}
