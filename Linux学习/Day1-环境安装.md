## 0X00 环境安装

![image-20210120000652900](https://i.loli.net/2021/01/20/Ht8RJKr7Wqh1v3x.png)

inst.gpt：系统会开始检测安装环境，然后很快就好了。

![image-20210120000800654](https://i.loli.net/2021/01/20/lvyj21xtzROTLnD.png)

#### 0x01 设置时区、语言、键盘布局

![image-20210120000946937](https://i.loli.net/2021/01/20/NdK8AZM5Bw42lmD.png)

在第一项里面，然后切换键盘布局

![image-20210120001107520](https://i.loli.net/2021/01/20/mG4gzSvPAxskqNr.png)

#### 0x03 安装源设置与软件选择

![image-20210120001319488](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210120001319488.png)

菜鸟选择

#### 0x04 磁盘分区与文件系统设置

首先要有bios boot

![image-20210120001639256](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210120001639256.png)

我们可以看到分区方案的位置其实是有三种选项的，其意义对应如下

```
标准分区 就是我们一直谈的分区 /dev/vda1之类的
LVM 可以弹性增加或缩小文件系统容量的分区
LVM精简配置 使用多少容量给你分配多少容量
```

下面再总结一下文件系统选项

```
ext2/ext3/ext4 linux早期的文件系统
swap 磁盘模拟为内存的交换分区 不会使用到目录树的挂载，不需要指定
BIOS BOOT gpt分区表可能会用到的东西
xfs 默认文件系统centos7
vfat 同时被windows和linux都支持的文件系统
```

![image-20210120002428446](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210120002428446.png)

以前建议交换分区的大小是内存两倍的大小才好。

![image-20210120002940620](https://i.loli.net/2021/01/20/2UwQl3hrfYOAKBE.png)

#### 0x04 内核管理与网络设置

![image-20210120003022020](https://i.loli.net/2021/01/20/V3dBulXOjT8ct5F.png)

![image-20210120003109503](https://i.loli.net/2021/01/20/gldPUTvfzyJ9HKO.png)

![image-20210120003146132](C:\Users\Crawler\AppData\Roaming\Typora\typora-user-images\image-20210120003146132.png)

支持一边安装一边设置

![image-20210120003352827](https://i.loli.net/2021/01/20/kOQHwnXh4U32YBr.png)

#### 0x07 总结

乔黑板：所有的配置都会记录在`/root/anaconda-ks.cfg`目录下，方便快速重键。

```
建议百度kickstart
```

