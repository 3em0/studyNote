---
title: Apache_conf
date: 2020-10-24 20:26:30
tags: 编程
---

# Apache的主要目录和配置文件理解

参考链接:http://httpd.apache.org/docs/2.4/misc/security_tips.html

## 一、Apache主要配置文件注释（演示）

Apache的主配置文件位置:`/etc/httpd/conf/httpd.conf`，默认站点的主目录:`/var/www/html/`

官方文档的第一句便告诉我们:`The location of this file is set at compile-time, but may be overridden with the -f command line flag`。And配置文件的修改只有在重启服务器之后才会重新生效，当然`htaccess`可以在临时目录下更改一些配置，`这些是文件上传常常有的漏洞位置`

---

### 基础配置区

- `ServerRoot "/www/server/apache"` 指定用于指定Apache的运行目录，后面的所有相当路径都是以此

- `mutex`互斥锁，进程调试的一个概念，不会直接跳过

- `Listen 0.0.0.0:80` appache的监听的ip和端口，可以防止阿帕奇监听太多

  ---

### 模块区

- <IfModule>

  ```
  <IfModule http2_module>
  ProtocolsHonorOrder On
  Protocols h2 http/1.1
  </IfModule>
  ```

  作用：检测模块是否能够使用，如果不能使用则会直接恢复期间的所有数据

  使用地点：当且仅当你的模块必须要配置文件才能启动时，才使用section。module可以是文件名

  可以在htaccess中使用

- LoadModule 

  ```
  LoadModule authn_file_module modules/mod_authn_file.so
  将这个动态加载库加载进来并取名为module
  ```

  不可在htaccess中使用

  loads the named module from the modules subdirectory of the ServerRoot.

  **漏洞**：题目中可能在此处加载一些其他的解释器进行绕过对PHP的限制（详情：西湖挑战杯）

  几个重要模块:

  `rewrite_module modules/mod_rewrite.so` 

  ```
  伪静态模块，重写模块，其中的原来我不是很明白，但是有两点作用
  1.开启URL匹配的规则，进行重定向，在URL无法解析的，执行其他的规则
  2.AllowOverride配合，实现重载部分配置的作用，使htacess生效
  ```

  ```
  原理解析:AllowOverride参数就是指明Apache服务器是否去找.htacess文件作为配置文件，如果设置为none,那么服务器将忽略.htacess文件，如果设置为All,那么所有在.htaccess文件里有的指令都将被重写。对于AllowOverride，还可以对它指定如下一些能被重写的指令类型. 
  通常利用Apache的rewrite模块对 URL 进行重写的时候， rewrite规则会写在 .htaccess 文件里。但要使 apache 能够正常的读取.htaccess 文件的内容，就必须对.htaccess 所在目录进行配置。从安全性考虑，根目录的AllowOverride属性一般都配置成不允许任何Override
  ```

  ---

### Scope of Directives

  放在主配置文件（行为对根目录的限制），放在这个类型中的（对指定目录下权限限制）

- `<Directory>`

  ```
  <Directory />     #行为对根目录的限制
      Options FollowSymLinks   # followsymlinks表示允许使用符号链接，默认为禁用
      AllowOverride None     # 表示禁止用户对目录配置文件(.htaccess进行修改)重载，普通站点不建议开启
  </Directory>
  
  <Directory "/var/www/html">#行为仅对/var/www/html目录生效
      Options Indexes FollowSymLinks
      AllowOverride None
      Order allow,deny
      Allow from all
  </Directory>
  ```

  options中Indexes表示当网页不存在的时候允许索引显示目录中的文件，FollowSymLinks是否允许访问符号链接文件。有的选项有ExecCGI表是否使用CGI，如Options Includes ExecCGI FollowSymLinks表示允许服务器执行CGI及SSI，禁止列出目录。SymLinksOwnerMatch表示当符号链接的文件和目标文件为同一用户拥有时才允许访问。

  AllowOverrideNone表示不允许这个目录下的访问控制文件来改变这里的配置，这也意味着不用查看这个目录下的访问控制文件，修改为：AllowOverride All 表示允许.htaccess。Order对页面的访问控制顺序后面的一项是默认选项，如allow，deny则默认是deny，Allowfromall表示允许所有的用户，通过和上一项结合可以控制对网站的访问控制。具体参数参考如下:

  ```
  Apply directive AllowOverride None (disabling .htaccess files).
  Apply directive AllowOverride FileInfo (for directory /home).
  Apply any FileInfo directives in /home/.htaccess, /home/web/.htaccess and /home/web/dir/.htaccess in that order.
  ```

  - AllowOverride 不多说了（使htaccess（注意不只是这个文件）中的哪些配置生效）对于不同的配置设置，在`htaccess`文件中生效的配置，Only available in <Directory> sections

    ```reStructuredText
    针对于他的三种配置结果有很多的配置生效问题是需要考虑
    1.ALL 不用多说
    2.None 直接就没有读取htaccess
    3.directive-type
     3.1 AuthConfig
     3.2 FileInfo
     3.3 Indexes
     3.4 Limit
     3.5 Nonfatal=[Override|Unknown|All]
     3.6 Options
    ```

    ```
    下面是无脑配置:
    1.只要支持这个配置项就能生效的配置（截取）
    <Files>，<FilesMatch> 匹配文件
    LimitRequestBody 限制请求的大小 LimitXMLRequestBody
    LuaHookLog 看到这个就要想到该死LUA绕过disfunc
  2.AuthConfig（提供一些用户名，密码的检测机制）
    Anonymous_* 检测后面的*类型是不是*类型 检测关键字
    ```
    
    有用的配置内容（懂的都懂）
    
    ### FileInfo（对服务器提供的响应和元数据进行广泛的控制）
    
    **Action(为特定处理程序或内容类型激活CGI脚本)**
    
    ```
    eg:Action image/gif /cgi-bin/images.cgi
    用iamges.cgi来处理image/GIF文件
    AddHandler my-file-type .xyz（用my来处理.xyz文件）
    Action my-file-type "/cgi-bin/program.cgi"（用program.cgi来付给my）
    SetHandler news-handler
    Action news-handler "/cgi-bin/news.cgi" virtual
    ```
    
    **AddCharset**（为特定文件设置编码方式）
    
    默认的编码方式：[AddDefaultCharset](http://httpd.apache.org/docs/2.4/mod/core.html#adddefaultcharset)
    
    这个可以用UTF-7等特殊的编码方式来绕过对于PHP文件的内容进行查杀的SHELL进行绕过 **AddEncoding**(为文件增加encode方式)
    
    ```
    AddEncoding x-gzip .gz
    AddEncoding x-compress .Z
    这里的x-很快就被取消掉了，直接使用GZIP和compress的压缩方式就可以了。
    ```
    
    **AddHandler和SetHandler **（ [handler](http://httpd.apache.org/docs/2.4/handler.html)）
    
    ```
    这个会和PHP的运行不同可能不能采取，看是否是handler模式运行
    Set:强制所有匹配的文件由处理程序处理
    <Location "/status">
      SetHandler server-status
    </Location> http://servername/status was called. 必须放在主配置中
    其他的参数就不再多说了，注意查看上面的handler中的内容
    Add:将文件扩展名映射到指定的处理程序
    AddHandler cgi-script .cgi 这里的cgi-script可以通过上面的action
    ```
    
    **AddType和ForceType **(将给定的文件名扩展名映射到指定的内容类型)
    
    注意和上面的区别
    
    [CGIMapExtension](http://httpd.apache.org/docs/2.4/mod/core.html#cgimapextension)（设置CGI脚本的解释器）
    
    **PassEnv和SetEnv和SetEnvIfExpr**（环境变量的问题）
    
    ```
    PASS 是指从shell传递环境变量
    Set 是指设置一个环境变量(设置一个内部环境变量，该变量随后可用于Apache HTTP Server模块，并传递给CGI脚本和SSI页面)这个设置的时间是比较晚的，如果要早设置，用SetEnvIf.
    SetEnv SPECIAL_PATH /foo/bin
    Unset 可以删除
    SetEnvIfExpr 通过一个表达式设置环境变量，字符串
    ENV就是环境变量的意识，其他的就是增加了一些条件判断
    ```
    
    **Rewrite**
    
    ### Indexes(控制服务器提供的目录索引页面包括自动索引生成)
    
    ### Limit(授权命令，限制访问)
    
    ### Options(访问Option和类似的指令以及控制过滤器)
    
    **FilterChain**（设置过滤器链）
    
    **SSLOptions**（SSL链接控制器）
    
    **Options**(配置特定目录中可用的特性)
    
    ```
    1.All
    2.ExecCGI(使用cgi脚本)
    3.FollowSymLinks
    就是允许你的网页文件夹下的链接文件链接到首页目录以外的文件。举例来说，如果你把首页目录设置为/var/www/html，那么你的网页程序最多只能访问到/var/www/html目录，上层目录是不可见的。但是你可以通过链接把文件链接到/var/www/html目录以外的文件以访问该文件，如果FollowSymLinks被设置的话
    Even though the server follows the symlink it does not change the pathname used to match against <Directory> sections.
    The FollowSymLinks and SymLinksIfOwnerMatch Options work only in <Directory> sections or .htaccess files
    4.Includes
    Server-side includes provided by mod_include are permitted.
    开启服务器端的包含
    eg：
    AddType text/html .shtml
    AddOutputFilter INCLUDES .shtml
    Options +Includes
    5.IncludesNOEXEC
    Server-side includes are permitted, but the #exec cmd and #exec cgi are disabled. It is still possible to #include virtual CGI scripts from ScriptAliased directories.
    服务器端包括是允许的，但#exec cmd和#exec cgi是禁用的。禁用系统命令
    6.Indexes（在目录下没有index.html时是否显示目录结构）
    7.MultiViews
    Content negotiated "MultiViews" are allowed using mod_negotiation.
    Note
    This option gets ignored if set anywhere other than <Directory>, as mod_negotiation needs real resources to compare against and evaluate from.
    
    8.SymLinksIfOwnerMatch
    The server will only follow symbolic links for which the target file or directory is owned by the same user id as the link.
    Note
    The FollowSymLinks and SymLinksIfOwnerMatch Options work only in <Directory> sections or .htaccess files.
    
    This option should not be considered a security restriction, since symlink testing is subject to race conditions that make it circumventable.
    
    Normally, if multiple Options could apply to a directory, then the most specific one is used and others are ignored; the options are not merged. (See how sections are merged.) However if all the options on the Options directive are preceded by a + or - symbol, the options are merged. Any options preceded by a + are added to the options currently in force, and any options preceded by a - are removed from the options currently in force.
    
    Note
    Mixing Options with a + or - with those without is not valid syntax and will be rejected during server startup by the syntax check with an abort.
    
    For example, without any + and - symbols:
    
    <Directory "/web/docs">
      Options Indexes FollowSymLinks
    </Directory>
    
    <Directory "/web/docs/spec">
      Options Includes
    </Directory>
    then only Includes will be set for the /web/docs/spec directory. However if the second Options directive uses the + and - symbols:
    
    <Directory "/web/docs">
      Options Indexes FollowSymLinks
    </Directory>
    
    <Directory "/web/docs/spec">
      Options +Includes -Indexes
    </Directory>
    then the options FollowSymLinks and Includes are set for the /web/docs/spec directory.
    
    Note
    Using -IncludesNOEXEC or -Includes disables server-side includes completely regardless of the previous setting.
    
    The default in the absence of any other settings is FollowSymlinks.
    ```
    
    

- <DirectoryMatch>

  ```
  <DirectoryMatch "^/var/www/combined/(?<sitename>[^/]+)">
      Require ldap-group cn=%{env:MATCH_SITENAME},ou=combined,o=Example
  </DirectoryMatch>
  ```

  In order to prevent confusion, numbered (unnamed) backreferences are ignored. Use named groups instead.

  **和上面的标签一样,都不能在htaccess中使用**

- <FILE>

  对文件的单独操作，可以在htaccess中使用，并且在htaccess加载完后才会加载，加个match也是一样的效果。

- <Location> 匹配URL，加个match也是一样的,在file加载完后才会加载

- <VirtualHost>

  ```
  可以使用虚拟主机上下文中允许的任何指令。当服务器接收到对特定虚拟主机上的文档的请求时，它使用包含在<VirtualHost>部分中的配置指令。
  ```

  ```
  <VirtualHost 10.1.2.3:80>
    ServerAdmin webmaster@host.example.com
    DocumentRoot "/www/docs/host.example.com"
    ServerName host.example.com
    ErrorLog "logs/host.example.com-error_log"
    TransferLog "logs/host.example.com-access_log"#这个日志文件如果是别人可以写入的，那就需要重新衡量一下这个安全等级了。
  </VirtualHost>
  ```

  一些都是正常的配置命名，不用细说的命令。
  
  ---

## .htaccess文件

参考链接：1.http://httpd.apache.org/docs/2.4/howto/htaccess.html

2.http://httpd.apache.org/docs/2.4/mod/overrides.html

- AccessFileName

  ```
  AccessFileName .acl
  ```

  Before returning the document `/usr/local/web/index.html`, the server will read `/.acl`, `/usr/.acl`, `/usr/local/.acl` and `/usr/local/web/.acl` for directives unless they have been disabled with

  ```
  Name of the distributed configuration file（分布式的脚本配置文件）可以在htaccess中使用
  ```

  **可以绕过对于htaccess文件的限制**


参考链接:

1.核心功能：http://httpd.apache.org/docs/2.4/mod/core.html

2.mod中的mod_mime：https://www.jb51.net/tools/onlinetools/apache-chs/mod/mod_mime.html

## 二、Apache的语法规则

```config
SetEnv SPECIAL_PATH /foo/bin
这是设置环境变量
```

- AcceptPathInfo

  ```
  Off(当且仅当URL指向真实存在的地址时返回正确内容，如果有PATHINFO就返回404)
  A request will only be accepted if it maps to a literal path that exists. Therefore a request with trailing pathname information after the true filename such as /test/here.html/more in the above example will return a 404 NOT FOUND error.
  On（类比与THinkPHP来考虑）
  A request will be accepted if a leading path component maps to a file that exists. The above example /test/here.html/more will be accepted if /test/here.html maps to a valid file.
  Default
  The treatment of requests with trailing pathname information is determined by the handler responsible for the request. The core handler for normal files defaults to rejecting PATH_INFO requests. Handlers that serve scripts, such as cgi-script and isapi-handler, generally accept PATH_INFO by default.
  
  PathInfo模式:PATH_INFO的方式的URL也是从协议开始,然后后面跟上域名,域名后面是入口文件,入口文件后依次为模块,控制器,方法;然后是依次传入的参数
  ```

  AllowEncodedSlashes（作用：是否允许`%2F`for `/` and additionally `%5C` for \ ）经常和他同时使用

- AddDefaultCharset 

   ```
   设置页面的默认编码
   可以在htaccess中使用
   ```

   **漏洞**：配合上面这点，配合我们的输入内容，可以构建一个XSS攻击。

   同类型的有:`AddCharset`可以对不同后缀名的文件实现编码

   ```config
   AddCharset EUC-JP .euc
   AddCharset ISO-2022-JP .jis
   AddCharset SHIFT_JIS .sjis
   ```

   

## 三、Module mod_mime

这是一个关于对于`content-type`和`language`解析的模块，这个模块在老版本的apache中经常出现解析问题，但是经过这么多年了，很多的漏洞都被修复了，但是其自带的一些天然的解析特点依然可以被我们用在绕过`WAF`的过程中，这样或许可以达到事半功倍的效果，这是我把这一部分单独拿出来的原因，废话不多说，我们进入正题。

  ### Files with Multiple Extensions

当一个文件具有多个后缀名的时候，如果两个后缀名的含义是不一样的，指向的是不同类型的处理器，这样不会留下什么隐患，比如`word.html.en`和`word.en.html`这两者都将被解析成为media-type `text/html`和 `Content-Language: en`。

但是当这样的文件名出现的时候就会出现问题`world.php.jpg`。这个文件最后会被解析成为一个php文件，原因是，apache的模块在解析的时候，是从右向左的，后面的结果会覆盖后面的结果。所以当我们在对于不同的后缀名指定解释器的时候，就应该使用`sethandler`，而不是`AddHandler`

### Content encoding

```
Content-encoding: pkzip
```

解码格式

### AddType

```config
AddType image/gif .gif
AddType image/jpeg jpeg jpg jpe
```

### MultiviewsMatch

这个配置的两个选项，一个允许多重后缀名，一个不允许多重后缀名。

```
MultiviewsMatch Any|NegotiatedOnly|Filters|Handlers [Handlers|Filters]
```

当其设置为`handlers`和`filters`这样就可以匹配解释器来运行了。