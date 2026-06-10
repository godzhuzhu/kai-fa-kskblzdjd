package cn.edu.whut.sept.zuul.game.websocket;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.message.GameMessageBridge;
import cn.edu.whut.sept.zuul.game.user.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    private final Game game;
    private final JwtUtil jwtUtil;
    private final GameMessageBridge messageBridge;

    @Autowired(required = false)
    private RedisSessionManager redisSessionManager;

    @Autowired(required = false)
    private RedisPubSubService redisPubSubService;

    public WebSocketConfig(Game game, JwtUtil jwtUtil, GameMessageBridge messageBridge) {
        this.game = game;
        this.jwtUtil = jwtUtil;
        this.messageBridge = messageBridge;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler(), "/game/websocket")
                .setAllowedOrigins("*");
    }

    @Bean
    public GameWebSocketHandler gameWebSocketHandler() {
        GameWebSocketHandler handler = new GameWebSocketHandler(game, jwtUtil, messageBridge,
                redisSessionManager, redisPubSubService);
        game.setWebSocketHandler(handler);
        if (redisPubSubService != null) {
            redisPubSubService.setHandler(handler);
        }
        return handler;
    }

    @Scheduled(fixedRate = 30000)
    public void checkHeartbeats() {
        game.getWebSocketHandler().checkHeartbeats();
    }

    @Scheduled(fixedRate = 45000)
    public void respawnItems() {
        game.respawnItems();
    }
}