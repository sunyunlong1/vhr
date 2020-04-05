//package org.sang.util;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@Slf4j
//public class TaskRedis {
//
//    @Autowired
//    private RedisUtil redisUtil;
//
//    @Scheduled(cron="0 */1 * * * ?")
//    public void sendMsg() {
//        boolean b=redisUtil.tryLock(Constant.KEY_LOCK, "1", 5);
//        if (!b) {
//            log.info("抢锁失败");
//            return;
//        }
//        try {
//            Long zsetSize=redisUtil.getZsetSize(Constant.KEY_ZSET);
//            if (zsetSize == 0L){
//                return ;
//            }
//            CountDownLatch countDownLatch=new CountDownLatch(500);
//            for (int i=0; i < zsetSize; i++) {
//                String uuid=redisUtil.getFirstData(Constant.KEY_ZSET);
//                redisUtil.sendMsg(countDownLatch, uuid);
//            }
//            try {
//                countDownLatch.await(50L, TimeUnit.SECONDS);
//            } catch (InterruptedException e) {
//                log.info("e:{}", e);
//            }
//        } catch (Exception e) {
//            log.info("e:{}", e);
//        } finally {
//            redisUtil.delLock(Constant.KEY_LOCK);
//        }
//    }
//
//
//}
