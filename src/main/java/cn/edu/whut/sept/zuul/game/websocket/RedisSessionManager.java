package cn.edu.whut.sept.zuul.game.websocket;

import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.Room;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Profile("prod")
public class RedisSessionManager {

    private static final String SESSION_PREFIX = "session:";
    private static final String ROOM_PLAYERS_PREFIX = "room:";

    private final StringRedisTemplate redis;

    public RedisSessionManager(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void playerOnline(Player player) {
        String key = SESSION_PREFIX + player.getUserId();
        redis.opsForHash().put(key, "online", "true");
        redis.opsForHash().put(key, "lastHeartbeat", String.valueOf(System.currentTimeMillis()));
        redis.opsForHash().put(key, "currentRoom", player.getCurrentRoom().getName());
        redis.expire(key, 120, TimeUnit.SECONDS);
        redis.opsForSet().add(ROOM_PLAYERS_PREFIX + player.getCurrentRoom().getName(),
                String.valueOf(player.getUserId()));
    }

    public void playerOffline(Player player) {
        String key = SESSION_PREFIX + player.getUserId();
        redis.delete(key);
        redis.opsForSet().remove(ROOM_PLAYERS_PREFIX + player.getCurrentRoom().getName(),
                String.valueOf(player.getUserId()));
    }

    public List<Integer> getPlayersInRoom(Room room) {
        Set<String> members = redis.opsForSet().members(ROOM_PLAYERS_PREFIX + room.getName());
        List<Integer> result = new ArrayList<>();
        if (members != null) {
            for (String id : members) {
                result.add(Integer.parseInt(id));
            }
        }
        return result;
    }

    public List<Integer> getOnlinePlayerIds() {
        List<Integer> result = new ArrayList<>();
        Set<String> keys = redis.keys(SESSION_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                String online = (String) redis.opsForHash().get(key, "online");
                if ("true".equals(online)) {
                    String id = key.substring(SESSION_PREFIX.length());
                    result.add(Integer.parseInt(id));
                }
            }
        }
        return result;
    }

    public void updateHeartbeat(Player player) {
        String key = SESSION_PREFIX + player.getUserId();
        redis.opsForHash().put(key, "lastHeartbeat", String.valueOf(System.currentTimeMillis()));
        redis.expire(key, 120, TimeUnit.SECONDS);
    }

    public long checkHeartbeats(long timeout) {
        long removed = 0;
        Set<String> keys = redis.keys(SESSION_PREFIX + "*");
        if (keys != null) {
            long now = System.currentTimeMillis();
            for (String key : keys) {
                String hb = (String) redis.opsForHash().get(key, "lastHeartbeat");
                if (hb != null && now - Long.parseLong(hb) > timeout) {
                    String room = (String) redis.opsForHash().get(key, "currentRoom");
                    String id = key.substring(SESSION_PREFIX.length());
                    if (room != null) {
                        redis.opsForSet().remove(ROOM_PLAYERS_PREFIX + room, id);
                    }
                    redis.delete(key);
                    removed++;
                }
            }
        }
        return removed;
    }
}