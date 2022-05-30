---
title: misc掉头发
date: 2020-12-15 20:32:32
tags:
---

# MISC出发

前言：听说misc打得好，头发多不了

kali自带的字典： `cd /usr/share/wordlists/`

字典网站:http://contest-2010.korelogic.com/wordlists.html.

# 上手区

## PDF

![image-20201215235723215](https://i.loli.net/2020/12/15/gxdRiJ9oM5QVjBA.png)

直接此地无银三百两，转word看图片后面

## give_you_flag

gif使用stegsolve打开，使用framework选项，一张一张的看，

![image-20201216000509626](https://i.loli.net/2020/12/16/JNabdeSq2cGym6E.png)

然后进行p图补角。

## GIF

![image-20201216000827328](https://i.loli.net/2020/12/16/j56GxUMcCERgkov.png)

104个图片有问题，黑白图片没有什么妖怪，那么就是0和1，或者摩尔斯。很显然摩尔斯没有`{}`

所以是0和1字符串，8位2进制。

## 一 2020-CSICTF-Misc-In Your Eyes

![image-20201215203510629](https://i.loli.net/2020/12/15/NnQvEBseahMimgY.png)

解压出来长这个样子，然后就是`蒙蔽树下蒙蔽果，蒙蔽树前你和我`。

但是基本猜测，没有奇奇怪怪的加密方式，我在试了一下steghide无果后，打开了一款神器

![image-20201215204106718](https://i.loli.net/2020/12/15/PaI9cvNg4Ob8XoE.png)

然后不用多说的，找到下面的数据

```
2471491ED07C69930E8F994E383E415F
```

十六进制数据转二进制谢谢

根据提示，眼睛看不见，猜测是布莱叶盲文

```
100100011100010100100100011110110100000111110001101010000000000000000000000000000000000000000000000000000000000000000000000000
```

```
http://tyleregeto.com/article/braille-6bit-binary-language
```

## 二 unseen

![image-20201215204732102](https://i.loli.net/2020/12/15/bFjvBk2IczsnamU.png)

![image-20201215204740621](https://i.loli.net/2020/12/15/W29gnNzCJGTlm8y.png)

![image-20201215204756078](https://i.loli.net/2020/12/15/xGS3qc9vDN2EXMC.png)

会找到flag.txt 发现全部都是换行和空格

![image-20201215205053115](https://i.loli.net/2020/12/15/MyXSizvRdLTrmQK.png)

## 三 panda

```
fcrackzip -v -u -D -p rockyou.txt panda.zip
```

爆破压缩包，然后 

![image-20201215205141757](https://i.loli.net/2020/12/15/pA2e8sqwHz9EotM.png)

用神器比对，得出flag