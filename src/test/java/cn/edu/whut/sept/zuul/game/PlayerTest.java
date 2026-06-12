package cn.edu.whut.sept.zuul.game;

import cn.edu.whut.sept.zuul.game.item.Sword;
import cn.edu.whut.sept.zuul.game.item.BloodVial;
import cn.edu.whut.sept.zuul.game.item.StormCleaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;
    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("test", "测试房间");
        player = new Player(1, "TestPlayer", room);
    }

    @Test
    void shouldStartWithDefaultStats() {
        assertEquals(10, player.getAttack());
        assertEquals(5, player.getDefense());
        assertEquals(100, player.getMaxHealth());
        assertEquals(100, player.getCurrentHealth());
        assertEquals(0, player.getCurrentLoad());
        assertEquals(50, player.getMaxCapacity());
    }

    @Test
    void shouldHurtCorrectly() {
        player.hurtBy(30);
        assertEquals(70, player.getCurrentHealth());
    }

    @Test
    void shouldNotGoBelowZero() {
        player.hurtBy(150);
        assertEquals(0, player.getCurrentHealth());
        assertTrue(player.isDead());
    }

    @Test
    void shouldBeDeadWhenHealthZero() {
        assertFalse(player.isDead());
        player.hurtBy(100);
        assertTrue(player.isDead());
    }

    @Test
    void shouldTakeItem() {
        Sword sword = new Sword();
        assertTrue(player.takeItem(sword));
        assertTrue(player.getBag().contains(sword));
        assertEquals(8, player.getCurrentLoad());
    }

    @Test
    void shouldRejectItemWhenOverCapacity() {
        player.setMaxCapacity(5);
        Sword sword = new Sword();
        assertFalse(player.takeItem(sword));
        assertEquals(0, player.getBag().size());
    }

    @Test
    void shouldEquipWeapon() {
        Sword sword = new Sword();
        player.takeItem(sword);
        player.equipWeapon(sword);
        assertEquals(sword, player.getEquippedWeapon());
        assertEquals(20, player.getAttack());
    }

    @Test
    void shouldUnequipWeapon() {
        Sword sword = new Sword();
        player.takeItem(sword);
        player.equipWeapon(sword);
        player.unequipWeapon();
        assertNull(player.getEquippedWeapon());
        assertEquals(10, player.getAttack());
    }

    @Test
    void shouldUseConsumable() {
        player.setCurrentHealth(50);
        BloodVial vial = new BloodVial();
        player.takeItem(vial);
        player.useItem(vial);
        assertEquals(90, player.getCurrentHealth());
        assertFalse(player.getBag().contains(vial));
    }

    @Test
    void shouldMoveToRoom() {
        Room other = new Room("other", "其他房间");
        player.moveTo(other);
        assertEquals(other, player.getCurrentRoom());
    }

    @Test
    void shouldGoBack() {
        room.setExit("east", new Room("east", "东方"));
        player.goBack();
        assertEquals("test", player.getCurrentRoom().getName());
    }

    @Test
    void shouldUpdateKills() {
        assertEquals(0, player.getKills());
        player.setKills(5);
        assertEquals(5, player.getKills());
    }

    @Test
    void shouldSetPosition() {
        player.setPosX(7);
        player.setPosY(5);
        assertEquals(7, player.getPosX());
        assertEquals(5, player.getPosY());
    }
}
