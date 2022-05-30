---
title: 安卓之旅-2
date: 2020-10-06 09:16:23
tags: Android
---



# Android之旅2

## 一、动静态调试四大组件

(一)、activity 

一个又一个的界面，需要在`manifest`里面注册

(二)、

(三)、service

(四)、broadcast receiver

## 二、开始分析

1.先看`mainactivity`

2.receiver 注册广播，自动启动

3.二进制数据解析软件editor



## 三、objection 初步体验

`https://github.com/hluwa/Wallbreaker`

葫芦娃大佬的objection的插件

```
objection -g + 包名 + explore
android hooking watch class_method android.widget.Textview.setText --dump-args --dump-backtrace  --dump-return  hook 类的方法
android hooking list activity  查看活动
android hooking list class_methods + class name 查看类的方法
android hooking list classes hook 类名 然后 cat ./objection/objection.log |grep -i 筛选该包的方法即可
```

jadx 的体验，我就不说了，要学点开发，才能做逆向

