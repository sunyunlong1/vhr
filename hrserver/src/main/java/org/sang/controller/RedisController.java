package org.sang.controller;

import org.sang.service.RedisMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    RedisMsgService redisMsgService;

    @RequestMapping("/sendMsg")
    public String sendMsg(String id,String msg,String isNow){
        return redisMsgService.sendMsg(id,msg,isNow);
    }
}
