package cn.edu.whut.sept.zuul.game.websocket;

import cn.edu.whut.sept.zuul.game.Player;
import org.springframework.web.socket.WebSocketSession;

public class GameSession {

    private final Player player;
    private final WebSocketSession webSocketSession;
    private long lastHeartbeat;

    public GameSession(Player player, WebSocketSession webSocketSession) {
        this.player = player;
        this.webSocketSession = webSocketSession;
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }
}
