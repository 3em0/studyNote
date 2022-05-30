# Shell

>好文: https://xz.aliyun.com/t/7798
>
>仓库: https://github.com/anti-genemits/JSP-Webshells
>
>javashell: https://github.com/Firebasky/Java/tree/main/shell

- [ ] [jspshell](https://xz.aliyun.com/t/7798)

先学习心心的javashell，因为jsp的环境搭建还是有点懒......

# tips

- 编码

jsp的shell直接编码绕过哦 这个是utf-16

```java
'''<% out.print("23333"); %>'''.encode("utf-16")
```

参考链接:https://www.anquanke.com/post/id/210630#h2-2

- 空格问题

  java在对于传入的string参数的时候会对其中的空格和其他符号进行特殊处理导致命令执行的语意发生改变。

  编码和传入数组可以解决
  
- 发现一个宝藏网站

  可以生成exec的payload：https://www.jackson-t.ca/runtime-exec-payloads.html