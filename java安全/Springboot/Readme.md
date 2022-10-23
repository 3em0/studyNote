# Springboot

> 参考: https://github.com/LandGrey/SpringBootVulExploit
>
> 待做：使用tabby扫描springboot在各个rce其他可以利用的类(起码出3个题！)
>

## 一 信息泄露

> /env /actuator/env
>
> heapdump

## 二 RCE

### 0x01 SpEL 模板注入

总结就是: springboot的`ErrorMvcAutoConfiguration`的对于模板渲染的时候，调用了`PropertyPlaceholderHelper#parseStringValue`，他在处理模板渲染语法的时候，存在递归调用。

![image-20221022202340482](https://img.dem0dem0.top//images/image-20221022202340482.png)

递归。

![image-20221022202804356](https://img.dem0dem0.top//images/image-20221022202804356.png)

注意看下面的图片中的解析流程。

![image-20221022202958112](https://img.dem0dem0.top//images/image-20221022202958112.png)

按照正常处理来说，到这里，流程就应该结束了，后面的内容是不需要继续迭代下去的，直接输出即可。但是却出现了下面这种情况。

![image-20221022203103920](https://img.dem0dem0.top//images/image-20221022203103920.png)

修复的过程就是新建了一个类，不支持递归了~。

### 0x02 SnakeYAML 

> post /env 可以修改springboot的环境变量，导致`spring.cloud.bootstrap.location`可以被恶意用户修改，然后`yaml`反序列化利用成功。

### 0x03 eureka xstream  

> 核心原理同上，区别在于：这个是xstream的反序列化。

### 0x04 jolokia logback JNDI 

> 利用流程： 接口利用`jolokia`调用`ch.qos.logback.classic.jmx.JMXConfigurator`类`reloadByURL`方法从远程加载`xml`文件=>`xxe`=>`JNDI`.

### 0x05 jolokia Realm JNDI RCE

> `type=MBeanFactory#createJNDIRealm`
>
> 1. 设置 connectionURL 地址为 RMI Service URL
> 2. 设置 contextFactory 为 RegistryContextFactory
>
> https://ricterz.me/posts/2019-03-06-yet-another-way-to-exploit-spring-boot-actuators-via-jolokia.txt

### 0x06 restart h2 database query RCE

> /env POST
>
> /restart POST
>
> `spring.datasource.hikari.connection-test-query`: 对应 HikariCP数据库连接池`connectionTestQuery`配置，在建立新的数据库链接前会先执行的命令

### 0x07 h2 database console JNDI

> `http://127.0.0.1:8080/h2-console`可以直接访问。
>
> 限制： 开启 -webAllowOthers 选项，支持外网访问 开启 -ifNotExists 选项，支持创建数据库
>
> 不出网
>
> language=en&setting=Generic+H2+%28Embedded%29&name=Generic+H2+%28Embedded%29&driver=org.h2.Driver&url=jdbc%3ah2%3amem%3atest%3bMODE%3dMSSQLServer%3binit%3dCREATE+TRIGGER+shell3+BEFORE+SELECT+ON+INFORMATION_SCHEMA.TABLES+AS+$$//javascript%0a%0ajava.lang.Runtime.getRuntime().exec('cmd+/c+calc.exe')$$&user=sa&password=

### 0x08 mysql jdbc deserialization 

> JDBC的正确配置：`statementInterceptors=com.mysql.jdbc.interceptors.ServerStatusDiffInterceptor&autoDeserialize=true`,会导致java反序列化的存在~
>
> 如何开发自己的fake-jdbcserver: https://su18.org/post/jdbc-connection-url-attack/

### 0x09 restart logging.config logback JNDI 

> 同0x03 jolokia logback JNDI 

### 0x0A restart logging.config groovy

> `logback-classic` 组件的 `ch.qos.logback.classic.util.ContextInitializer.java` 代码文件逻辑中会判断 url 是否以 `groovy` 结尾

### 0x0B restart spring.main.sources groovy

> `spring-boot` 组件中的 `org.springframework.boot.BeanDefinitionLoader.java` 文件代码逻辑中会判断 url 是否以 `.groovy` 结尾

### 0x0C restart spring.datasource.data h2 database

>   `spring-boot-autoconfigure` 组件中的 `org.springframework.boot.autoconfigure.jdbc.DataSourceInitializer.java` 文件代码逻辑中会使用 `runScripts` 方法执行请求 URL 内容中的 h2 database sql 代码，造成 RCE 漏洞
