# Shiro 权限绕过 yyds!

环境: > https://github.com/KpLi0rn/ShiroVulnEnv

博客 》 https://www.yuque.com/tianxiadamutou/zcfd4v/emcdeq

```
?  匹配一个字符
*  匹配一个或多个字符
** 匹配一个或多个目录
```

payload

```java
/index/..;/admin/index
/login/..;/admin/index
/index/..;/admin/
/login/..;/admin/
/login/..;/json
/;/json
/actuator/;/env/
/admin/;/xxx
/admin/%3b/xxx
/admin/%252fxxx
/admin/%3Bxx
/admin/%20
```



## CVE-2020-11989

### 利用条件

- Apache Shiro <= 1.5.2
- Spring 框架中只使用 Shiro 鉴权

- 需要后端特定的格式才可进行触发

- - 即：Shiro权限配置必须为 /xxxx/* ，同时后端逻辑必须是 /xxx/{variable} 且 variable 的类型必须是 String

### Dem0

```java
map.put("/doLogin", "anon");
map.put("/unauth","user");
map.put("/admin/*", "authc");

<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-web</artifactId>
    <version>1.5.2</version>
</dependency>
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-spring</artifactId>
    <version>1.5.2</version>
</dependency>
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-core</artifactId>
    <version>1.2.4</version>
</dependency>
```

![image-20220112094255840](https://img.dem0dem0.top/images/image-20220112094255840.png)

### 利用

```
/admin/Hello%252fBKpLi0rn
```

在测试的时候发现，好像就只有`1.5.2`这一版本可以

### 测试

其实原理很好猜测，就是shiro进行了两次url解码，spring只进行了一次。https://www.anquanke.com/post/id/218270#h3-7

处理流程，我们随便在哪里打一个断点就知道是先处理shiro再spring。

- shiro的处理流程:

  ![image-20220112160318171](https://img.dem0dem0.top/images/image-20220112160318171.png)

  前面是正常的filter分发，我并没有看明白。从这里开始分析。[`getAlreadyFilteredAttributeName()`](https://shiro.apache.org/static/1.5.2/apidocs/org/apache/shiro/web/servlet/OncePerRequestFilter.html#getAlreadyFilteredAttributeName())标记一下，然后判断该请求中是否已经有该标记了，来判断是否已经调用过了。没调用过就

  ![image-20220112160533137](https://img.dem0dem0.top/images/image-20220112160533137.png)

  进入到这个函数中。这是调用的`AbstractShiroFilter#doFilterInternal`然后处理请求和返回值

- shiro权限绕过流程:

![image-20220112154333745](https://img.dem0dem0.top/images/image-20220112154333745.png)

这里shiro的`getRequestUri`在拿到的是 `Hello%2fBLi0rn`因为中间件已经解码了一次，这里uri再解码就变成了图中的样子。从而绕过了。将解码之后的 uri 进行赋值，然后会判断其中是否含有分号，如果有的话就截取分号前的内容进行返回

![image-20220112154521132](https://img.dem0dem0.top/images/image-20220112154521132.png)

![image-20220112154745965](https://img.dem0dem0.top/images/image-20220112154745965.png)

很简单的看出是不满足的，达到绕过的效果。然后就开始返回到前几部

![image-20220112154955691](https://img.dem0dem0.top/images/image-20220112154955691.png)

这个位置，沃恩现在`resolved`发现是返回的null，因为我们不满足他的格式，所以还是原理的filter。(这里就是servlet的那个filter了)

- spring

  原则是`Shiro`会对`Servlet`容器的`FilterChain`进行代理，也就是正常情况下应该返回的`ProxiedFilterChain`，即先走`Shiro`自己的`Filter`体系，然后才会委托给`Servlet`容器的`FilterChain`进行`Servlet`容器级别的`Filter`链执行。

  ![image-20220112162042561](https://img.dem0dem0.top/images/image-20220112162042561.png)

使`DispatcherServlet#getHandler`来获取可以处理这个请求的handler。(Return the HandlerExecutionChain for this request.)

最后跟进去，关键点落在了 `getUrlPathHelper().getLookupPathForRequest(request)` ，他们会根据url中的参数来取得使用pattern的handler。 `lookupHandlerMethod`spring的映射关键就在这个函数了。

### 修复

新版本采用的是 取消掉shiro中的`getRequestUri`中的无脑解码函数，而是对于`%2f`不进行解析。

##  CVE-2020-1957

### 利用条件

- Apache Shiro <= 1.5.1
- Spring 框架中只使用 Shiro 鉴权

### 环境

```
<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.5.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
</parent>
```

```java
        //CVE-2020-1957
        map.put("/doLogin", "anon");
        map.put("/demo/**","anon");
        map.put("/unauth", "user");
        map.put("/admin/**","authc");
        map.put("/**", "authc");
```

然后需要一个不需要鉴权的路由

### 利用

```
/demo/..;/admin/index
```

### 测试

![image-20220112164249420](https://img.dem0dem0.top/images/image-20220112164249420.png)

这就是上一个处理解码的函数，到这里我们可以发现这里有一个阶段，也就是最后传入到后面匹配的uri应该就是`/demo/..`

![image-20220112164648231](https://img.dem0dem0.top/images/image-20220112164648231.png)

也就是这里进入shiro的filter了，但是我们知道`demo`并不需要

在spring中使用this.**getRequestUri**`removeSemicolonContent`删去了`;`,所以也就不存在后面其他的部分了。

![image-20220112170036279](https://img.dem0dem0.top/images/image-20220112170036279.png)

### 修复

![image-20220112164925941](https://img.dem0dem0.top/images/image-20220112164925941.png)

把这里改成了一个组合体`getContextPath() 、getServletPath() 、getPathInfo()` 

## CVE-2020-13933

### 利用条件

- Apache Shiro < 1.6.0
- Spring 框架中只使用 Shiro 鉴权

- 需要后端特定的格式才可进行触发

- - 即：Shiro权限配置必须为 /xxxx/* ，同时后端逻辑必须是 /xxx/{variable} 且 variable 的类型必须是 String

### 环境

1.5.3用11989的环境

### 测试

![image-20220112171924656](https://img.dem0dem0.top/images/image-20220112171924656.png)

shiro的好分割

![image-20220112171909768](https://img.dem0dem0.top/images/image-20220112171909768.png)

匹配规则

```
*  匹配一个或多个字符
```

**不会匹配/admin/** 绕过了.....

### 修复

增加一个对于url中的特殊字符报错的类，对于特殊字符直接报错.....

同时增加了 /** 的规则，来防止一些匹配不到的情况

## CVE-2020-17523

### 利用条件

- Apache Shiro < 1.7.1
- Spring 框架中只使用 Shiro 鉴权

- 需要后端特定的格式才可进行触发

- - 即：Shiro权限配置必须为 /xxxx/* ，同时后端逻辑必须是 /xxx/{variable} 且 variable 的类型必须是 String

### 环境

就用上面的

### 利用

```
/admin/%20
```

### 测试

![image-20220112172614906](https://img.dem0dem0.top/images/image-20220112172614906.png)

![image-20220112172642881](https://img.dem0dem0.top/images/image-20220112172642881.png)

自动trim了一下，所以就绕过了。

## CVE-2021-41303

利用 http://bypass/aaa/index,,,好像就是下面这个不会被拦截，他在获取handler的时候有顺序，我们在调试的时候也可以发现这一点

![image-20220112172818900](https://img.dem0dem0.top/images/image-20220112172818900.png)

## all

其实就是shiro和spring对于一些特殊字符的处理不统一导致我们可以绕过，这种思路在其他组件中也存在的，希望有一天挖一个。
