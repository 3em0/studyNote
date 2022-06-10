# Java模板注入

大概看了一下，有这么几种模板

 - FreeMarker模板

   ```
   <ul>
   <#list .data_model?keys as key>
   <li>${key}</li>
   </#list>
   </ul>
   ${.data_model.keySet()}
   ```

 - Thymeleaf模板

 - jsp模板

 - Velocity模板

   ```
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

> ● java.lang.Class.getClassLoader()
>
> ● java.lang.Thread.getCurrentClassLoader()
>
> ● java.lang.ProtectionDomain.getClassLoader()
>
> ● javax.servlet.ServletContext.getClassLoader()
>
> ● org.osgi.framework.wiring.BundleWiring.getClassLoader()
>
> ● org.springframework.context.ApplicationContext.getClassLoader()

参考资料

> 1.(包括了大部分的模板注入)https://cloud.tencent.com/developer/article/1771497