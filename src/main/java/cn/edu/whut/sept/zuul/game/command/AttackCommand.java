package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.combat.event.AttackEvent;
import cn.edu.whut.sept.zuul.game.combat.event.DeathEvent;
import cn.edu.whut.sept.zuul.game.combat.event.FightWinEvent;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;
import cn.edu.whut.sept.zuul.game.websocket.GameWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

public class AttackCommand extends Command {

    private static final long COOLDOWN_MS = 2000;

    @Override
    public boolean execute(Game game, Player player) {
        if (!hasSecondWord()) {
            game.getMessageBridge().send(new SinglePlayerMessage("攻击谁？"), player);
            return false;
        }

        String targetName = getSecondWord();

        if (game.getWebSocketHandler() == null) {
            game.getMessageBridge().send(new SinglePlayerMessage("没有叫 " + targetName + " 的在线玩家。"), player);
            return false;
        }

        GameWebSocketHandler webSocketHandler = game.getWebSocketHandler();

        Player target = null;
        for (Player p : webSocketHandler.getOnlinePlayers()) {
            if (p.getPlayerName().equals(targetName)) {
                target = p;
                break;
            }
        }

        if (target == null) {
            game.getMessageBridge().send(new SinglePlayerMessage("没有叫 " + targetName + " 的在线玩家。"), player);
            return false;
        }

        if (target == player) {
            game.getMessageBridge().send(new SinglePlayerMessage("你不能攻击自己！"), player);
            return false;
        }

        if (player.getCurrentRoom() != target.getCurrentRoom()) {
            game.getMessageBridge().send(new SinglePlayerMessage("那个玩家不在这里！"), player);
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - player.getLastAttackTime() < COOLDOWN_MS) {
            game.getMessageBridge().send(new SinglePlayerMessage("你暂时无法攻击！"), player);
            return false;
        }
        player.setLastAttackTime(now);

        int damage = Math.max(Math.max(3, player.getAttack() / 5), player.getAttack() - target.getDefense());
        target.hurtBy(damage);
        AttackEvent attackEvent = new AttackEvent(player, target, damage);
        player.notifyHurt(attackEvent);
        target.notifyHurt(attackEvent);

        int counterDamage = Math.max(0, (int)(target.getAttack() * 0.25) - player.getDefense());
        if (counterDamage > 0) {
            player.hurtBy(counterDamage);
            game.getMessageBridge().send(
                new SinglePlayerMessage(target.getPlayerName() + " 反击造成 " + counterDamage + " 点伤害！"), player);
        }

        if (player.isDead()) {
            handleDeath(game, target, player);
        }
        if (target.isDead()) {
            handleDeath(game, player, target);
        }

        webSocketHandler.roomPush(player.getCurrentRoom());

        return false;
    }

    private void handleDeath(Game game, Player killer, Player victim) {
        DeathEvent deathEvent = new DeathEvent(killer, victim);
        victim.notifyDeath(deathEvent);

        if (!victim.isDead()) {
            return;
        }

        if (victim.getEquippedWeapon() != null) {
            victim.unequipWeapon();
        }
        if (victim.getEquippedArmor() != null) {
            victim.unequipArmor();
        }

        List<AbstractItem> toDrop = new ArrayList<>(victim.getBag());
        for (AbstractItem item : toDrop) {
            victim.dropItem(item);
            victim.getCurrentRoom().addItem(item);
        }

        victim.setAttack(10);
        victim.setDefense(5);
        victim.setCurrentHealth(victim.getMaxHealth());
        victim.getPreviousRooms().clear();
        victim.getCurrentRoom().addItem(new cn.edu.whut.sept.zuul.game.item.Sword());
        victim.moveTo(game.getStartingRoom());

        FightWinEvent winEvent = new FightWinEvent(killer, victim);
        killer.notifyFightWin(winEvent);

        killer.setMaxHealth(killer.getMaxHealth() + 10);
        killer.setCurrentHealth(Math.min(killer.getCurrentHealth() + 10, killer.getMaxHealth()));
        killer.setAttack(killer.getAttack() + 2);
        killer.setKills(killer.getKills() + 1);

        GameWebSocketHandler handler = game.getWebSocketHandler();
        if (handler != null) {
            handler.broadcastRankings();
        }

        game.getMessageBridge().send(
                new SinglePlayerMessage("你击败了 " + victim.getPlayerName() + "！+2 ATK, +10 MaxHP"), killer);
        game.getMessageBridge().send(
                new SinglePlayerMessage("你被 " + killer.getPlayerName() + " 击败了！"), victim);
    }
}
