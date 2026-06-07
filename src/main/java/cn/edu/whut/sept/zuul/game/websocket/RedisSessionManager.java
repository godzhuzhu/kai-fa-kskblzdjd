package cn.edu.whut.sept.zuul.game.websocket;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Profile("prod")
public class RedisSessionManager {

    private static final String SESSION_PREFIX = "session:";
    private static final String ROOM_PREFIX = "room:";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisSessionManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void playerOnline(Player player) {
        String key = SESSION_PREFIX + player.getUserId();
        redisTemplate.opsForHash().put(key, "playerName", player.getPlayerName());
        redisTemplate.opsForHash().put(key, "online", "true");
        redisTemplate.opsForHash().put(key, "currentRoom", player.getCurrentRoom().getName());
        redisTemplate.expire(key, 90, TimeUnit.SECONDS);

        String roomKey = ROOM_PREFIX + player.getCurrentRoom().getName() + ":players";
        redisTemplate.opsForSet().add(roomKey, String.valueOf(player.getUserId()));
    }

    public void playerOffline(Player player) {
        String key = SESSION_PREFIX + player.getUserId();
        redisTemplate.opsForHash().put(key, "online", "false");
        redisTemplate.expire(key, 10, TimeUnit.SECONDS);

        String roomKey = ROOM_PREFIX + player.getCurrentRoom().getName() + ":players";
        redisTemplate.opsForSet().remove(roomKey, String.valueOf(player.getUserId()));
    }

    public void updateHeartbeat(Player player) {
        String key = SESSION_PREFIX + player.getUserId();
        redisTemplate.expire(key, 90, TimeUnit.SECONDS);
    }

    public List<Integer> getPlayersInRoom(Room room) {
        String roomKey = ROOM_PREFIX + room.getName() + ":players";
        Set<String> members = redisTemplate.opsForSet().members(roomKey);
        List<Integer> ids = new ArrayList<>();
        if (members != null) {
            for (String m : members) {
                String key = SESSION_PREFIX + m;
                String online = (String) redisTemplate.opsForHash().get(key, "online");
                if ("true".equals(online)) {
                    ids.add(Integer.parseInt(m));
                }
            }
        }
        return ids;
    }

    public boolean isOnline(int userId) {
        String key = SESSION_PREFIX + userId;
        String online = (String) redisTemplate.opsForHash().get(key, "online");
        return "true".equals(online);
    }
}
