package com.dem0.Application.controller;

import com.dem0.Application.model.R;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

@RestController
public class MainsiteErrorController implements ErrorController {
    private ErrorAttributes errorAttributes;
    private final String ERROR_PATH ="/error";
    @Autowired
    public MainsiteErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes=errorAttributes;
    }
    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
    @RequestMapping(value = ERROR_PATH, produces = {"text/html"})
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        int code = response.getStatus();
        ServletWebRequest requestAttributes = new ServletWebRequest(request);
        Map<String, Object> attr=this.errorAttributes.getErrorAttributes(requestAttributes, false);
        String path = URLDecoder.decode((String) attr.get("path"), "UTF-8");
        path = waf(path) ? path:"Hacker go out!";
        if (404 == code) {
            ModelAndView modelAndView = new ModelAndView("error/404");
            model.addAttribute("path", makePath(path));
            return modelAndView;
        } else {
            return new ModelAndView("error/500");
        }
    }

    @RequestMapping(value =ERROR_PATH)
    public R handleError(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        int code = response.getStatus();
        ServletWebRequest requestAttributes = new ServletWebRequest(request);
        Map<String, Object> attr=this.errorAttributes.getErrorAttributes(requestAttributes, false);
        String path = URLDecoder.decode((String) attr.get("path"), "UTF-8");
        path = waf(path) ? path:"Hacker go out!";
        if (404 == code) {
            return R.error(404, "wuwu 人家就是找不到麻~~"+makePath(path));
        } else {
            return R.error(500, "wuwu 人家就是不会写代码");
        }
    }

    public boolean waf(String path){
        String[] blacklist = new String[]{"#set","Runtime","getMethod","getClass","flag","cat","getConstructor","#evalue","java","get","#parse","#{parse}"};
        for (String black : blacklist) {
            if(path.contains(black)){
                return  false;
            }
        }
        return  true;
    }

    public String makePath(String path){
        Velocity.init();
        VelocityContext context = new VelocityContext();
        StringWriter swOut = new StringWriter();
        Velocity.evaluate(context, swOut, "test", path);
        return  swOut.toString();
    }

}
