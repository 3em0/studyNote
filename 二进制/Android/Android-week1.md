---
title: 安卓之旅-1
date: 2020-10-05 20:13:35
tags: Android
---

# Android-week1

## kali报错指北

- apt-key报错

  ```bash
  curl https://archive.kali.org/archive-key.asc | apt-key add
  ```

- 时区

  ```
  # dpkg-reconfigure tzdata
  然后选择Asia→Shanghai
  ```

- 中文显示

  ```
  'apt install xfonts-intl-chinese'
  apt install ttf-wqy-microhei
  ```

- 中文输入法

  https://blog.csdn.net/qq_42333641/article/details/89325576

- genymotion 刷入arm支持

  ```
  Genymotion-ARM-Translation_for_8.0
  ```

- wifi有一个×

  ```
  在手机的shell里以root用户执行：
  # settings put global captive_portal_http_url https://www.google.cn/generate_204
  # settings put global captive_portal_https_url https://www.google.cn/generate_204
  # settings put global ntp_server 1.hk.pool.ntp.org
  # reboot
  ```

- netstat

  ```
  netstat -aple 进程的端口和当前状态
  netstat -tunlp | grep port 端口和进程绑定
  ```

  

## 一、四大组件与系统架构

### （一）Android四大组件

1、Activity

一个Activity通常就是一个单独的窗口。Activity之间通过Intent进行通信。Activity应用中每一个Activity都必须要在AndroidManifest.xml配置文件中声明，否则系统将不识别也不执行该Activity。

2、Service

Started（启动）：当应用程序组件（如Activity）调用StartService()方法启动服务时，服务处于Started状态。

bound（绑定）：当应用程序组件调用bindService()方法绑定到服务时，服务处于bound状态。

Service通常位于后台运行，它一般不需要与用户交互，因此Service组件没有图形用户界面。Service组件需要继承Service基类。Service组件通常用于为其他组件提供后台服务或监控其他组件的运行状态。

3、Content provider

Android平台提供了Content Provider使一个应用程序的指定数据集提供给其他应用程序。其他应用可以通过ContentResolver类从该内容提供者中获取或存入数据。

只有需要在多个应用程序间共享数据是才需要内容提供者。例如，通讯录数据被多个应用程序使用，且必须存储在一个内容提供者中。它的好处是统一数据访问方式。

ContentProvider实现数据共享。ContentProvider用于保存和获取数据，并使其对所有应用程序可见。这是不同应用程序间共享数据的唯一方式，因为android没有提供所有应用共同访问的公共存储区。

开发人员不会直接使用ContentProvider类的对象，大多数是通过ContentResolver对象实现对ContentProvider的操作。5、ContentProvider使用URI来唯一标识其数据集，这里的URI以content://作为前缀，表示该数据由ContentProvider来管理。

4、Broadcast Receiver

你的应用可以使用它对外部事件进行过滤，只对感兴趣的外部事件(如当电话呼入时，或者数据网络可用时)进行接收并做出响应。广播接收器没有用户界面。然而，它们可以启动一个activity或serice来响应它们收到的信息，或者用NotificationManager来通知用户。通知可以用很多种方式来吸引用户的注意力，例如闪动背灯、震动、播放声音等。一般来说是在状态栏上放一个持久的图标，用户可以打开它并获取消息。

广播接收者的注册有两种方法，分别是程序动态注册和AndroidManifest文件中进行静态注册。

动态注册广播接收器特点是当用来注册的Activity关掉后，广播也就失效了。静态注册无需担忧广播接收器是否被关闭，只要设备是开启状态，广播接收器也是打开着的。也就是说哪怕app本身未启动，该app订阅的广播在触发时也会对它起作用。

### （二）Android系统架构

`Android`采用分层的架构，分为四层，从高层到底层分为`应用程序层`（app+System apps），`应用程序框架层`（Java API Framework），`系统运行库和运行环境层`(Libraries + android Runtime)和`Linux核心层`(HAL+ Linux Kernel)

![](https://images0.cnblogs.com/blog/473657/201301/18203746-970e2cbe223e4c1c9ca129e7a2feb6c6.jpg)

### （三） Android常用开发与逆向命令总结

1、`file`查看文件属性

2、使用`echo`命令写内容到文件中，然后利用`cat`读取文件内容。

3、使用`dumpsys`命令获取当前顶层`activity`的信息，grep进行过滤，-i参数忽略大小写。

4、`ls -alit`按时间排序显示当前目录全部信息。

5、`dumpsys package com.termux`查看该APP内存中的信息

6、`ps -e`显示全部进程

`ps -e |grep -i termux `筛选命令

7、dumpsys dbinfo com.termux查看数据库信息

8、`adb pull /sdcard/app`将手机中的文件拷贝到电脑当前目录下

`adb push D:\tmp.txt /sdcard`将本地文件放到手机中

9、`adb logcat`查看当前日志信息

```
adb logcat |grep -i com.termux
```

10、指定连接某台设备的`adb shell`

```
adb -s 192.168.3.18:5555 shell
```

11、查看某端口对应的进程名

```
netstat -tunlp |grep 7001  # 端口
netstat -tunlp |grep "com.termux" # 进程名
netstat -aple |grep -i https #正在通信的端口，查看使用https的通信
netstat -tuulp|grep  查看监听端口
lsof -p + 进程ip|grep + TCP 查看监听端口
```

12、`htop`实时查看手机进程

```
pkg install htop
```

手机`root`用户查看`htop`，全部进程

```
$ su
# /data/data/com.termux/files/usr/bin/htop
```

一本书的链接：http://yuedu.163.com/book_reader/581dbb97c3424be08ab582cf64735cde_4

## 二、手机刷机指北

**环境也是分3,6,9等的**

**Frida两套环境：**

1、pixel(sailfish)+官方8.1.0_r1+twrp3.3.0+Magisk+Frida

2、pixel(sailfish)+twrp3.3.0+lineage16.0+addonsu16.0

**Xposed一套环境：**

1、pixel(sailfish)+官方7.1.2_r8+twrp3.2.1-0+SuperSU+XposedInstaller

**Fart同Aosp两套环境：**

1、pixel(sailfish)+最新fastboot+Fart8.1.0

2、n6p(angler)+老fastboot+Fart8.1.0

**Kali NetHunter一套环境：**

1、n6p(angler)+原生8.1.0_r1+twrp3.3.1+SuperSu



环境安装入门

```
kali时间 dpkg-reconfigure tzdata
kali中文  apt install xfonts-intl-chinese apt install ttf-wqy-microhei
下载Android studio wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/4.1.1.0/android-studio-ide-201.6953283-linux.tar.gz
tar -zxvf
必备三个软件 apt install tmux jnettop htop
把adb加入路径 nano ~/.bashrc  historysize 加三个0 PATH=$PATH:/root/Android/Sdk/platform-tools
telet ip 端口 查看端口
ping ip查看
neofetch 查看系统
```

然后

```
安装jeb 直接拷贝 unzip即可
安装jadx github上面下载 unzip即可
安装010 proxychains wget https://www.sweetscape.com/download/010EditorLinux64Installer.tar.gz
装pip proxychains python3  get-pip.py
apt install tree
tree -NCfhl 寻找文件
安装objection pip insatll objection
安装frida的安卓端：proxychains wget https://github.com/frida/frida/releases/download/12.11.18/frida-server-12.11.18-android-arm64.xz
插件 proxychains git clone https://github.com/hluwa/FRIDA-DEXDump ~/Downloads/FRIDA-DEXDump
proxychains git clone https://github.com/hluwa/Wallbreaker ~/.objection/plugins/Wallbreaker
```

安装apktool

```
右键另存为wrapper
mv apktool.txt apktool
mv apktool_2.4.1.jar  apktool.jar
```

## 
