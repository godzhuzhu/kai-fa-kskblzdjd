package cn.edu.whut.sept.zuul.game.websocket;

import cn.edu.whut.sept.zuul.game.Room;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("prod")
public class RedisPubSubService implements MessageListener {

    private static final String CHANNEL_PREFIX = "channel:room:";

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer container;
    private final ObjectMapper objectMapper;

    private final Map<String, WebSocketOutgoingPayload> lastPayloads = new ConcurrentHashMap<>();

    private GameWebSocketHandler handler;

    public RedisPubSubService(RedisTemplate<String, String> redisTemplate,
                              RedisMessageListenerContainer container) {
        this.redisTemplate = redisTemplate;
        this.container = container;
        this.objectMapper = new ObjectMapper();
    }

    public void setHandler(GameWebSocketHandler handler) {
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        container.addMessageListener(this, ChannelTopic.of(CHANNEL_PREFIX + "*"));
    }

    public void publish(Room room, WebSocketOutgoingPayload payload) {
        String channel = CHANNEL_PREFIX + room.getName();
        String key = channel + ":" + payload.getType();
        String body = lastPayloads.get(key) == null ? "" : "";
        try {
            redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(payload));
        } catch (IOException e) {
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (handler == null) {
            return;
        }
        try {
            WebSocketOutgoingPayload payload = objectMapper.readValue(message.getBody(), WebSocketOutgoingPayload.class);
            handler.broadcastLocal(payload);
        } catch (Exception ignored) {
        }
    }
}
