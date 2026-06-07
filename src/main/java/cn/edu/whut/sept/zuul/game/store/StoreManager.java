package cn.edu.whut.sept.zuul.game.store;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import cn.edu.whut.sept.zuul.game.item.Items;
import cn.edu.whut.sept.zuul.game.store.service.IStoreService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StoreManager {

    private final IStoreService storeService;

    public StoreManager(IStoreService storeService) {
        this.storeService = storeService;
    }

    public void saveGame(Game game, String saveName) {
        List<Room> allRooms = game.getAllRooms();
        List<Player> allPlayers = new ArrayList<>();
        Store store = Store.fromGame(allRooms, allPlayers);
        String json = StoreUtil.toJson(store);
        storeService.save(saveName, json);
    }

    public void loadGame(Game game, String saveName) {
        String json = storeService.load(saveName);
        if (json == null) {
            return;
        }
        Store store = StoreUtil.fromJson(json);
        if (store.getRooms() == null || store.getRooms().isEmpty()) {
            return;
        }

        List<Room> allRooms = game.getAllRooms();
        List<RoomSaver> roomSavers = store.getRooms();
        Map<String, Room> roomMap = new HashMap<>();
        for (int i = 0; i < roomSavers.size(); i++) {
            if (i < allRooms.size()) {
                RoomSaver rs = roomSavers.get(i);
                Room room = allRooms.get(i);
                roomMap.put(rs.getName(), room);
                for (String itemName : rs.getItemNames()) {
                    AbstractItem item = Items.createItem(itemName);
                    if (item != null) {
                        room.addItem(item);
                    }
                }
            }
        }

        if (store.getPlayers() != null) {
            for (PlayerSaver ps : store.getPlayers()) {
                Player player = game.getOrCreatePlayer(ps.getUserId());
                if (!roomMap.isEmpty() && ps.getCurrentRoomIndex() >= 0
                        && ps.getCurrentRoomIndex() < allRooms.size()) {
                    player.moveTo(allRooms.get(ps.getCurrentRoomIndex()));
                }
                for (String itemName : ps.getBagItemNames()) {
                    AbstractItem item = Items.createItem(itemName);
                    if (item != null) {
                        player.takeItem(item);
                    }
                }
                player.setMaxCapacity(ps.getMaxCapacity());
                player.setAttack(ps.getAttack());
                player.setDefense(ps.getDefense());
                player.setCurrentHealth(ps.getCurrentHealth());
            }
        }
    }
}
