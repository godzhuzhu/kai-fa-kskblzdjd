package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;

/**
 * 血瓶 — 道具类物品，使用后 +30 生命值（不超过上限），从背包移除。
 *
 * @author gmy
 * @since 1.0
 */
public class BloodVial extends AbstractItem {

    public BloodVial() {
        super("BloodVial", "A vial of restorative blood (+30 health)", 2);
    }

    @Override
    public void takenBy(Player player) {
    }

    @Override
    public void droppedBy(Player player) {
    }

    @Override
    public void usedBy(Player player) {
        int newHealth = player.getCurrentHealth() + 30;
        if (newHealth > player.getMaxHealth()) {
            newHealth = player.getMaxHealth();
        }
        player.setCurrentHealth(newHealth);
    }
}
