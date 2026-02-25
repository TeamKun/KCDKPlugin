package net.kunmc.lab.app.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ItemSerializeUtil {

    public static String serialize(ItemStack item) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack", e);
        }
    }

    public static ItemStack deserialize(String base64) {
        try {
            // URL-safe Base64('_', '-')と標準Base64('+', '/')の両方に対応
            byte[] decoded;
            if (base64.contains("_") || base64.contains("-")) {
                decoded = Base64.getUrlDecoder().decode(base64);
            } else {
                decoded = Base64.getDecoder().decode(base64);
            }
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
                 BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack", e);
        }
    }

    public static List<String> serializeContents(ItemStack[] items) {
        List<String> result = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null) {
                result.add(serialize(item));
            }
        }
        return result;
    }

    public static ItemStack[] deserializeContents(List<String> base64List) {
        if (base64List == null || base64List.isEmpty()) {
            return new ItemStack[0];
        }
        ItemStack[] result = new ItemStack[base64List.size()];
        for (int i = 0; i < base64List.size(); i++) {
            result[i] = deserialize(base64List.get(i));
        }
        return result;
    }
}
