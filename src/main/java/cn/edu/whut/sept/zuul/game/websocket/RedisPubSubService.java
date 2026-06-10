package cn.edu.whut.sept.zuul.game.websocket;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
@Profile("prod")
public class RedisPubSubService implements MessageListener {

    private static final String CHANNEL_PREFIX = "channel:room:";

    private final StringRedisTemplate redis;
    private final RedisMessageListenerContainer container;
    private GameWebSocketHandler handler;

    public RedisPubSubService(StringRedisTemplate redis, RedisMessageListenerContainer container) {
        this.redis = redis;
        this.container = container;
    }

    @PostConstruct
    public void init() {
        container.addMessageListener(this, new PatternTopic(CHANNEL_PREFIX + "*"));
    }

    @PreDestroy
    public void destroy() {
        container.removeMessageListener(this);
    }

    public void setHandler(GameWebSocketHandler handler) {
        this.handler = handler;
    }

    public void publish(String roomName, String jsonPayload) {
        redis.convertAndSend(CHANNEL_PREFIX + roomName, jsonPayload);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (handler != null) {
            String channel = new String(message.getChannel());
            String roomName = channel.substring(CHANNEL_PREFIX.length());
            handler.handlePubSubMessage(roomName, new String(message.getBody()));
        }
    }
}