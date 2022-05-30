# 寻觅踪迹

## 0x01 tcpdump

`tcpdump -vv  -i ens33`跟踪指定的网卡

`tcpdump -vv  -i ens33  port http`指定支持显示的协议有http。

`-r 从文件中读取流量包，

`-c`表示最大能抓到的packet的数量。

`hose`结合port利用对发往或来自特定主机的i像那些链接。

## 0x02 ngrep

是tcpdump和grep的结合体。

`ngrep -q -c 64 linux port 80` 

`-q`只答应载荷的头部

`-c`显示多少列

`-xx`用16进制在流量包中搜索。

:a:查看当前的路由表

```
dem0@ubuntu:~/Desktop/bash$ ip route
default via 192.168.76.2 dev ens33 proto dhcp metric 100 
169.254.0.0/16 dev ens33 scope link metric 1000 
172.17.0.0/16 dev docker0 proto kernel scope link src 172.17.0.1 
172.18.0.0/16 dev br-3c117fa30e74 proto kernel scope link src 172.18.0.1 linkdown 
192.168.76.0/24 dev ens33 proto kernel scope link src 192.168.76.128 metric 100 
```

:b:查看最近链接的主机

```
ip neighbor
192.168.76.254 dev ens33 lladdr 00:50:56:eb:d9:b5 STALE
192.168.76.2 dev ens33 lladdr 00:50:56:f7:f5:18 STALE
```

:ab:跟踪路由

```
ip route get 192.168.76.2
```

## 0x03 srace 跟踪系统调用

```c
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
void main(){
	char *tmp;
	tmp = malloc(400);
	strcat(tmp,"testing");
	printf("TMP:%s",tmp);
	free(tmp);
	exit(0);
}

```

strace

```
execve("./a.out", ["./a.out"], 0x7ffda59d1c70 /* 48 vars */) = 0
brk(NULL)                               = 0x55b2e9c2d000
arch_prctl(0x3001 /* ARCH_??? */, 0x7ffe24287490) = -1 EINVAL (Invalid argument)
access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
openat(AT_FDCWD, "/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
fstat(3, {st_mode=S_IFREG|0644, st_size=67877, ...}) = 0
mmap(NULL, 67877, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7f63fc812000
close(3)                                = 0
openat(AT_FDCWD, "/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0\360q\2\0\0\0\0\0"..., 832) = 832
pread64(3, "\6\0\0\0\4\0\0\0@\0\0\0\0\0\0\0@\0\0\0\0\0\0\0@\0\0\0\0\0\0\0"..., 784, 64) = 784
pread64(3, "\4\0\0\0\20\0\0\0\5\0\0\0GNU\0\2\0\0\300\4\0\0\0\3\0\0\0\0\0\0\0", 32, 848) = 32
pread64(3, "\4\0\0\0\24\0\0\0\3\0\0\0GNU\0\t\233\222%\274\260\320\31\331\326\10\204\276X>\263"..., 68, 880) = 68
fstat(3, {st_mode=S_IFREG|0755, st_size=2029224, ...}) = 0
mmap(NULL, 8192, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f63fc810000
pread64(3, "\6\0\0\0\4\0\0\0@\0\0\0\0\0\0\0@\0\0\0\0\0\0\0@\0\0\0\0\0\0\0"..., 784, 64) = 784
pread64(3, "\4\0\0\0\20\0\0\0\5\0\0\0GNU\0\2\0\0\300\4\0\0\0\3\0\0\0\0\0\0\0", 32, 848) = 32
pread64(3, "\4\0\0\0\24\0\0\0\3\0\0\0GNU\0\t\233\222%\274\260\320\31\331\326\10\204\276X>\263"..., 68, 880) = 68
mmap(NULL, 2036952, PROT_READ, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f63fc61e000
mprotect(0x7f63fc643000, 1847296, PROT_NONE) = 0
mmap(0x7f63fc643000, 1540096, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x25000) = 0x7f63fc643000
mmap(0x7f63fc7bb000, 303104, PROT_READ, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x19d000) = 0x7f63fc7bb000
mmap(0x7f63fc806000, 24576, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1e7000) = 0x7f63fc806000
mmap(0x7f63fc80c000, 13528, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7f63fc80c000
close(3)                                = 0
arch_prctl(ARCH_SET_FS, 0x7f63fc811540) = 0
mprotect(0x7f63fc806000, 12288, PROT_READ) = 0
mprotect(0x55b2e84eb000, 4096, PROT_READ) = 0
mprotect(0x7f63fc850000, 4096, PROT_READ) = 0
munmap(0x7f63fc812000, 67877)           = 0
brk(NULL)                               = 0x55b2e9c2d000
brk(0x55b2e9c4e000)                     = 0x55b2e9c4e000
fstat(1, {st_mode=S_IFCHR|0620, st_rdev=makedev(0x88, 0), ...}) = 0
write(1, "TMP:testing", 11TMP:testing)             = 11
exit_group(0)                           = ?
+++ exited with 0 +++
```

malloc和free是管理任务内存的用户空间函数，他们仅仅在程序总的内存发生改变时猜调用brk.

## 0x04 lstarce

```
dem0@ubuntu:~/Desktop/bash$ ltrace ./a.out 
TMP:testing+++ exited (status 0) +++
```

因为我们没有定义用户空间函数，所以程序没有输出

```
dem0@ubuntu:~/Desktop/bash$ ltrace -S ./a.out 
SYS_brk(0)                                                                                                                   = 0x55cb08b6b000
SYS_arch_prctl(0x3001, 0x7ffd0b602e10, 0x7f38bc5372c0, 13)                                                                   = -22
SYS_access("/etc/ld.so.preload", 04)                                                                                         = -2
SYS_openat(0xffffff9c, 0x7f38bc540b80, 0x80000, 0)                                                                           = 3
SYS_fstat(3, 0x7ffd0b602010)                                                                                                 = 0
SYS_mmap(0, 0x10925, 1, 2)                                                                                                   = 0x7f38bc50a000
SYS_close(3)                                                                                                                 = 0
SYS_openat(0xffffff9c, 0x7f38bc54ae10, 0x80000, 0)                                                                           = 3
SYS_read(3, "\177ELF\002\001\001\003", 832)                                                                                  = 832
SYS_pread(3, 0x7ffd0b601dd0, 784, 64)                                                                                        = 784
SYS_pread(3, 0x7ffd0b601da0, 32, 848)                                                                                        = 32
SYS_pread(3, 0x7ffd0b601d50, 68, 880)                                                                                        = 68
SYS_fstat(3, 0x7ffd0b602060)                                                                                                 = 0
SYS_mmap(0, 8192, 3, 34)                                                                                                     = 0x7f38bc508000
SYS_pread(3, 0x7ffd0b601cb0, 784, 64)                                                                                        = 784
SYS_pread(3, 0x7ffd0b601990, 32, 848)                                                                                        = 32
SYS_pread(3, 0x7ffd0b601970, 68, 880)                                                                                        = 68
SYS_mmap(0, 0x1f14d8, 1, 2050)                                                                                               = 0x7f38bc316000
SYS_mprotect(0x7f38bc33b000, 1847296, 0)                                                                                     = 0
SYS_mmap(0x7f38bc33b000, 0x178000, 5, 2066)                                                                                  = 0x7f38bc33b000
SYS_mmap(0x7f38bc4b3000, 0x4a000, 1, 2066)                                                                                   = 0x7f38bc4b3000
SYS_mmap(0x7f38bc4fe000, 0x6000, 3, 2066)                                                                                    = 0x7f38bc4fe000
SYS_mmap(0x7f38bc504000, 0x34d8, 3, 50)                                                                                      = 0x7f38bc504000
SYS_close(3)                                                                                                                 = 0
SYS_arch_prctl(4098, 0x7f38bc509540, 0xffff80c743af61a0, 64)                                                                 = 0
SYS_mprotect(0x7f38bc4fe000, 12288, 1)                                                                                       = 0
SYS_mprotect(0x55cb06edb000, 4096, 1)                                                                                        = 0
SYS_mprotect(0x7f38bc548000, 4096, 1)                                                                                        = 0
SYS_munmap(0x7f38bc50a000, 67877)                                                                                            = 0
SYS_brk(0)                                                                                                                   = 0x55cb08b6b000
SYS_brk(0x55cb08b8c000)                                                                                                      = 0x55cb08b8c000
SYS_fstat(1, 0x7ffd0b602630)                                                                                                 = 0
SYS_write(1, "TMP:testing", 11TMP:testing)                                                                                              = 11
SYS_exit_group(0 <no return ...>
+++ exited (status 0) +++

```

-S选项可以将系统和用户的显示出来。

# 系统调优

## 0x01 简介

常用的监视工具

`cpu: top,dstat,perf,ps,mpstat,strace,trace`

`网络:netstat,ss,iotop,ip,iptraf,nicstat,ethtool`

`磁盘: ftrace,iostat,dstat和blktrace`

`内存:top，dstsat,perf,vmstat和swapon`

##  0x02 识别服务

`ps -p 1 -o cmd`查看当前系统的初始化进程

`/sbin/init auto noprompt`

```
dem0@ubuntu:~/Desktop/bash$ ps -eaf | grep upstart
dem0       18547    9828  0 18:01 pts/0    00:00:00 grep --color=auto upstart
dem0@ubuntu:~/Desktop/bash$ ps -eaf | grep systemd
root         356       1  0 13:45 ?        00:00:00 /lib/systemd/systemd-journald
```

很明显，我的电脑运行的时systemd.

查看运行的服务

```
 [ + ]  acpid
 [ - ]  alsa-utils
 [ - ]  anacron
 [ - ]  apache-htcacheclean
 [ - ]  apache2
 [ + ]  apparmor
 [ + ]  apport
 [ + ]  avahi-daemon
 [ + ]  bluetooth
 [ - ]  console-setup.sh
 [ + ]  cron
 [ + ]  cups
 [ + ]  cups-browsed
 [ + ]  dbus
 [ + ]  docker
 [ + ]  gdm3
 [ + ]  grub-common
 [ - ]  hwclock.sh
 [ + ]  irqbalance
 [ + ]  kerneloops
 [ - ]  keyboard-setup.sh
 [ + ]  kmod
 [ + ]  mongodb
 [ + ]  network-manager
 [ + ]  open-vm-tools
 [ + ]  openvpn
 [ - ]  plymouth
 [ - ]  plymouth-log
 [ - ]  pppd-dns
 [ + ]  procps
 [ - ]  pulseaudio-enable-autospawn
 [ - ]  redis-server
 [ - ]  rsync
 [ + ]  rsyslog
 [ - ]  saned
 [ - ]  screen-cleanup
 [ - ]  speech-dispatcher
 [ - ]  spice-vdagent
 [ + ]  udev
 [ + ]  ufw
 [ + ]  unattended-upgrades
 [ - ]  uuidd
 [ + ]  whoopsie
 [ - ]  x11-common
```

前面是`+`表示正在运行，`-`表示没有运行。

##  0x03 禁用服务

`systemctl disable` 

相反启用就是`enable`

## 0x04 补充

以上内容都是通过系统提供的来运行的，但是还有许多是用户手动添加的。`xinetd`:按需启用

直接在他的配置文件中修改，并且重启就可以了。

## 0x05 ss命令

`ss -t`查看TCP套接字

`ss -l`查看处于LISTEN状态的链接

`ss -u`查看处于udp

:a:演示过程

ss -el 查看到在services中没有显示的端口服务。

我们用 `lsof -i:port`来查看具体服务是什么

## 0x06 dstat收集IO

dtsat ： 默认每秒收集磁盘额IO信息。

`--top-bio` 显示出执行IO最多的进程

`--top-cpu` CPU占用最多的进程

`--top-io`网络io站哟个

`--top-latency` 系统负载

`--top-mem`内存负载

## 0x07 pidstat

dstat如果一个程序有多个进程，他就探测不出来了。

找出同一个进程所有的资源占用。

`-d 输出io`

`-r 缺页故障和内存使用`

`-u 输出cpu`

`-w输出任务切换`

## 0x08 sysctl 系统调参

## 0x09 nice可以调整任务的优先级

# 结语

这个系列终于结束了（歇逼，花了一周时间过了一下强大的linux系统
