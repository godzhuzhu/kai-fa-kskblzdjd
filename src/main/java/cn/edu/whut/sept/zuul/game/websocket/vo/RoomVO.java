package cn.edu.whut.sept.zuul.game.websocket.vo;

import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import cn.edu.whut.sept.zuul.game.websocket.vo.BagItemVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomVO {

    private String roomName;
    private String description;
    private List<Map<String, Object>> items;
    private List<String> exits;
    private List<RoomPlayerVO> players;
    private boolean portal;
    private int[][] tiles;
    private int posX;
    private int posY;

    public static RoomVO from(Room r, List<RoomPlayerVO> players) {
        RoomVO vo = new RoomVO();
        vo.roomName = r.getName();
        vo.description = r.getShortDescription();
        vo.portal = r.isPortal();
        vo.items = new ArrayList<>();
        for (AbstractItem item : r.getItems()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("name", item.getName());
            itemData.put("displayName", item.getDisplayName());
            itemData.put("description", item.getDescription());
            itemData.put("weight", item.getWeight());
            itemData.put("range", item.getAttackRange());
            itemData.put("type", item.getAttackType());
            int[] pos = r.getItemPosition(item.getName());
            if (pos != null) {
                itemData.put("x", pos[0]);
                itemData.put("y", pos[1]);
            }
            vo.items.add(itemData);
        }
        vo.exits = new ArrayList<>(r.getExits());
        vo.players = players;
        vo.tiles = r.getTiles();
        return vo;
    }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
    public List<String> getExits() { return exits; }
    public void setExits(List<String> exits) { this.exits = exits; }
    public List<RoomPlayerVO> getPlayers() { return players; }
    public void setPlayers(List<RoomPlayerVO> players) { this.players = players; }
    public boolean isPortal() { return portal; }
    public void setPortal(boolean portal) { this.portal = portal; }
    public int[][] getTiles() { return tiles; }
    public void setTiles(int[][] tiles) { this.tiles = tiles; }
}
