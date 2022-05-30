#### 0x01 GNOME的操作

基本操作跳过即可

#### 0x02 X Window与命令行模式的切换

`clt+alt+f1-6`

其中1是windows界面，2是shell界面。是否需要默认使用图形界面，只需要`graphical.target`这个目标服务设置默认或者非默认即可。

#### 0x03 命令行的登陆

显示日期：`date `+ 显示格式用`+`来指定

显示日历：`cal month year`

简单的计算器：`bc` scale= 5 指定输出小数点后5位。

ctrl+d 代表键盘输入结束

shift+up 或 down 则是在shell中显示内容过多时，进行数据翻页。

 man page中的特殊字段

第一行中 data[1],

这里的1表示用户在shell中可以操作的命令或执行文件

如果是5表示是配置文件或某些文件的格式

如果是8，表示系统管理员才能使用命令

```
/word 在向下显示的文档中查询word
?word 向上查找word
```







