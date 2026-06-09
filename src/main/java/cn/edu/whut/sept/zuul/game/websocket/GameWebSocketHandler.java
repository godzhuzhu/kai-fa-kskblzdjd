package cn.edu.whut.sept.zuul.game.websocket;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Direction;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.item.AbstractItem;
import cn.edu.whut.sept.zuul.game.message.GameMessageBridge;
import cn.edu.whut.sept.zuul.game.user.security.JwtUtil;
import cn.edu.whut.sept.zuul.game.websocket.vo.PlayerVO;
import cn.edu.whut.sept.zuul.game.websocket.vo.RoomPlayerVO;
import cn.edu.whut.sept.zuul.game.websocket.vo.RoomVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final long HEARTBEAT_TIMEOUT = 60000;

    private final Game game;
    private final JwtUtil jwtUtil;
    private final GameMessageBridge messageBridge;
    private final ObjectMapper objectMapper;

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final Map<Integer, GameSession> playerSessions = new ConcurrentHashMap<>();

    private final RedisSessionManager redisSessionManager;
    private final RedisPubSubService redisPubSubService;

    public GameWebSocketHandler(Game game, JwtUtil jwtUtil, GameMessageBridge messageBridge,
                                RedisSessionManager redisSessionManager, RedisPubSubService redisPubSubService) {
        this.game = game;
        this.jwtUtil = jwtUtil;
        this.messageBridge = messageBridge;
        this.objectMapper = new ObjectMapper();
        this.redisSessionManager = redisSessionManager;
        this.redisPubSubService = redisPubSubService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenParam(session);
        if (token == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        int userId;
        try {
            userId = jwtUtil.validateToken(token);
        } catch (Exception e) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        Player player = game.getOrCreatePlayer(userId);
        player.setOnline(true);

        int[] sp = player.getCurrentRoom().getSpawnPoint();
        player.setPosX(sp[0]);
        player.setPosY(sp[1]);

        GameSession existing = playerSessions.remove(userId);
        if (existing != null) {
            sessions.remove(existing.getWebSocketSession().getId());
            existing.getPlayer().setOnline(false);
            try { existing.getWebSocketSession().close(); } catch (Exception ignored) {}
        }

        GameSession gameSession = new GameSession(player, session);
        sessions.put(session.getId(), gameSession);
        playerSessions.put(userId, gameSession);

        if (redisSessionManager != null) {
            redisSessionManager.playerOnline(player);
        }

        sendToSession(session, new WebSocketOutgoingPayload("playerPush", PlayerVO.from(player)));
        roomPush(player.getCurrentRoom());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        GameSession gameSession = sessions.get(session.getId());
        if (gameSession == null) {
            return;
        }

        WebSocketIncomingPayload payload = objectMapper.readValue(message.getPayload(), WebSocketIncomingPayload.class);
        String action = payload.getAction();

        if ("heartbeat".equals(action)) {
            gameSession.updateHeartbeat();
            if (redisSessionManager != null) {
                redisSessionManager.updateHeartbeat(gameSession.getPlayer());
            }
            return;
        }

        if ("command".equals(action)) {
            String cmd = payload.getData();
            Player player = gameSession.getPlayer();
            game.processCommand(player, cmd);
            playerPush(player);
            roomPush(player.getCurrentRoom());
            messagePush(player, game.getLastCommandOutput());
        }

        if ("move".equals(action)) {
            handleMove(gameSession.getPlayer(), payload);
        }

        if ("interact".equals(action)) {
            handleInteract(gameSession.getPlayer());
        }

        if ("drop".equals(action)) {
            handleDrop(gameSession.getPlayer(), payload.getData());
        }

        if ("use".equals(action)) {
            handleUse(gameSession.getPlayer(), payload.getData());
        }

        if ("attack".equals(action)) {
            try {
                com.fasterxml.jackson.databind.JsonNode n = objectMapper.readTree(payload.getData());
                handleAttack(gameSession.getPlayer(), n.get("dx").asInt(), n.get("dy").asInt());
            } catch (Exception e) {
                handleAttack(gameSession.getPlayer(), payload.getData());
            }
        }
    }

    private void handleMove(Player player, WebSocketIncomingPayload payload) {
        String dataStr = payload.getData();
        if (dataStr == null) return;
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(dataStr);
            int dx = node.has("dx") ? node.get("dx").asInt() : 0;
            int dy = node.has("dy") ? node.get("dy").asInt() : 0;

            int newX = player.getPosX() + dx;
            int newY = player.getPosY() + dy;
            Room currentRoom = player.getCurrentRoom();

            if (!currentRoom.isWalkable(newX, newY)) {
                return;
            }

            Direction doorDir = currentRoom.getDoorDirection(newX, newY);
            if (doorDir != null) {
                Room nextRoom = currentRoom.getExitMap().get(doorDir.toLower());
                if (nextRoom == null) return;
                if (nextRoom.isPortal()) {
                    Room randomRoom = game.getRandomRoom();
                    if (randomRoom != null) {
                        nextRoom = randomRoom;
                    }
                }
                player.moveTo(nextRoom);
                int[] sp = nextRoom.getSpawnPoint();
                player.setPosX(sp[0]);
                player.setPosY(sp[1]);
            } else {
                player.setPosX(newX);
                player.setPosY(newY);
            }

            playerPush(player);
            roomPush(player.getCurrentRoom());
        } catch (Exception ignored) {
        }
    }

    private void handleInteract(Player player) {
        Room currentRoom = player.getCurrentRoom();
        AbstractItem item = currentRoom.takeItemAt(player.getPosX(), player.getPosY());
        if (item != null && player.takeItem(item)) {
            messagePush(player, "You picked up " + item.getName() + ".");
        }
        playerPush(player);
        roomPush(player.getCurrentRoom());
    }

    private void handleDrop(Player player, String itemName) {
        if (itemName == null) return;
        for (AbstractItem item : player.getBag()) {
            if (item.getName().equals(itemName)) {
                player.dropItem(item);
                Room room = player.getCurrentRoom();
                int x = player.getPosX();
                int y = player.getPosY() + 1;
                if (!room.isWalkable(x, y)) { x = player.getPosX(); y = player.getPosY() - 1; }
                if (!room.isWalkable(x, y)) { x = player.getPosX() + 1; y = player.getPosY(); }
                if (!room.isWalkable(x, y)) { x = player.getPosX() - 1; y = player.getPosY(); }
                room.placeItem(item, x, y);
                messagePush(player, "You dropped " + itemName + ".");
                break;
            }
        }
        playerPush(player);
        roomPush(player.getCurrentRoom());
    }

    private void handleUse(Player player, String itemName) {
        if (itemName == null) return;
        for (AbstractItem item : player.getBag()) {
            if (item.getName().equals(itemName)) {
                if (item.isWeapon()) {
                    messagePush(player, itemName + " is a weapon. Pick it up to equip.");
                    return;
                }
                player.useItem(item);
                messagePush(player, "Used " + itemName + ".");
                break;
            }
        }
        playerPush(player);
        roomPush(player.getCurrentRoom());
    }

    private void handleAttack(Player player, String targetName) {
        game.processCommand(player, "attack " + targetName);
        playerPush(player);
        roomPush(player.getCurrentRoom());
    }

    private void handleAttack(Player attacker, int dx, int dy) {
        long now = System.currentTimeMillis();

        AbstractItem weapon = null;
        int maxRange = 1;
        int cooldown = 300;
        String atkType = "melee";
        for (AbstractItem item : attacker.getBag()) {
            if (item.isWeapon() && item.getAttackRange() > maxRange) {
                weapon = item;
                maxRange = item.getAttackRange();
                cooldown = item.getAttackCooldown();
                atkType = item.getAttackType();
            }
        }
        if (now - attacker.getLastAttackTime() < cooldown) return;
        attacker.setLastAttackTime(now);

        int totalDamage = attacker.getAttack();
        Room room = attacker.getCurrentRoom();

        if ("melee".equals(atkType)) {
            int tx = attacker.getPosX() + dx, ty = attacker.getPosY() + dy;
            hitPlayer(attacker, room, tx, ty, totalDamage);
        } else if ("ranged".equals(atkType)) {
            for (int i = 1; i <= maxRange; i++) {
                int tx = attacker.getPosX() + dx * i, ty = attacker.getPosY() + dy * i;
                if (!room.isWalkable(tx, ty)) break;
                if (hitPlayer(attacker, room, tx, ty, totalDamage)) break;
            }
        } else if ("aoe".equals(atkType)) {
            for (int ox = -1; ox <= 1; ox++)
                for (int oy = -1; oy <= 1; oy++)
                    hitPlayer(attacker, room, attacker.getPosX() + ox, attacker.getPosY() + oy, totalDamage / 2);
        }

        playerPush(attacker);
        roomPush(room);
    }

    private boolean hitPlayer(Player attacker, Room room, int tx, int ty, int damage) {
        for (Player p : getPlayersInRoom(room, attacker)) {
            if (p.getPosX() == tx && p.getPosY() == ty) {
                int dmg = Math.max(1, damage - p.getDefense());
                p.hurtBy(dmg);
                if (p.isDead()) {
                    for (AbstractItem item : new ArrayList<>(p.getBag())) {
                        p.dropItem(item);
                        room.placeItem(item, p.getPosX(), p.getPosY());
                    }
                    p.setCurrentHealth(p.getMaxHealth());
                    p.setAttack(10); p.setDefense(5);
                    int[] sp = p.getCurrentRoom().getSpawnPoint();
                    p.setPosX(sp[0]); p.setPosY(sp[1]);
                }
                messagePush(attacker, "Hit " + p.getPlayerName() + " for " + dmg + " damage!");
                messagePush(p, attacker.getPlayerName() + " hit you for " + dmg + " damage!");
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        GameSession gameSession = sessions.remove(session.getId());
        if (gameSession != null) {
            gameSession.getPlayer().setOnline(false);
            playerSessions.remove(gameSession.getPlayer().getUserId());
            if (redisSessionManager != null) {
                redisSessionManager.playerOffline(gameSession.getPlayer());
            }
        }
    }

    public void playerPush(Player player) {
        GameSession session = playerSessions.get(player.getUserId());
        if (session != null) {
            sendToSession(session.getWebSocketSession(),
                    new WebSocketOutgoingPayload("playerPush", PlayerVO.from(player)));
        }
    }

    public void roomPush(Room room) {
        List<Player> playersInRoom = getPlayersInRoom(room, null);
        List<RoomPlayerVO> playerVOs = RoomPlayerVO.fromList(playersInRoom);
        WebSocketOutgoingPayload payload = new WebSocketOutgoingPayload("roomPush", RoomVO.from(room, playerVOs));

        if (redisPubSubService != null) {
            try {
                String json = objectMapper.writeValueAsString(payload);
                redisPubSubService.publish(room.getName(), json);
            } catch (Exception ignored) {
            }
        }

        if (redisPubSubService == null) {
            for (Player p : playersInRoom) {
                GameSession session = playerSessions.get(p.getUserId());
                if (session != null) {
                    sendToSession(session.getWebSocketSession(), payload);
                }
            }
        }
    }

    public void handlePubSubMessage(String roomName, String jsonPayload) {
        try {
            WebSocketOutgoingPayload payload = objectMapper.readValue(jsonPayload, WebSocketOutgoingPayload.class);
            for (GameSession session : sessions.values()) {
                Player p = session.getPlayer();
                if (p.isOnline() && p.getCurrentRoom().getName().equals(roomName)) {
                    sendToSession(session.getWebSocketSession(), payload);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void messagePush(Player player, String msg) {
        GameSession session = playerSessions.get(player.getUserId());
        if (session != null) {
            sendToSession(session.getWebSocketSession(),
                    new WebSocketOutgoingPayload("messagePush", msg));
        }
    }

    public void broadcastToRoom(Room room, String msg) {
        List<Player> players = getPlayersInRoom(room, null);
        for (Player p : players) {
            messagePush(p, msg);
        }
    }

    public void broadcastLocal(WebSocketOutgoingPayload payload) {
        for (GameSession session : sessions.values()) {
            sendToSession(session.getWebSocketSession(), payload);
        }
    }

    public List<Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();
        if (redisSessionManager != null) {
            List<Integer> ids = redisSessionManager.getOnlinePlayerIds();
            for (int id : ids) {
                GameSession session = playerSessions.get(id);
                if (session != null && session.getWebSocketSession().isOpen()) {
                    Player p = session.getPlayer();
                    if (p.isOnline()) {
                        online.add(p);
                    }
                }
            }
        } else {
            for (GameSession session : sessions.values()) {
                Player p = session.getPlayer();
                if (p.isOnline() && session.getWebSocketSession().isOpen()) {
                    online.add(p);
                }
            }
        }
        return online;
    }

    public List<Player> getPlayersInRoom(Room room, Player exclude) {
        List<Player> result = new ArrayList<>();
        if (redisSessionManager != null) {
            List<Integer> ids = redisSessionManager.getPlayersInRoom(room);
            for (int id : ids) {
                GameSession session = playerSessions.get(id);
                if (session != null && session.getWebSocketSession().isOpen()) {
                    Player p = session.getPlayer();
                    if (p.isOnline() && p != exclude) {
                        result.add(p);
                    }
                } else if (session != null) {
                    session.getPlayer().setOnline(false);
                }
            }
        } else {
            for (GameSession session : sessions.values()) {
                Player p = session.getPlayer();
                if (p.isOnline() && p.getCurrentRoom() == room && p != exclude
                        && session.getWebSocketSession().isOpen()) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    public void checkHeartbeats() {
        if (redisSessionManager != null) {
            redisSessionManager.checkHeartbeats(HEARTBEAT_TIMEOUT);
            return;
        }

        long now = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, GameSession> entry : sessions.entrySet()) {
            if (now - entry.getValue().getLastHeartbeat() > HEARTBEAT_TIMEOUT) {
                toRemove.add(entry.getKey());
            }
        }
        for (String id : toRemove) {
            GameSession session = sessions.remove(id);
            if (session != null) {
                session.getPlayer().setOnline(false);
                playerSessions.remove(session.getPlayer().getUserId());
                try {
                    session.getWebSocketSession().close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public GameMessageBridge getMessageBridge() {
        return messageBridge;
    }

    private void sendToSession(WebSocketSession session, WebSocketOutgoingPayload payload) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        } catch (IOException ignored) {
        }
    }

    private String getTokenParam(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "token".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}