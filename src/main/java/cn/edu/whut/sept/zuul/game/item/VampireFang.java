package cn.edu.whut.sept.zuul.game.item;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.combat.event.IPlayerListener;

public class VampireFang extends AbstractItem implements IPlayerListener {

    private Player owner;

    public VampireFang() {
        super("VampireFang", "吸血獠牙", "被诅咒的獠牙 (+8攻击, 攻击回血33%)", 5);
        setAttackRange(1);
        setAttackCooldown(600);
        setAttackType("melee");
    }

    @Override
    public void takenBy(Player player) {
        player.setAttack(player.getAttack() + 8);
        this.owner = player;
        player.addListener(this);
    }

    @Override
    public void droppedBy(Player player) {
        player.setAttack(player.getAttack() - 8);
        player.removeListener(this);
        this.owner = null;
    }

    @Override
    public void usedBy(Player player) {
    }

    @Override
    public void onHurt(Player player, AttackEvent event) {
        if (player == owner && event.getAttacker() == player) {
            int heal = Math.max(1, event.getDamage() / 3);
            player.setCurrentHealth(Math.min(player.getCurrentHealth() + heal, player.getMaxHealth()));
        }
    }

    @Override
    public void onDeath(Player player, DeathEvent event) {
    }

    @Override
    public void onFightWin(Player player, FightWinEvent event) {
    }
}
