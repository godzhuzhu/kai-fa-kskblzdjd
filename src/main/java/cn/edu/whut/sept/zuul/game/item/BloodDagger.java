package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;

public class BloodDagger extends AbstractItem implements IPlayerListener {

    public BloodDagger() {
        super("BloodDagger", "A blood-thirsty dagger (heal 20 HP on kill)", 4);
    }

    @Override
    public void takenBy(Player player) {
        player.addListener(this);
    }

    @Override
    public void droppedBy(Player player) {
        player.removeListener(this);
    }

    @Override
    public void usedBy(Player player) {
    }

    @Override
    public void onHurt(Player player, AttackEvent event) {
    }

    @Override
    public void onDeath(Player player, DeathEvent event) {
    }

    @Override
    public void onFightWin(Player player, FightWinEvent event) {
        int newHealth = Math.min(player.getCurrentHealth() + 20, player.getMaxHealth());
        player.setCurrentHealth(newHealth);
    }
}
