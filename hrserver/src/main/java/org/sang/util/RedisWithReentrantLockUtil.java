package org.sang.util;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName RedisWithReentrantLock
 * @Decription redis可重入锁
 * @Auther Sunyunlong
 * @Date 2019-09-07 11:33
 * @Version 1.0
 **/
public class RedisWithReentrantLockUtil {

    private static ThreadLocal<Map<String, Integer>> lockers = new ThreadLocal<>();

    private Jedis jedis;

    public RedisWithReentrantLockUtil(Jedis jedis) {
        this.jedis = jedis;
    }

    private boolean _lock(String key) {
        return jedis.set(key, "", "nx", "ex", 5L) != null;
    }

    private void _unlock(String key) {
        jedis.del(key);
    }

    private Map<String, Integer> currentLockers() {
        Map<String, Integer> refs = lockers.get();
        if (refs != null) {
            return refs;
        }
        lockers.set(new HashMap<>());
        return lockers.get();
    }

    public boolean lock(String key) {
        Map<String, Integer> refs = currentLockers();
        Integer refCnt = refs.get(key);
        if (refCnt != null) {
            refs.put(key, refCnt + 1);
            return true;
        }
        boolean ok = this._lock(key);
        if (!ok) {
            return false;
        }
        refs.put(key, 1);
        return true;
    }

    public boolean unlock(String key) {
        Map<String, Integer> refs = currentLockers();
        Integer refCnt = refs.get(key);
        if (refCnt == null) {
            return false;
        }
        refCnt -= 1;
        if (refCnt > 0) {
            refs.put(key, refCnt);
        } else {
            refs.remove(key);
            this._unlock(key);
        }
        return true;
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1");
        RedisWithReentrantLockUtil redis = new RedisWithReentrantLockUtil(jedis);
        System.out.println(redis.lock("codehole"));
        System.out.println(redis.lock("codeholh"));
        Map<String, Integer> stringIntegerMap = lockers.get();
        Integer integer = stringIntegerMap.get("codehole");
        Integer integer1 = stringIntegerMap.get("codeholh");
        System.out.println(integer);
        System.out.println(integer1);
        System.out.println(redis.unlock("codehole"));
        System.out.println(redis.unlock("codeholh"));
    }

}
