# 抓包环境配置: `charles` 

- 抓包环境

  1. postern
  2. android 8.1
  3. frida
  
- VPN代理的优越性在于检测的难度

- https的证书挂载

  ![https://images.zsxq.com/FgNeSb0GlC9EB5XhR6fZ6CKHpcKH?imageMogr2/auto-orient/quality/100!/ignore-error/1&e=1648742399&token=kIxbL07-8jAj8w1n4s9zv64FuZZNEATmlU_Vm6zD:VSYqEJ4PcQqw7xNr4WKRBuWdX9A=](https://images.zsxq.com/FgNeSb0GlC9EB5XhR6fZ6CKHpcKH?imageMogr2/auto-orient/quality/100!/ignore-error/1&e=1648742399&token=kIxbL07-8jAj8w1n4s9zv64FuZZNEATmlU_Vm6zD:VSYqEJ4PcQqw7xNr4WKRBuWdX9A=)

  

根目录无法挂载的话，建议挂载到`system`。
- socks5是最强的协议(不接受反驳)
- 科学挂载是需要 `extern options`

> 一般： https: 大部分只采用客户端校验服务器
>
> 双向绑定: 很少有服务器校验客户端
>
> SSL Cert Pinning: 更少 :趣充

# 基于Hook的抓包



> 缺点： 不如抓包软件全面
>
> 优点: 无视证书 基于HOOK直接得到参数



- ZenTracer how to use

  可以hook所有的类和几千个API。
  
- Objection 

  ```
  objection -g 报名 explore -c "文件"
  ```
  
- 定位方法

  1. 通过objection 打出所有类

  2. 在objection的目录中 列举 

  3. grep搜索特征

  4. hook网络方法 查看调用栈

- SSlOGGER


# Frida 使用

## 基本使用方法

- 两种操作模式: 命令 RPC

- `FRIDA`开发思想: `HOOK`三把斧头

  >1.先hook 看参数和返回值
  >
  >2.再构造参数 主动调用
  >
  >3.最后配合RPC导出结果

- `FRIDA` 两种操作APP模式: spawn attach

- `FRIDA`命令行参数和常见模式

- `FRIDA HOOK JAVA`

## 参数调用栈返回值

>Hook 用不上Java.choose的
>

## 主动调用及批量自动化

> 忽略方法实现: 构造参数 主动调用

# 网络库 `HttpURLConnection`

自吐：`spawn`

先看内存，然后看Android developer 最后看aosp源码

# okhttp3+logging

- 网络库之`okhttp3`介绍
- 开发流程和逻辑分析
- 关键类、收发包函数梳理
- Frida脚本收发内容"字吐"
- Okhttp Interpretor

可以使用官网的拦截器的代码，直接将dex文件加入到dex中，使用官网的拦截器去拦截。

```
Java.openClassFile(dexPath).load();
```

```js
function hook_okhttp3() {
    Java.perform(function () {
        Java.openClassFile("/data/local/tmp/classes.dex").load();
        var MyInterceptor = Java.use("com.r0ysue.learnokhttp.MyInterceptor");
        var MyInterceptorObj = MyInterceptor.$new();
        var Builder = Java.use("okhttp3.OkHttpClient$Builder");
        console.log(Builder);
        Builder.build.implementation = function () {
            this.interceptors().add(MyInterceptorObj);
            return this.build();
        };
        console.log("hook_okhttp3...");
    });
}
```

> 针对于这几篇课程，主要使用的就是下面这几篇文章中讲到的
>
> https://mp.weixin.qq.com/s?__biz=Mzg3MjU3NzU1OA==&mid=2247496439&idx=1&sn=a32f3d50e680ef1a51f254f846dca556&source=41#wechat_redirect
>
> https://github.com/r0ysue/AndroidSecurityStudy

简单调用 官网的拦截器

```java

package com.r0ysue.learnokhttp;

/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;


public final class okhttp3Logging implements Interceptor {
    private static final String TAG = "okhttpGET";

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        String requestStartMessage = "--> "
                + request.method()
                + ' ' + request.url();
        Log.e(TAG, requestStartMessage);

        if (hasRequestBody) {
            // Request body headers are only present when installed as a network interceptor. Force
            // them to be included (when available) so there values are known.
            if (requestBody.contentType() != null) {
                Log.e(TAG, "Content-Type: " + requestBody.contentType());
            }
            if (requestBody.contentLength() != -1) {
                Log.e(TAG, "Content-Length: " + requestBody.contentLength());
            }
        }

        Headers headers = request.headers();
        for (int i = 0, count = headers.size(); i < count; i++) {
            String name = headers.name(i);
            // Skip headers from the request body as they are explicitly logged above.
            if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                Log.e(TAG, name + ": " + headers.value(i));
            }
        }

        if (!hasRequestBody) {
            Log.e(TAG, "--> END " + request.method());
        } else if (bodyHasUnknownEncoding(request.headers())) {
            Log.e(TAG, "--> END " + request.method() + " (encoded body omitted)");
        } else {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            Log.e(TAG, "");
            if (isPlaintext(buffer)) {
                Log.e(TAG, buffer.readString(charset));
                Log.e(TAG, "--> END " + request.method()
                        + " (" + requestBody.contentLength() + "-byte body)");
            } else {
                Log.e(TAG, "--> END " + request.method() + " (binary "
                        + requestBody.contentLength() + "-byte body omitted)");
            }
        }


        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            Log.e(TAG, "<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        Log.e(TAG, "<-- "
                + response.code()
                + (response.message().isEmpty() ? "" : ' ' + response.message())
                + ' ' + response.request().url()
                + " (" + tookMs + "ms" + (", " + bodySize + " body:" + "") + ')');

        Headers myheaders = response.headers();
        for (int i = 0, count = myheaders.size(); i < count; i++) {
            Log.e(TAG, myheaders.name(i) + ": " + myheaders.value(i));
        }

        if (!HttpHeaders.hasBody(response)) {
            Log.e(TAG, "<-- END HTTP");
        } else if (bodyHasUnknownEncoding(response.headers())) {
            Log.e(TAG, "<-- END HTTP (encoded body omitted)");
        } else {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();

            Long gzippedLength = null;
            if ("gzip".equalsIgnoreCase(myheaders.get("Content-Encoding"))) {
                gzippedLength = buffer.size();
                GzipSource gzippedResponseBody = null;
                try {
                    gzippedResponseBody = new GzipSource(buffer.clone());
                    buffer = new Buffer();
                    buffer.writeAll(gzippedResponseBody);
                } finally {
                    if (gzippedResponseBody != null) {
                        gzippedResponseBody.close();
                    }
                }
            }

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            if (!isPlaintext(buffer)) {
                Log.e(TAG, "");
                Log.e(TAG, "<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                return response;
            }

            if (contentLength != 0) {
                Log.e(TAG, "");
                Log.e(TAG, buffer.clone().readString(charset));
            }

            if (gzippedLength != null) {
                Log.e(TAG, "<-- END HTTP (" + buffer.size() + "-byte, "
                        + gzippedLength + "-gzipped-byte body)");
            } else {
                Log.e(TAG, "<-- END HTTP (" + buffer.size() + "-byte body)");
            }
        }

        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyHasUnknownEncoding(Headers myheaders) {
        String contentEncoding = myheaders.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }
}
```

# 网络库 Retrofit

基于扇面的okhttp3看看就好了，干货一般(

# 究极自吐Socket

- 顶层协议的基石:`Sokcet`
- 基于内存漫游的关键类定位
- HTTP收发包底层究极自吐
- HTTPS收发包底层究极自吐
- 所有网络底层究极自吐`libc(ssl)`,`Syscall`

> todo: Socket的函数hook read write

# WebSocket XMPP 协议自吐

> ws特征: 和http是孪生兄弟
>
> 使用库也是okhttp
>

# protobuf逆向

> 先搞一个demo 然后再来脱机

# 没有抓不到的包

- 手机上抓: `Nethunter`“可视化抓包” 直观

  > 优点: 无法对抗，全部都能抓，直观，所见所得 粒度太粗了。

- 手机`wireshark`

  > 只能看明文，不能加解密

- 手机上抓:`tcpdump`

  > 不需要装kali nethunter
  >
  > 挂载到system下

  ![image-20220307083136492](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220307083136492.png)

- 电脑+手机 `hook Socket`

  > - 优点1: 对于包的内容进行解密
  > - 优点2: 不需要解协议，无需绕过证书协议
  
- 电脑+手机`Charles`+`Postern`

  > - 优点:全面 已经解好了协议 HTTP
  > - 缺点:

# 没有`trace`不出的执行流

- 类是安卓时间的一等公民
- 基于内存漫游的关键类定位和trace
- 溯源就是定位到具体的类

- 案例:`移动TV`

  > 为什么不能以spawn的方式运行，因为开始壳还在上面

  总结:

  * 对开发的熟悉成都决定逆向的程度

  * 所有的判读都在服务器，那在本地的hook有上面用呢?

  * 寻找业务逻辑漏洞

# Frida分析违法应用

- Frida hook Socket抓所有应用层包
- Frida hook onClick 直接定位控件业务代码
- Frida hook Ciper 直接定位加解密代码
- Java.choose 修改对象属性破解`VIP`
- Frida hook 收发包/改包破解`VIP`
- Frida RPC /Python开发脱机脚本

> - 竖向选择 alt+shift
>
> - 计算的过程最好放在电脑上，把数据send到电脑中

# Frida Hook Native

- 基础

  > - JNI&NDK
  > - ND开发的基本流程
  > - 静态注册喝动态注册
  > - frida hook native

>cmdline: c++flite 可以解出name-mangling之后的函数原型

> 参考链接:
>
> 1. https://www.jianshu.com/p/87ce6f565d37
> 2. https://bbs.pediy.com/thread-224672.html

# Frida Hook libssl.so

- 安卓系统框架中的SSL库解析

- Java层与Native层对照联动关系

- 枚举系统库的符号表和导出表

- frida-trace so 符号自动化trace

  > 无脑一把梭哈 Nice的工具

- frida hook native 打印各种数据类型

- frida RPC 流量重组成pcap保存

> 我的native函数? 在哪个so包里面呢

# Frida Hook libc

- libc.so介绍
- 字符操作关键函数:
- 文件操作关键函数:
- 网络操作关键函数:
- 线程操作关键函数:

> 枚举符号修复一下

比较常用的写入函数记录

```js
function writeSomething(path, contents) {
    var fopen_addr = Module.findExportByName("libc.so", "fopen");
    var fputs_addr = Module.findExportByName("libc.so", "fputs");
    var fclose_addr = Module.findExportByName("libc.so", "fclose");

    //console.log("fopen=>",fopen_addr,"  fputs=>",fputs_addr,"  fclose=>",fclose_addr);

    var fopen = new NativeFunction(fopen_addr, "pointer", ["pointer", "pointer"])
    var fputs = new NativeFunction(fputs_addr, "int", ["pointer", "pointer"])
    var fclose = new NativeFunction(fclose_addr, "int", ["pointer"])

    //console.log(path,contents)

    var fileName = Memory.allocUtf8String(path);
    var mode = Memory.allocUtf8String("a+");

    var fp = fopen(fileName, mode);

    var contentHello = Memory.allocUtf8String(contents);
    var ret = fputs(contentHello,fp)

    fclose(fp);
}
```

比较常用的attach函数

```js
function attach(name,address){
    // console.log("attaching ",name,"address ==>",address);
   Interceptor.attach(address,{
       onEnter:function(args){
          console.log("Entering => " ,name);
          console.log("args[0] => ",args[0].readCString());
           // console.log("args[1] => ",args[1].readCString())
           // console.log("args[2] => ",args[2])

       },onLeave:function(retval){
           console.log("retval is => ",retval)
       }
   })

}
```

Frida hook so的部分参考

```js
function traceNativeExport(){
    var modules = Process.enumerateModules();
    for(var i =0; i < modules.length; i++){
        var module = modules[i];
        if(module.name.indexOf("libc.so")<0){
            continue
        }
        var exports = module.enumerateExports();
        for(var j = 0; j < exports.length; j++){
            if(exports[j].name.indexOf("open")>=0){
                attach(exports[j].name,exports[j].address)
            }
            // console.log("module name is ==> ",module.name,"symbol name is ==>",exports[j].name);
        }
    }
}

function traceNativeSymbol(){
    var modules = Process.enumerateModules();
    for(var i =0; i < modules.length; i++){
        var module = modules[i];

        var exports = module.enumerateSymbols();
        for(var j = 0; j < exports.length; j++){
            var path="/data/user/0/com.demo.deso/cache/so/"+module.name+".txt";
            console.log("module name is ==> ",module.name,"symbol name is ==>",exports[j].name);
            writeSomething(path,"type: "+exports[j].type+" function name :"+exports[j].name+" address : "+exports[j].address+" offset => 0x"+(exports[j].address - module.base).toString(16)+"\n");
        }
    }
}
```

# FRIDA 补充

- HOOK
- 主动调用补充
- 参数构造补充
- 动态加载DEX
- native hook 补充

> 主动调用的参数构造两种方法:
>
> - 先hook
> - 自己new一个(域内反射)

# 案例解析

- 第一题

  ```java
  package com.kanxue.pediy1;
  
  import android.view.View;
  import java.io.UnsupportedEncodingException;
  import java.security.MessageDigest;
  import java.security.NoSuchAlgorithmException;
  
  /* loaded from: classes.dex */
  public class VVVVV {
      private VVVVV() {
      }
  
      public static boolean VVVV(View.OnClickListener context, String input) {
          if (input.length() != 5) {
              return false;
          }
          byte[] v = eeeee(input);
          byte[] p = "6f452303f18605510aac694b0f5736beebf110bf".getBytes();
          if (v.length != p.length) {
              return false;
          }
          for (int i = 0; i < v.length; i++) {
              if (v[i] != p[i]) {
                  return false;
              }
          }
          return true;
      }
  
      private static byte[] eeeee(String input) {
          byte[] SALT = {95, 35, 83, 73, 75, 35, 95};
          try {
              StringBuilder sb = new StringBuilder();
              sb.append((char) SALT[0]);
              sb.append((char) SALT[1]);
              for (int i = 0; i < input.length(); i++) {
                  sb.append((char) input.getBytes("iso-8859-1")[i]);
                  sb.append((char) SALT[i + 2]);
              }
              sb.append((char) SALT[6]);
              byte[] bArr = new byte[0];
              return sssss(sb.toString()).getBytes("iso-8859-1");
          } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
              return null;
          }
      }
  
      private static String ccccc(byte[] data) {
          StringBuffer buf = new StringBuffer();
          for (int i = 0; i < data.length; i++) {
              int halfbyte = (data[i] >>> 4) & 15;
              int two_halfs = 0;
              while (true) {
                  if (halfbyte < 0 || halfbyte > 9) {
                      buf.append((char) ((halfbyte - 10) + 97));
                  } else {
                      buf.append((char) (halfbyte + 48));
                  }
                  halfbyte = data[i] & 15;
                  int two_halfs2 = two_halfs + 1;
                  if (two_halfs >= 1) {
                      break;
                  }
                  two_halfs = two_halfs2;
              }
          }
          return buf.toString();
      }
  
      private static String sssss(String text) {
          try {
              MessageDigest md = MessageDigest.getInstance("SHA-1");
              byte[] bArr = new byte[40];
              md.update(text.getBytes("iso-8859-1"), 0, text.length());
              return ccccc(md.digest());
          } catch (UnsupportedEncodingException e2) {
              e2.printStackTrace();
              return null;
          } catch (NoSuchAlgorithmException e) {
              e.printStackTrace();
              return null;
          }
      }
  }
  ```

  可以看到最无脑的办法就是主动调用eeee方法，然后爆破。

- 第二题

  寻找类加载器`Frida hook : hook动态加载的dex，与查找interface，`

  这里的解释就是因为loadDexClass在OnclickListener中调用，所以当点击事件发生之后，要加载的DexClassLoader才会被创建。可以首先先Java.choose主动调用loadDexClass方法；然后Java.enumerateClassLoaders枚举所有的类加载器，找到存在“com.kanxue.pediy1.VVVVV”的类加载器。

  ```js
    Java.choose("com.kanxue.pediy1.MainActivity",{
              onMatch:function(instance){
                  console.log("hook instance method",instance.loadDexClass());
                  // console.log("hook instance stringFromJni",instance.stringFromJNI('66999').overload('java.lang.String'));
              },onComplete:function(){console.log("search complete")}
          })
          Java.enumerateClassLoaders({
              onMatch:function(loader){
                  console.log("Find ClassLoader",loader);
                  try{
                      if (loader.findClass("com.kanxue.pediy1.VVVVV")){
                          console.log("Successfully Find ClassLoader",loader);
                          Java.classFactory.loader = loader;
                      }
                  }catch(error){
                      console.log("found error",error);
   
                  }
   
              },onComplete:function(){console.log("find complete")}
          })
  ```

- 第三关

  过frida的反调试

  > hook libc 看看trace了哪些函数

  ```js
  var kill_addr = Module.findExportByName("libc.so","kill");
      // var kill = new NativeFunction(kill_addr,"int",["int","int"])
      Interceptor.replace(kill_addr,new NativeCallback(function(int1,int2){
  
      },"int",["int","int"]))
  ```

# Hook抓包

r0capture: 

# Frida辅助证书解绑两大案例

- 服务器校验客户端证书,原理和案例
- Frida绕过检测开启抓包自动退出(基于hook的抓包 无视证书)
- 客户端证书和密码直接Frida自吐
- 客户端证书格式互转及导入抓包
- 证书绑定场景的鉴定,分析和解绑
- 混淆后的证书绑定Frida解绑抓包

> 服务器在校验客户端证书：400 No required SSL certificate was sent

需要设置charles的抓包端口可以更好解析协议，然后keystore可以转换证书格式

> 证书绑定校验失败：Client closed the connection before a request was made. Possibly the SSL certificate was rejected.
> You may need to configure your browser or application to trust the Charles Root Certificate. See SSL Proxying in the Help menu.
>
> 这个是在客户端的证书校验，将自己的证书给校验了一下，不然就给自己关掉。

这里需要理解的一点是，加入我们在开发的过程中需要使用到下载到手机上的证书进行对于证书的校验，那么我们必然会使用到的就是`文件操作`函数来进行一把梭哈。那么我们hook的点也可以在这里产生。

PW：6at67t6t65rwq6sa5wq565r4qwrsaa

# NDK+NDK-examples

https://github.com/android/ndk-samples

https://www.jianshu.com/p/87ce6f565d37
