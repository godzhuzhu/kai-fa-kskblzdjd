package cn.edu.whut.sept.zuul.game.store;

import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import java.util.ArrayList;
import java.util.List;

public class Store {

    private List<RoomSaver> rooms;
    private List<PlayerSaver> players;

    public List<RoomSaver> getRooms() { return rooms; }
    public void setRooms(List<RoomSaver> rooms) { this.rooms = rooms; }
    public List<PlayerSaver> getPlayers() { return players; }
    public void setPlayers(List<PlayerSaver> players) { this.players = players; }

    public static Store fromGame(List<Room> allRooms, List<cn.edu.whut.sept.zuul.game.Player> allPlayers) {
        Store store = new Store();
        List<RoomSaver> roomSavers = new ArrayList<>();
        for (Room room : allRooms) {
            RoomSaver rs = new RoomSaver();
            rs.setName(room.getName());
            rs.setDescription(room.getShortDescription());
            rs.setPortal(room.isPortal());
            List<String> exitNames = new ArrayList<>(room.getExits());
            rs.setExits(exitNames);
            List<String> itemNames = new ArrayList<>();
            for (AbstractItem item : room.getItems()) {
                itemNames.add(item.getName());
            }
            rs.setItemNames(itemNames);
            roomSavers.add(rs);
        }
        store.setRooms(roomSavers);

        List<PlayerSaver> playerSavers = new ArrayList<>();
        for (cn.edu.whut.sept.zuul.game.Player player : allPlayers) {
            PlayerSaver ps = new PlayerSaver();
            ps.setUserId(player.getUserId());
            ps.setPlayerName(player.getPlayerName());
            ps.setCurrentRoomIndex(allRooms.indexOf(player.getCurrentRoom()));
            List<Integer> prevIndexes = new ArrayList<>();
            for (Room r : player.getPreviousRooms()) {
                prevIndexes.add(allRooms.indexOf(r));
            }
            ps.setPreviousRoomIndexes(prevIndexes);
            List<String> bagNames = new ArrayList<>();
            for (AbstractItem item : player.getBag()) {
                bagNames.add(item.getName());
            }
            ps.setBagItemNames(bagNames);
            ps.setMaxCapacity(player.getMaxCapacity());
            ps.setAttack(player.getAttack());
            ps.setDefense(player.getDefense());
            ps.setCurrentHealth(player.getCurrentHealth());
            ps.setMaxHealth(player.getMaxHealth());
            playerSavers.add(ps);
        }
        store.setPlayers(playerSavers);
        return store;
    }
}
