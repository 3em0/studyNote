package com.dem0.Application.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RestController
public class MainController {
    @RequestMapping("/")
    public ModelAndView vlun(Model model){
        ModelAndView modelAndView = new ModelAndView("hello");
        return modelAndView;
    }
}
