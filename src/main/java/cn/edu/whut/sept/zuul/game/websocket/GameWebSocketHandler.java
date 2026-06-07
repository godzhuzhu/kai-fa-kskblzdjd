package cn.edu.whut.sept.zuul.game.websocket;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import cn.edu.whut.sept.zuul.game.message.GameMessageBridge;
import cn.edu.whut.sept.zuul.game.message.IMessage;
import cn.edu.whut.sept.zuul.game.message.SinglePlayerMessage;
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

    public GameWebSocketHandler(Game game, JwtUtil jwtUtil, GameMessageBridge messageBridge) {
        this.game = game;
        this.jwtUtil = jwtUtil;
        this.messageBridge = messageBridge;
        this.objectMapper = new ObjectMapper();
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

        GameSession gameSession = new GameSession(player, session);
        sessions.put(session.getId(), gameSession);
        playerSessions.put(userId, gameSession);

        sendToSession(session, new WebSocketOutgoingPayload("playerPush", PlayerVO.from(player)));
        sendToSession(session, new WebSocketOutgoingPayload("roomPush",
                RoomVO.from(player.getCurrentRoom(),
                        RoomPlayerVO.fromList(getPlayersInRoom(player.getCurrentRoom(), player)))));
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
            return;
        }

        if ("command".equals(action)) {
            String cmd = payload.getData();
            Player player = gameSession.getPlayer();
            game.processCommand(player, cmd);
            playerPush(player);
            roomPush(player.getCurrentRoom());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        GameSession gameSession = sessions.remove(session.getId());
        if (gameSession != null) {
            gameSession.getPlayer().setOnline(false);
            playerSessions.remove(gameSession.getPlayer().getUserId());
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

        for (Player p : playersInRoom) {
            GameSession session = playerSessions.get(p.getUserId());
            if (session != null) {
                sendToSession(session.getWebSocketSession(),
                        new WebSocketOutgoingPayload("roomPush", RoomVO.from(room, playerVOs)));
            }
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

    public List<Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();
        for (GameSession session : sessions.values()) {
            Player p = session.getPlayer();
            if (p.isOnline()) {
                online.add(p);
            }
        }
        return online;
    }

    public List<Player> getPlayersInRoom(Room room, Player exclude) {
        List<Player> result = new ArrayList<>();
        for (GameSession session : sessions.values()) {
            Player p = session.getPlayer();
            if (p.isOnline() && p.getCurrentRoom() == room && p != exclude) {
                result.add(p);
            }
        }
        return result;
    }

    public void checkHeartbeats() {
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
