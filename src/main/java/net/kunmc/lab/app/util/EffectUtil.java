package net.kunmc.lab.app.util;

import net.kunmc.lab.app.config.data.Effect;
import net.kunmc.lab.app.config.data.Role;
import net.kunmc.lab.app.config.data.Team;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class EffectUtil {

    public static List<PotionEffect> resolveEffects(Team team, Role role) {
        List<PotionEffect> result = new ArrayList<>();

        if (role != null) {
            // ロール固有のエフェクト
            for (Effect e : role.getEffects()) {
                result.add(e.toPotionEffect());
            }
            // extendsEffects=trueならチームエフェクトも追加
            if (role.isExtendsEffects()) {
                for (Effect e : team.getEffects()) {
                    result.add(e.toPotionEffect());
                }
            }
        } else {
            // ロールなし→チームエフェクトのみ
            for (Effect e : team.getEffects()) {
                result.add(e.toPotionEffect());
            }
        }

        return result;
    }

    public static void applyEffects(Player player, List<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect, true);
        }
    }
}
