---
title: 安卓之旅-4
date: 2020-11-20 21:26:42
tags: Android
---

# Android之Xposed

基础书籍推荐:`1.疯狂JAVA讲义；2.疯狂安卓讲义；`

逆向分析必须知道他的原理，不然只会用工具，那就直接GG。

谷歌的镜像网站：https://developers.google.com/android/images

然后要准备的东西有

```
1.上面的系统包
2.twrp
3.supersu
```

下载解压完成后，进入bootloader，然后`./flash-all.sh`

`adb push SR5-SuperSU-v2.82-SR5-20171001224502.zip /sdcard`

`adb push de.robv.android.xposed.installer_3.1.5-43_minAPI15\(nodpi\)_apkmirror.com.apk /sdcard/Download/`

`adb reboot bootloader`

`fastboot flash  recovery twrp-3.3.0-0-bullhead.img`

然后如果失败的话就在本地进行fastboot替换就可以了。

然后再常规刷入即可。

```
【亲测有效】开机后wifi有感叹号, 时间无法同步解决办法 
在手机的shell里以root用户执行：
# settings put global captive_portal_http_url https://www.google.cn/generate_204
# settings put global captive_portal_https_url https://www.google.cn/generate_204
# settings put global ntp_server 1.hk.pool.ntp.org
# reboot
```

# 手调smail开始了

## Xposed开发环境和Hello 

```
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">


    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="请输入纯数字噢"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />


    <EditText
        android:id="@+id/editText"
        android:hint="username"
        android:layout_width="fill_parent"
        android:layout_height="40dp"
        android:maxLength="20"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="1.0"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.095" />

    <EditText
        android:id="@+id/editText2"
        android:hint="password"
        android:layout_width="fill_parent"
        android:layout_height="40dp"
        android:maxLength="20"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.239" />

    <Button
        android:id="@+id/button"
        android:layout_width="100dp"
        android:layout_height="35dp"
        android:layout_gravity="right|center_horizontal"
        android:text="LOGIN"
        android:visibility="visible"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.745" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

```
package com.example.demo10;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    EditText username_et;
    EditText password_et;
    TextView message_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("demo10roysue","Hello from demo10!");

        password_et = (EditText) this.findViewById(R.id.editText2);
        username_et = (EditText) this.findViewById(R.id.editText);
        message_tv = ((TextView) findViewById(R.id.textView));

        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (username_et.getText().toString().compareTo("admin") == 0) {
                    message_tv.setText("You cannot login as admin");
                    return;
                }
                //hook target
                message_tv.setText("Sending to the server :" + Base64.encodeToString((username_et.getText().toString() + ":" + password_et.getText().toString()).getBytes(), Base64.DEFAULT));

            }
        });

    }
}
```

## 手调demo10.apk

上参考链接: https://blog.csdn.net/YJJYXM/article/details/109197440

### 可调试app

有源码调试就不用展示了，下面直接来jeb的无源码调试。这是一个可以调试的app。

`adb install -r -t demo10.apk`

```
-r 升级
-t 允许安装调试版的app
```

Android 低版本展示安装的包名:`pm list packages | grep demo`

然后直接开始手撸.以调试模式来运行`dumpsys activity top | grep demo10`找到要调试的应用的入口

```
adb shell am start -D -n
```

然后就可以进行调试了。

### 不可调试app

插件: `Xdebuggable`

> Smali: 抠算法细节 变形的标准算法 本地变量 小项目
>
> frida: 大项目 快速定位 各种执行流

frida 逆向三把斧

>hook定位
>
>主动调用
>
>批量调用

# DDMS调试

> 1. 辅助增强工具
> 2. DumpView分析

# 手机刷机: `Frida`开发和调试环境

https://www.freebuf.com/articles/network/190565.html

nodejs:https://nodejs.org/en/download/package-manager/

https://github.com/nodesource/distributions/blob/master/README.md#deb

- 老手机刷安卓10焕发第二春

  网站:https://sourceforge.net/projects/blissroms

- python全版本(pyenv

  kali和ubuntu一样的 安装pyenvhttps://github.com/pyenv/pyenv#installation

  ```
  sudo apt-get update; sudo apt-get install make build-essential libssl-dev zlib1g-dev \
  libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
  libncursesw5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev libffi-dev liblzma-dev
  ```

  然后安装

  ```
  想要使用基于特定frida版本的objection，只需先安装好特定版本的frida和frida-tools（星球里搜“特定版本”有对应关系），再去objection的release里找那个日期之后一点点的版本。比如以frida 12.8.0版本举例：
  pip install frida==12.8.0
  pip install frida-tools==5.3.0（在tags里面找
  pip install objection==1.8.4
  按照这个顺序，在装objection的时候，就会直接Requirement already satisfied，不会再去下载新的frida来安装了。
  ```

- frida(-tools)全版本随意切

  ```
  frida脚本开发环境正确操作：
  之前建议的这种方法frida-gum.d.ts已经被大胡子删掉了，他现在不赞成使用这种方式。
  目前最新正确的Frida环境搭建方法：
  1. git clone https://github.com/oleavr/frida-agent-example.git
  2. cd frida-agent-example/
  3. npm install
  4. 使用VSCode等IDE打开此工程，在agent下编写typescript，会有智能提示。
  5. npm run watch会监控代码修改自动编译生成js文件
  6. frida -U -f com.example.android --no-pause -l _agent.js
  ```

  

- objection版本，ip+端口,(批量)插件

  ```
  /frida-server-15.1.17-android-arm64 -v -l 0.0.0.0:8888 
  ```

- frida(rpc)多主机 多手机 多端口

```
pyenv依赖问题一直很麻烦，可以切到miniconda
1. 下载安装脚本（建议科学）：
proxychains wget repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh

2. 给安装脚本可执行权限
chmod +x Miniconda3-latest-Linux-x86_64.sh 

3. 运行安装脚本：
sh Miniconda3-latest-Linux-x86_64.sh 

source source .bashrc 
4. 安装python3.8.0环境（建议科学）
proxychains conda create -n py380 python=3.8.0

5. 切换到python3.8.0
conda activate py380

6. 安装frida 12.8.0全家桶（建议科学）
proxychains pip install frida==12.8.0
proxychains pip install frida-tools==5.3.0
proxychains pip install objection==1.8.4

7. 为了新建的终端默认的env在py380，可以将下面这句话放在/root/.bashrc的末尾，然后source一下。

conda activate py380

8. 也可以把这句话放在/root/.bashrc的末尾，source之后只要运行mfrida命令即可切换到py380环境。

alias mfrida="conda activate py380"

9. 第七条和第八条二选一即可。开始享受12.8.0全家桶吧~

10. 列出主机上已有的环境：
conda env list 

11. 删除主机上名为py380的环境：
conda env remove -n env_name
```

