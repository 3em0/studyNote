package com.dem0.freemarker.controller;

import com.liferay.portal.kernel.template.StringTemplateResource;
import com.liferay.portal.kernel.template.TemplateManager;
import com.liferay.portal.kernel.template.TemplateManagerUtil;
import com.liferay.portal.template.freemarker.configuration.FreeMarkerEngineConfiguration;
import com.liferay.portal.template.freemarker.internal.FreeMarkerManager;
import com.liferay.portal.template.freemarker.internal.FreeMarkerTemplateContextHelper;
import com.liferay.portal.template.freemarker.internal.FreeMarkerTemplateResourceLoader;
import com.liferay.portal.template.freemarker.internal.LiferayTemplateClassResolver;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class TestController {
    @Autowired
    private  Configuration con;

    @RequestMapping(value = "/freemarker")
    public void freemarker(@RequestParam(value="username",defaultValue="World") String username, HttpServletRequest httpserver, HttpServletResponse response) {
        try{
            String data = "1ooooooooooooooooooo~";
            String templateContent = "<html><body>Hello " + username + " ${data}</body></html>";

            String langType = "ftl";
            FreeMarkerManager templateManager = new FreeMarkerManager();
            templateManager.setTemplateContextHelper(new FreeMarkerTemplateContextHelper());
            templateManager.setTemplateClassResolver(new LiferayTemplateClassResolver());
            templateManager.setTemplateResourceLoader(new FreeMarkerTemplateResourceLoader());
            StringTemplateResource templateResource = new StringTemplateResource("classic_WAR_classictheme_STANDARD_pop_up",templateContent);
            com.liferay.portal.kernel.template.Template template = templateManager.getTemplate(templateResource, false);
            StringWriter writer = new StringWriter();;
            template.prepare(httpserver);
            template.processTemplate(writer);
            response.getWriter().println(writer.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/hello")
    public String hello(Model model,@RequestParam(value="name", required=false, defaultValue="World") String name) {
        model.addAttribute("name", name);
        model.addAttribute("age", "30");
        return "hello";
    }

    private String createHtmlFromString(String templateContent, String data) throws IOException, TemplateException {
        Configuration cfg = new Configuration();
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("myTemplate",templateContent);
        cfg.setTemplateLoader(stringLoader);
        Template template = cfg.getTemplate("myTemplate","utf-8");
        Map root = new HashMap();
        root.put("data",data);
//        cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
        StringWriter writer = new StringWriter();
        template.process(root,writer);
        return writer.toString();
    }
    @RequestMapping(value = "/template", method =  RequestMethod.POST)
    public String template(@RequestBody Map<String,String> templates) throws IOException {
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        for(String templateKey : templates.keySet()){
            stringLoader.putTemplate(templateKey, templates.get(templateKey));
        }
        con.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{stringLoader,
                con.getTemplateLoader()}));
        return "index";
    }
}
