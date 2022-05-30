---
title: SSI
date: 2020-10-30 08:50:48
tags: CTF
---

# SSI注入

## 一、什么是SSI

SSI 注入全称Server-Side Includes Injection，即服务端包含注入。SSI 是类似于 CGI，用于动态页面的指令。SSI 注入允许远程在 Web 应用中注入脚本来执行代码。SSI是嵌入HTML页面中的指令，在页面被提供时由服务器进行运算，以对现有HTML页面增加动态生成的内容，而无须通过CGI程序提供其整个页面，或者使用其他动态技术。从技术角度上来说，SSI就是在HTML文件中，可以通过注释行调用的命令或指针，即允许通过在HTML页面注入脚本或远程执行任意代码。

类似于SSTI注入的感觉，都是基于动态页面的实现功能

在Nginx中开启SSI

```
ssi on;
ssi_silent_errors off;
ssi_types text/shtml;
```

## 二、SSI语法

首先要先说一下这个SHTML文件，因为在shtml文件经常使用ssi指令来引入其它的html文件。

```
①显示服务器端环境变量<#echo>

本文档名称：

<!–#echo var="DOCUMENT_NAME"–>

现在时间：

<!–#echo var="DATE_LOCAL"–>

显示IP地址：

<! #echo var="REMOTE_ADDR"–>

②将文本内容直接插入到文档中<#include>

<! #include file="文件名称"–>

<!--#include virtual="index.html" -->

<! #include virtual="文件名称"–>

<!--#include virtual="/www/footer.html" -->

注：file包含文件可以在同一级目录或其子目录中，但不能在上一级目录中，virtual包含文件可以是Web站点上的虚拟目录的完整路径

③显示WEB文档相关信息<#flastmod><#fsize>(如文件制作日期/大小等)

文件最近更新日期：

<! #flastmod file="文件名称"–>

文件的长度：

<!–#fsize file="文件名称"–>

④直接执行服务器上的各种程序<#exec>(如CGI或其他可执行程序)

<!–#exec cmd="文件名称"–>

<!--#exec cmd="cat /etc/passwd"-->

<!–#exec cgi="文件名称"–>

<!--#exec cgi="/cgi-bin/access_log.cgi"–>

将某一外部程序的输出插入到页面中。可插入CGI程序或者是常规应用程序的输入，这取决于使用的参数是cmd还是cgi。

⑤设置SSI信息显示格式<#config>(如文件制作日期/大小显示方式)

⑥高级SSI可设置变量使用if条件语句。
```

## 三、应用的场景

通常情况下会和xss注入同时出现，一般都是两个漏洞。如果服务器开启了支持的话，就会存在SSI漏洞。

条件大概有以下几点

```
Web 服务器已支持SSI（服务器端包含）

Web 应用程序未对对相关SSI关键字做过滤

Web 应用程序在返回响应的HTML页面时，嵌入用户输入
```

常用的注入命令：https://www.owasp.org/index.php/Server-Side_Includes_%28SSI%29_Injection

挖掘的思路大概是以下两个方面:

```
从业务场景来Fuzz，比如获取IP、定位、时间等

识别页面是否包含.stm,.shtm和.shtml后缀

伏特分布式漏洞扫描平台已经全面支持SSI检测。
```

页面是否是SHTML基本上可以石锤这个漏洞的存在性

## 四、参考例题
 [BJDCTF2020]EasySearch