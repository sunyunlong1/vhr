package org.sang.util;

import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 加锁
     * @param key
     * @param value 当前事件+超时事件
     * @return
     */
    public boolean lock(String key,String value){
        //加锁成功
        if (redisTemplate.opsForValue().setIfAbsent(key,value)){
            return true;
        }
        //假如currentValue=A先占用了锁  其他两个线程的value都是B,保证其中一个线程拿到锁
        String currentValue = redisTemplate.opsForValue().get(key);
        //锁过期  防止出现死锁
        if (!StringUtils.isEmpty(currentValue) &&
                Long.parseLong(currentValue) < System.currentTimeMillis()){
            //获取上一步锁的时间
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (!StringUtils.isEmpty(oldValue) &&
                    oldValue.equals(currentValue)){
                return true;
            }
        }
        return false;
    }

    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unlock(String key,String value){
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) &&
                    currentValue.equals(value)){
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        }catch (Exception e){
            log.error("【redis分布式锁】 解锁异常，{}",e);
        }
    }
}