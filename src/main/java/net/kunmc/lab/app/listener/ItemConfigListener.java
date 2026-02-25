package net.kunmc.lab.app.listener;

import net.kunmc.lab.app.Store;
import net.kunmc.lab.app.config.data.GameConfig;
import net.kunmc.lab.app.config.data.Role;
import net.kunmc.lab.app.config.data.Team;
import net.kunmc.lab.app.util.ItemSerializeUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemConfigListener implements Listener {

    private static final String TITLE_PREFIX = "Items: ";

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(TITLE_PREFIX)) return;

        String target = title.substring(TITLE_PREFIX.length());
        String[] parts = target.split("\\.", 2);
        String teamName = parts[0];
        String roleName = parts.length > 1 ? parts[1] : null;

        GameConfig config = Store.config.getGameConfig();
        Team team = config.getTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst().orElse(null);

        if (team == null) return;

        Inventory inv = event.getInventory();
        ItemStack[] contents = inv.getContents();
        List<String> serialized = ItemSerializeUtil.serializeContents(contents);

        if (roleName != null) {
            Role role = team.getRoles().stream()
                    .filter(r -> r.getName().equalsIgnoreCase(roleName))
                    .findFirst().orElse(null);
            if (role != null) {
                role.setItems(serialized);
            }
        } else {
            team.setItems(serialized);
        }

        // 保存
        Store.config.saveGameConfig();
        event.getPlayer().sendMessage("§aアイテム設定を保存しました。");
    }
}
