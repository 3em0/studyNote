package com.example.thymeleafssti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Demo {
    Logger log =  LoggerFactory.getLogger(com.example.thymeleafssti.ThymeleafSstiApplication.class);

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "hello world!");
        return "index";
    }


    @GetMapping("/admin")
    public String path(@RequestParam String language) {
        return "language/" + language + "/admin";
    }

    @GetMapping("/test/en")
    public String testPage(Model model) {
        model.addAttribute("message", "hello en!");
        return "language/en/admin";
    }
    @GetMapping("/test/cn")
    public String testPages(Model model) {
        model.addAttribute("message", "hello cn!");
        return "language/cn/admin";
    }

    @GetMapping("/page")
    public String path(@RequestParam String exp, Model model) {
        model.addAttribute("exp", exp);
        return "exp";
    }

    @GetMapping("/getContent")
    public String getPage(String page) {
        return "/test::" + page;
    }

    @GetMapping("/admin/{page}")
    public String getAdminPage(@PathVariable String page) {
        log.info("Pages: " + page);
        return "language/cn/" + page;
    }

    @GetMapping("/con/{page}")
    public void getAdminPages(@PathVariable String page) {
        log.info("Pages: " + page);
    }

    @GetMapping("/home/{page}")
    public String getHome(@PathVariable String page) {
        log.info("Pages: " + page);
        return "home/" + page;
    }


}