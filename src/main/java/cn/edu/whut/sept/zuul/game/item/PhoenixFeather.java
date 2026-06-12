package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;

public class PhoenixFeather extends AbstractItem implements IPlayerListener {

    private boolean used = false;

    public PhoenixFeather() {
        super("PhoenixFeather", "凤凰羽毛", "燃烧的羽毛 (死亡时复活，恢复50%生命)", 2);
        setPassiveEffect(true);
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
        if (!used) {
            used = true;
            player.setCurrentHealth(player.getMaxHealth() / 2);
            player.removeListener(this);
            player.removeFromBag(this);
            if (player.getEquippedArmor() == this) {
                player.discardArmor();
            }
        }
    }

    @Override
    public void onFightWin(Player player, FightWinEvent event) {
    }
}
