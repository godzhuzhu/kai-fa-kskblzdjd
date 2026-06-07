package cn.edu.whut.sept.zuul.game.combat.event;

import cn.edu.whut.sept.zuul.game.Player;

public class AttackEvent {

    private final Player attacker;
    private final Player target;
    private final int damage;

    public AttackEvent(Player attacker, Player target, int damage) {
        this.attacker = attacker;
        this.target = target;
        this.damage = damage;
    }

    public Player getAttacker() { return attacker; }
    public Player getTarget() { return target; }
    public int getDamage() { return damage; }
}
