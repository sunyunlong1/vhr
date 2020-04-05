package org.sang.service;

import org.sang.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisMsgService {

    @Autowired
    private RedisUtil redisUtil;

    public String sendMsg(String id, String msg, String isNow) {
        int count=0;
        redisUtil.insertToRank(id, msg, isNow);
        count++;
        return count > 0 ? id + "发送成功" : id + "发送失败";
    }
}
