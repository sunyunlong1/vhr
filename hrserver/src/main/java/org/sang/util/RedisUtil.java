package org.sang.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.expression.Lists;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * redis工具类
 */
@Component
@Slf4j
public class RedisUtil {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public void insertToRank(String id, String msg, String isNow) {
        double score=this.getScore(isNow);
        String uuid=UUID.randomUUID().toString().replaceAll("-", "");
        HashOperations<String, Object, Object> opsForHash=
                stringRedisTemplate.opsForHash();
        Map<String, String> map=new HashMap<>();
        map.put("id", id);
        map.put("msg", msg);
        opsForHash.putAll(Constant.KEY_HASH + uuid, map);
        this.insertZset(uuid, score);
    }

    /**
     * 插入zset，按照score排序
     *
     * @param key
     * @param score
     */
    public void insertZset(String key, double score) {
        ZSetOperations<String, String> opsForZSet=
                stringRedisTemplate.opsForZSet();
        opsForZSet.add(Constant.KEY_ZSET, key, score);
    }

    /**
     * 获取当前时间
     *
     * @param isNow is时表示实时消息，放入zset前面
     * @return
     */
    private double getScore(String isNow) {
        Date nowTime=new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        String now=simpleDateFormat.format(nowTime);
        if ("is".equals(isNow)) {
            return Double.parseDouble(now.substring(0, 12));
        }
        return Double.parseDouble(now);
    }

    /**
     * 抢锁
     *
     * @param key
     * @param value
     * @param timeOut
     * @return
     */
    public boolean tryLock(String key, String value, long timeOut) {
        Boolean isSuccess=stringRedisTemplate.opsForValue().setIfAbsent(key, value);
        if (isSuccess) {
            stringRedisTemplate.expire(key, timeOut, TimeUnit.MINUTES);
        }
        return isSuccess;
    }

    /**
     * 释放锁
     * @param key
     */
    public void delLock(String key){
        stringRedisTemplate.delete(key);
    }
    /**
     * 获取zset第一条数据
     *
     * @param key
     * @return
     */
    public String getFirstData(String key) {
        ZSetOperations<String, String> opsForZSet=stringRedisTemplate.opsForZSet();
        Set<String> range=opsForZSet.range(key, 0, 1);
        if (!CollectionUtils.isEmpty(range)) {
            String uuid = range.iterator().next();
            opsForZSet.remove(key,uuid);
            return uuid;
        }
        return null;
    }

    /**
     * 查询当前Zset多少个数据
     * @param key
     * @return
     */
    public Long getZsetSize(String key){
        return stringRedisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取hash数据
     * @param key
     * @return
     */
    public Map<String,Object> getHashData(String key){
        HashOperations<String, Object, Object> opsForHash=stringRedisTemplate.opsForHash();
        Object id=opsForHash.get(key, "id");
        Object msg=opsForHash.get(key, "msg");
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("msg",msg);
        return map;
    }

    @Async
    public void sendMsg(CountDownLatch countDownLatch,String uuid){
        try{
            Map<String, Object> hashData=this.getHashData(Constant.KEY_HASH + uuid);
            log.info("开始发送"+hashData.get("id")+"消息: "+hashData.get("msg"));
        }catch (Exception e){
            log.info("e:{}",e);
        }finally {
            countDownLatch.countDown();
        }
    }

    /**
     * 插入
     * @param key
     * @param num
     */
    public void insert(String key,String num){
        ValueOperations<String, String> opsForValue
                =stringRedisTemplate.opsForValue();
        opsForValue.set(key,num);
    }

    /**
     * 库存减1
     * @param key
     */
    public void sub(String key){
        stringRedisTemplate.opsForValue().increment(key, -1);
        log.info("当前剩余库存"+stringRedisTemplate.opsForValue().get(key));
    }
}
