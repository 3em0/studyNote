## 0x01 ps

常用指令模式

`ps -e` 显示所有进程

`ps -eo pattern,patter` -o指定过滤器显示特定的进程内容

:a: 这里如何来确认命令的运行环境是否正确，可以使用命令e``

`ps -eo pid,cmd e | tail -n 1`

这个指令就可以看见运行的最后一条指令时运行环境的环境变量，这方便我们在增加定时任务的时候，将相应的环境变量添加到i其中。

:b:创建进程树状图，跟踪子进程和父进程

`ps -u clif f | grep -A2 Xterm | head -3`

这个命令就可以看到所有的shell调用的ssh会话。

:red_car:介绍一下过滤器

`-u`指定有效用户

`-U`指定进程的真实用户

`-L` 显示线程相关的信息 NLWP(线程数量) LWP(线程ip)

## 0x02 文件信息

`which`  找出命令位置

`whereis` 找出命令位置+手册的位置

`whatis`输出简短介绍

 `file`输出文件的格式和其他信息。

## 0x03 杀死那个进程

`kill -l`  列举能使用的信号

```bash
dem0@ubuntu:~/Desktop/bash$ kill -l
 1) SIGHUP	 2) SIGINT	 3) SIGQUIT	 4) SIGILL	 5) SIGTRAP
 6) SIGABRT	 7) SIGBUS	 8) SIGFPE	 9) SIGKILL	10) SIGUSR1
11) SIGSEGV	12) SIGUSR2	13) SIGPIPE	14) SIGALRM	15) SIGTERM
16) SIGSTKFLT	17) SIGCHLD	18) SIGCONT	19) SIGSTOP	20) SIGTSTP
21) SIGTTIN	22) SIGTTOU	23) SIGURG	24) SIGXCPU	25) SIGXFSZ
26) SIGVTALRM	27) SIGPROF	28) SIGWINCH	29) SIGIO	30) SIGPWR
31) SIGSYS	34) SIGRTMIN	35) SIGRTMIN+1	36) SIGRTMIN+2	37) SIGRTMIN+3
38) SIGRTMIN+4	39) SIGRTMIN+5	40) SIGRTMIN+6	41) SIGRTMIN+7	42) SIGRTMIN+8
43) SIGRTMIN+9	44) SIGRTMIN+10	45) SIGRTMIN+11	46) SIGRTMIN+12	47) SIGRTMIN+13
48) SIGRTMIN+14	49) SIGRTMIN+15	50) SIGRTMAX-14	51) SIGRTMAX-13	52) SIGRTMAX-12
53) SIGRTMAX-11	54) SIGRTMAX-10	55) SIGRTMAX-9	56) SIGRTMAX-8	57) SIGRTMAX-7
58) SIGRTMAX-6	59) SIGRTMAX-5	60) SIGRTMAX-4	61) SIGRTMAX-3	62) SIGRTMAX-2
63) SIGRTMAX-1	64) SIGRTMAX
```

`kill   pid`终止进程

`kill -s pid` 指定发送给进程的信号

```
1 对进程或终端进行挂起检测
2 ctrl+c
9 强行杀死
15 默认终止进程信号
20 ctrl+z
```

`killall + 进程的名字` ： 可以终止进程

## 0x04 捕获并且响应信号

![image-20211231232705464](https://img.dem0dem0.top/images/image-20211231232705464.png)

```bash
#!/bin/bash
#name:
#to:


function handler(){
	echo Hey recived signal: SIGINT
}

echo My process ID is $$

trap "handler" SIGINT
while true
do
	sleep 1
done
```

## 0x05 wall和write

向使用同一个系统的其他终端用户发送信息。

信息：包括文件

`wall`是向终端的所有用户发送信息。

## 0x06 /proc

每个进程在这个目录下都有一个属于自己目录，目录名词就是pid。

`environ:`  进程运行的环境变量

`cwd` 进程的工作目录

`exe` 这是一个到进程所对应的可执行文件的符号链接

`fd`进程用到的文件描述符

`io` 进程所读/写的字符数

## 0x07 收集系统信息

`hostname uname` 获取主机的名字

`uname -a` 输出Linux内核的banbe

```
Linux ubuntu 5.11.0-43-generic #47~20.04.2-Ubuntu SMP Mon Dec 13 11:06:56 UTC 2021 x86_64 x86_64 x86_64 GNU/Linux
```

`uname -r`  输出发行版本

```
5.11.0-43-generic
```

proc下的神奇文件

```bash
/proc/cpuinfo 输出CPU的详细信息
/proc/meminfo 内存相关的信息
/proc/partitions 分区信息
```

## 0x08 cron 进行进程调度

当cron执行命令的时候是以`该表项的创建者身份去执行`的，但它不会去执行用户的.bashrc文件，**如果需要环境变量需要自己定义。**

这个命令的排版格式

```
分钟 小时 天 月份 星 命令
```

加入需要每五分钟 `*/5` 这个就是随便哪五分钟都可以。

:a:设置环境变量

```
00 * * * * http_proxy=xxxx;cmd
```

这个是针对每一条指令生成的环境变量环境

:b: 系统启动时执行命令

`@reboot cmd`

`-r`删除cron表

`-u`查看指定用户的表，以身份。

## 0x09 用户管理

`useradd USER -p PASSWORD -m` 添加用户

`deluser USER -s SHELL --remove-all-files`  删除用户

`chsh user -s shell` 修改用户的默认shell

`chage -E date` 处理用户过期信息