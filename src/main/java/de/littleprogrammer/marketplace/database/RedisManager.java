package de.littleprogrammer.marketplace.database;

import de.littleprogrammer.marketplace.files.DatabaseFile;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisManager {
    private String host = new DatabaseFile().getString("redis.host");
    private int port = new DatabaseFile().getInt("redis.port");
    private static RedisManager instance;
    private JedisPool jedisPool;

    private RedisManager() {
        jedisPool = new JedisPool(host, port);
    }

    public static RedisManager getInstance() {
        if (instance == null) {
            instance = new RedisManager();
        }
        return instance;
    }

    public void set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public void update(String key, String newValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, newValue);
        }
    }

    public void remove(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }
}
