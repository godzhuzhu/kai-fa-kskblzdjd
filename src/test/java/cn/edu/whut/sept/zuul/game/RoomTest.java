package cn.edu.whut.sept.zuul.game;

import cn.edu.whut.sept.zuul.game.item.Sword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("outside", "大学正门外");
    }

    @Test
    void shouldHaveName() {
        assertEquals("outside", room.getName());
        assertEquals("大学正门外", room.getShortDescription());
    }

    @Test
    void shouldSetExit() {
        Room theater = new Room("theater", "演讲厅");
        room.setExit("east", theater);
        assertEquals("theater", room.getExit("east").getName());
    }

    @Test
    void shouldNotBePortalByDefault() {
        assertFalse(room.isPortal());
    }

    @Test
    void shouldSetPortal() {
        room.setPortal(true);
        assertTrue(room.isPortal());
    }

    @Test
    void shouldAddAndRemoveItem() {
        Sword sword = new Sword();
        room.addItem(sword);
        assertTrue(room.getItems().contains(sword));
        room.removeItem("Sword");
        assertFalse(room.getItems().contains(sword));
    }

    @Test
    void shouldPlaceItemAtPosition() {
        Sword sword = new Sword();
        room.setTiles(new int[][]{{0,0},{0,0}});
        room.setWidth(2);
        room.setHeight(2);
        room.placeItem(sword, 1, 1);
        assertTrue(room.hasItemAt(1, 1));
    }

    @Test
    void shouldReturnNullForMissingExit() {
        assertNull(room.getExit("north"));
    }

    @Test
    void shouldGetExits() {
        room.setExit("east", new Room("east", "东方"));
        room.setExit("west", new Room("west", "西方"));
        assertEquals(2, room.getExits().size());
    }
}
