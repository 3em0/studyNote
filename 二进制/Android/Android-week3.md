---
title: 安卓之旅-3
date: 2020-10-09 10:34:19
tags: Android
---

## 安卓之旅-3

### 一、解决软件会自动断掉USB链接的设置

在手机上使用termux,开启frida，监听0.0.0.0:8888端口

```
./frida-** -l 0.0.0.0:8888
```

就会在0.0.0.0的端口上进行监听，这里的0.0.0.0是通过该设备的所有流量都会被劫持，而127.0.0.1只有本地机器发出的流量才会被劫持。然后，在电脑上开启objection

```
objection -N -h ip -p 端口 -g 包名 explore
```

使用下面的命令进行查看报名

```
frida -ps -H ip:端口 | grep shimeng
```

-h是适应网络链接，-u是使用usb链接

查看监听端口

```
netstat -tuulp | grep frida
lsof -p 2349 |grep TCP
netstat -aple | grep frida
```

## 二、破解第一步

1.我要确定点击解锁的第一步在哪里，就是要把类hook上，要`trace`

然后我们发现这个类有点问题，为什么一直在那里自己启动，然后一直跑，我们要看看他的方法了。

然后知道了他的源码是音量的增加。

2.然后输入密码试一下，来trace一下，然后再静态分析一波，继续深入，突突他



## 三、自动化动态分析和快速定位

先来准备一波工具，首先是工具解包

```
ln -s /root/Desktop/android-studio/jre/bin/jarsigner /usr/bin
ln -s /root/Android/Sdk/build-tools/30.0.3/aapt /usr/bin
ln -s /root/Android/Sdk/build-tools/30.0.3/aapt2 /usr/bin
ln -s /root/Android/Sdk/platform-tools/adb /usr/bin
```

创建一下细节

安装apktools

```
1.Download Linux wrapper script (Right click, Save Link As apktool)
2.Download apktool-2 (find newest here)
3.Rename downloaded jar to apktool.jar
4.Move both files (apktool.jar & apktool) to /usr/local/bin (root needed)
5.Make sure both files are executable (chmod +x)
6.Try running apktool via cli
```

objection  patchapk的参数设置

```
objection patchapk -s fulao2.apk -a x86
来对x86架构的来干他
help命令使用方法照旧
memory list exports lib.so 列举该so文件的所有导出函数
android heap search instances 搜索tostring 在安卓的堆上进行搜索 找到对应的handler也就是下面的hashid，就可以调用了。
android heap execute  + hashid + 函数 --return-string 执行这个handler的函数，获得返回值 最直接的主动调用
android heap evaluate + hashid  然后输出 console.log(clazz.getPixel(传入参数)) 就可以了

控制四大组件
android hooking +方法+四大组件
android intent launch_activity + activity 进入四大组件
android hooking list services 列举广播
android hooking watch 
jobs list 查看当前被hook的函数
jobs kill 杀掉任务

objection 继承了flask 可以从电脑向手机中传入脚本进行跑（详情看肉丝姐姐的知识星球)
objection -g EXPLORE --enable-api 打开flask 然后使用他提供的api接口进行继续的操作就可以实现想要的功能了。

```

## 四、真实app实操（去强制升级）

Dalvik虚拟机就是一个java代码的虚拟机，在本地的as路径下都有d8和dx，以及各种文件都会在本地进行生成

参考链接:https://www.jianshu.com/p/6bdbbab73705

首先来一个app实操(去升级)

1.`我们想一想弹出这个升级窗口是如何开发的`

参考链接：https://www.jianshu.com/p/18e1f518c625

2.现在要寻找他有三个办法

(1).字符串搜索大法

(2),顺着上面的思路进行hook

```
android heap search isntance + 类名
```

使用插件

```
plugin wallbreaker objectsearch android.app.AlertDialogxxxxxxxxxx 
plugin wallbreaker objectsearch android.app.AlertDialog   找到hashcode
plugin wallbreaker objectdump hashcode
```

就可以查看成功了，然后进行dexdump即可。

(3)和程序启动进行抢时间hook

(4)frida的两个模式：1.attach2.spawn两个模式

`--startup-command + 命令，这样可以开软件自启动命令`

最后记录两个生成签名和给包签名的命令

```
keytool -genkey -alias abc.keystore -keyalg RSA -validity 20000 -keystore abc.keystore
```

```
jarsigner  -verbose -keystore abc.keystore -signedjar testx.apk zhibo.apk abc.keystore
```

## 五、重打包去强升级

- DEXDump三种使用模式脱壳

  1.py脚本使用

  2.

- Objection快速自动化定位

- Wallbreaker内存可视漫游

  内存中存在的实例，new String("nihao"),GC kicak in

- **所见即所得的代码定位思路**（其实也是从开发的方向来看的）

- 修改源码重打包强制升级

内存会有回收机制，但是肉眼可见的类是可以直接看到的，

hook：就是在api上挂一个钩子

**下面来进行实操**

`青青草视频`

### 1.使用objection来进行链接并操作脱壳

```
objection -g com.hello.qqc explore
plugin load /root/.objection/plugins/dexdump/frida_dexdump
plugin dexdump dump
```

下载的文件自动保存到桌面下的目录`cd Desktop/com.hello.qqc`

寻找主进入口：`grep -ril "MainActivity" *`

### 2.直接使用dexdump中的main.py来进行脱壳

```
先把objection 跑起来，再进行运行，与壳做一手对抗
python3 main.py 
注意这里可以指定frida的指定模式,attach 或者swap
```

### 3.使用pip install

直接获得命令 frida-dexdump 直接进行脱壳

然后使用下面的命令进行重打包

```
apktool -s d + *.apk 保留dex文件
然后删除那个class.dex文件
然后把脱壳之后的文件中那个dex文件进行重命名classes,classes2,classes3
然后再更改那个入口进入的配置文件
```

### 真实APP实操

- 所谓原生就是离CPU越近，安卓就是libart.so来解释，linux原生就是原生直接解释

- 想要分析/破解加固，就必须从开发和加固的角度来实现

- 要看是不是我们要的类，就看他的结构对不对

- 直接乱码的数据根本没法复制的，就去进行编码，然后hook的时候再反编码回去

  案例:混淆后的Okhtpp3的混淆

