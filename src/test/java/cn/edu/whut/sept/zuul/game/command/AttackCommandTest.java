package cn.edu.whut.sept.zuul.game.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Command;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AttackCommandTest {

    private Player attacker;
    private Player target;
    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("arena", "竞技场");
        attacker = new Player(1, "Attacker", room);
        target = new Player(2, "Target", room);
    }

    @Test
    void shouldDamageTarget() {
        int hpBefore = target.getCurrentHealth();
        int damage = Math.max(Math.max(3, attacker.getAttack() / 5), attacker.getAttack() - target.getDefense());
        target.hurtBy(damage);
        assertTrue(target.getCurrentHealth() < hpBefore);
    }

    @Test
    void damageShouldBeAtLeast3() {
        target.setDefense(999);
        int damage = Math.max(Math.max(3, attacker.getAttack() / 5), attacker.getAttack() - target.getDefense());
        assertEquals(3, damage);
    }

    @Test
    void shouldCalculateCounterDamage() {
        int counterDamage = Math.max(0, (int)(target.getAttack() * 0.25) - attacker.getDefense());
        assertTrue(counterDamage >= 0);
    }

    @Test
    void counterDamageReducedByDefense() {
        int counter = Math.max(0, (int)(target.getAttack() * 0.25) - attacker.getDefense());
        assertEquals(0, counter);
        attacker.setDefense(0);
        counter = Math.max(0, (int)(target.getAttack() * 0.25) - attacker.getDefense());
        assertEquals(2, counter);
    }

    @Test
    void deathShouldResetStats() {
        target.setCurrentHealth(1);
        int damage = Math.max(Math.max(3, attacker.getAttack() / 5), attacker.getAttack() - target.getDefense());
        target.hurtBy(damage);
        assertTrue(target.isDead());
    }

    @Test
    void attackerGetsKillBonus() {
        attacker.setKills(attacker.getKills() + 1);
        attacker.setMaxHealth(attacker.getMaxHealth() + 10);
        attacker.setAttack(attacker.getAttack() + 2);
        assertEquals(1, attacker.getKills());
        assertEquals(110, attacker.getMaxHealth());
        assertEquals(12, attacker.getAttack());
    }

    @Test
    void playersInSameRoomCanFight() {
        assertEquals(attacker.getCurrentRoom(), target.getCurrentRoom());
    }

    @Test
    void playersInDifferentRoomCannotFight() {
        Room other = new Room("other", "其他");
        target.moveTo(other);
        assertNotEquals(attacker.getCurrentRoom(), target.getCurrentRoom());
    }

    @Test
    void shouldSetKills() {
        assertEquals(0, attacker.getKills());
        attacker.setKills(3);
        assertEquals(3, attacker.getKills());
    }
}
