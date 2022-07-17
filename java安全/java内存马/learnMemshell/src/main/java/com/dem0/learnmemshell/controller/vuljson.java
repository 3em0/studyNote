package com.dem0.learnmemshell.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class vuljson {
    @RequestMapping("/json")
    @ResponseBody
    public String vulJson(@RequestBody String input){
        System.out.println(input);
        Object o = JSON.parseObject(input);
        return "Successed!";
    }
}
