package org.sang.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.sang.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisMsgService {

    /** 超时时间 */
    private static final int TIMEOUT = 10000;

    private static final String KEY = "redisson-key";

    private static final String KEY_NUM = "key-num";

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedissonClient redissonClient;

    public String sendMsg(String id, String msg, String isNow) {
        int count=0;
        redisUtil.insertToRank(id, msg, isNow);
        count++;
        return count > 0 ? id + "发送成功" : id + "发送失败";
    }

    public String insert(){
        redisUtil.insert(KEY_NUM,"10000");
        return "插入成功";
    }

    /**
     * @Description: 秒杀商品接口
     * @param
     * @return JsonObject
     * @exception
     * @author mazhq
     * @date 2018/11/18 13:46
     */
    public String kill() {
        long time = System.currentTimeMillis() + TIMEOUT;
        RLock lock=
                redissonClient.getLock(KEY);
        try{
            Thread a = new Thread(){
                @Override
                public void run() {
                    Thread.currentThread().setName("线程1");
                    try {
                        boolean isSuccess=lock.tryLock(0,1, TimeUnit.SECONDS);
                        if (isSuccess){
                            log.info(Thread.currentThread().getName()+"获得锁，开始秒杀");
                            redisUtil.sub(KEY_NUM);
                        }else{
                            log.info(Thread.currentThread().getName()+"强锁失败");
                        }
                    } catch (InterruptedException e) {
                        log.info(Thread.currentThread().getName()+"强锁异常");
                    }
                }
            };
            Thread b = new Thread(){
                @Override
                public void run() {
                    Thread.currentThread().setName("线程2");
                    try {
                        boolean isSuccess=lock.tryLock(0,1, TimeUnit.SECONDS);
                        if (isSuccess){
                            log.info(Thread.currentThread().getName()+"获得锁，开始秒杀");
                            redisUtil.sub(KEY_NUM);
                        }else{
                            log.info(Thread.currentThread().getName()+"强锁失败");
                        }
                    } catch (InterruptedException e) {
                        log.info(Thread.currentThread().getName()+"强锁异常");
                    }
                }
            };
            Thread c = new Thread(){
                @Override
                public void run() {
                    Thread.currentThread().setName("线程3");
                    try {
                        boolean isSuccess=lock.tryLock(0,1, TimeUnit.SECONDS);
                        if (isSuccess){
                            log.info(Thread.currentThread().getName()+"获得锁，开始秒杀");
                            redisUtil.sub(KEY_NUM);
                        }else{
                            log.info(Thread.currentThread().getName()+"强锁失败");
                        }
                    } catch (InterruptedException e) {
                        log.info(Thread.currentThread().getName()+"强锁异常");
                    }
                }
            };
            a.start();
            b.start();
            c.start();
        }catch (Exception e){
            log.info("--------");
        }finally {
            lock.unlock();
        }

        return "111";
    }

}
