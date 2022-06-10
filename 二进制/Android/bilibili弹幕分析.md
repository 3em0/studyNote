哔哩哔哩的字幕和视频接口

## 0x01 抓包

视频接口抓取:

![image-20220404155837175](https://img.dem0dem0.top/images/image-20220404155837175.png)

我们可以从大小中看出来，某一个接口每次都特别大。所以初步猜测就是他

![image-20220404155931143](https://img.dem0dem0.top/images/image-20220404155931143.png)

我们先这样猜测是这个数据包，现在开始构造请求

```bash
curl -H 'Host: app.bilibili.com' -H 'env: prod' -H 'app-key: android' -H 'user-agent: Dalvik/2.1.0 (Linux; U; Android 8.1.0; Nexus 5X Build/OPM7.181205.001) 6.66.0 os/android model/Nexus 5X mobi_app/android build/6660400 channel/html5_search_google innerVer/6660400 osVer/8.1.0 network/2' -H 'x-bili-trace-id: 291ce2646146ea497cc1dffce7624adb:7cc1dffce7624adb:0:0' -H 'x-bili-aurora-eid: ' -H 'x-bili-aurora-zone: sh001' -H 'x-bili-metadata-bin: EgdhbmRyb2lkILDClgMqE2h0bWw1X3NlYXJjaF9nb29nbGUyJVhZM0ZFMzEzNUUwQTlBQjc1RjQwQzAyNjhFRUU0Qzk4N0Q5QjE6B2FuZHJvaWQ' -H 'x-bili-device-bin: CAEQsMKWAxolWFkzRkUzMTM1RTBBOUFCNzVGNDBDMDI2OEVFRTRDOTg3RDlCMSIHYW5kcm9pZCoHYW5kcm9pZDoTaHRtbDVfc2VhcmNoX2dvb2dsZUIGZ29vZ2xlSghOZXh1cyA1WFIFOC4xLjBaQGQ4MWI4Y2U5YjkxYTQwNzViNTE0Y2UzMzRkMTRkY2MyMjAyMjA0MDQxNTMyMTMzZTg5OTFkNGMyZTA2NWFmM2ZiQGQ4MWI4Y2U5YjkxYTQwNzViNTE0Y2UzMzRkMTRkY2MyMjAyMjA0MDQxNTMyMTMzZTg5OTFkNGMyZTA2NWFmM2ZqBjYuNjYuMHJAZDgxYjhjZTliOTFhNDA3NWI1MTRjZTMzNGQxNGRjYzIyMDIyMDQwNDE1MzIxMzNlODk5MWQ0YzJlMDY1YWYzZnj9vaqSBg' -H 'x-bili-network-bin: CAE' -H 'x-bili-restriction-bin: ' -H 'x-bili-locale-bin: Cg4KAnpoEgRIYW5zGgJDThIOCgJ6aBIESGFucxoCQ04' -H 'x-bili-exps-bin: CgIIAQ' -H 'buvid: XY3FE3135E0A9AB75F40C0268EEE4C987D9B1' -H 'x-bili-fawkes-req-bin: CgdhbmRyb2lkEgRwcm9kGghjOTVjYjk5Ng' -H 'bili-bridge-engine: cronet' -H 'content-type: application/grpc' --data-binary "
```

大概参数就是

```
x-bili-trace-id: 291ce2646146ea497cc1dffce7624adb:7cc1dffce7624adb:0:0
x-bili-aurora-eid:
x-bili-aurora-zone: sh001
x-bili-metadata-bin: EgdhbmRyb2lkILDClgMqE2h0bWw1X3NlYXJjaF9nb29nbGUyJVhZM0ZFMzEzNUUwQTlBQjc1RjQwQzAyNjhFRUU0Qzk4N0Q5QjE6B2FuZHJvaWQ
x-bili-device-bin: CAEQsMKWAxolWFkzRkUzMTM1RTBBOUFCNzVGNDBDMDI2OEVFRTRDOTg3RDlCMSIHYW5kcm9pZCoHYW5kcm9pZDoTaHRtbDVfc2VhcmNoX2dvb2dsZUIGZ29vZ2xlSghOZXh1cyA1WFIFOC4xLjBaQGQ4MWI4Y2U5YjkxYTQwNzViNTE0Y2UzMzRkMTRkY2MyMjAyMjA0MDQxNTMyMTMzZTg5OTFkNGMyZTA2NWFmM2ZiQGQ4MWI4Y2U5YjkxYTQwNzViNTE0Y2UzMzRkMTRkY2MyMjAyMjA0MDQxNTMyMTMzZTg5OTFkNGMyZTA2NWFmM2ZqBjYuNjYuMHJAZDgxYjhjZTliOTFhNDA3NWI1MTRjZTMzNGQxNGRjYzIyMDIyMDQwNDE1MzIxMzNlODk5MWQ0YzJlMDY1YWYzZnj9vaqSBg
x-bili-network-bin: CAE
x-bili-restriction-bin:
x-bili-locale-bin: Cg4KAnpoEgRIYW5zGgJDThIOCgJ6aBIESGFucxoCQ04
x-bili-exps-bin: CgIIAQ
buvid: XY3FE3135E0A9AB75F40C0268EEE4C987D9B1
x-bili-fawkes-req-bin: CgdhbmRyb2lkEgRwcm9kGghjOTVjYjk5Ng
```

总结一下就是下面这几个参数

```
x-bili-trace-id
x-bili-metadata-bin
x-bili-device-bin
x-bili-locale-bin
buvid
x-bili-fawkes-req-bin
```

然后我们可以看到其中`grpc`很显眼呀，我们从这个出发来破解一下，这里暂时忽略掉.

## 0x02 开始分析

首先我们搜索到grpc中设置`header`的方法https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/header/HeaderClientInterceptor.java。但是我们用`ClientInterceptor`,发现根本搜索不到，可以猜到加了一点小混淆。但是我不慌，我们看看这个`ClientInterceptor`[有没有其他的特征](https://github.com/grpc/grpc-java/blob/1ab7a6dd0fa03d2e7be049baf977f67ba358aae5/api/src/main/java/io/grpc/ClientInterceptor.java#L42)，`MethodDescriptor`,搜索，然后经过一顿操作之后，找到下面这个类，发现key都可以对上

![image-20220404210321233](https://img.dem0dem0.top/images/image-20220404210321233.png)

但是这个并不是最后的类。

![image-20220405083455757](https://img.dem0dem0.top/images/image-20220405083455757.png)

我们可以看到n0.h.f()这个方法肯定是有问题的，所以我们跟进去分析，我们很容易知道，(这个其实就是grpc的`addComman`忘记拼写了，但是就是那个添加headers)，根据参数类型，可以初步猜测就是这个类。

![image-20220405083734703](https://img.dem0dem0.top/images/image-20220405083734703.png)

```js
Java.perform(function(){
    function bytesToString(value) {
        var buffer = Java.array('byte', value);
        var StringClass = Java.use('java.lang.String');
        return StringClass.$new(buffer);
    }
    console.log("--->hook custom headers");
    var n0 = Java.use("io.grpc.n0");
    n0.n.overload("io.grpc.n0$h","java.lang.Object").implementation = function (key, value) {
        var reg = "Key{name='(.*?)'}";
        console.log("=====>key:" + key.toString().match(reg)[1]);
        if (value.getClass() == "class [B") {
            var _value = bytesToString(value);
            console.log("===>value:" + _value)
        }
        else {
            console.log("===>value:" + value)
        }
        this.n(key, value);
    }

});
```

这是对于一个headers的轻度自吐。我们继续分析

![image-20220405084203924](https://img.dem0dem0.top/images/image-20220405084203924.png)

抓包 看一下

```
CAEQsMKWAxolWFkzRkUzMTM1RTBBOUFCNzVGNDBDMDI2OEVFRTRDOTg3RDlCMSIHYW5kcm9pZCoHYW5kcm9pZDoTaHRtbDVfc2VhcmNoX2dvb2dsZUIGZ29vZ2xlSghOZXh1cyA1WFIFOC4xLjBaQGQ4MWI4Y2U5YjkxYTQwNzViNTE0Y2UzMzRkMTRkY2MyMjAyMjA0MDQxNTMyMTMzZTg5OTFkNGMyZTA2NWFmM2ZiQGQ4MWI4Y2U5YjkxYTQwNzViNTE0Y2UzMzRkMTRkY2MyMjAyMjA0MDQxNTMyMTMzZTg5OTFkNGMyZTA2NWFmM2ZqBjYuNjYuMHJAZDgxYjhjZTliOTFhNDA3NWI1MTRjZTMzNGQxNGRjYzIyMDIyMDQwNDE1MzIxMzNlODk5MWQ0YzJlMDY1YWYzZnj9vaqSBg
```

```
AEQsMKWAxolWFkzRkUzMTM1RTBBOUFCNzVGNDBDMDI2OEVFRTRDOTg3RDlCMSIHYW5kcm9pZCoHYW5kcm9pZDoTaHRtbDVfc2VhcmNoX2dvb2dsZUIGZ29vZ2xlSghOZXh1cyA1WFIFOC4xLjBaQGQ4MWI4Y2U5YjkxYTQwNzViNTE0Y2UzMzRkMTRkY2MyMjAyMjA0MDQxNTMyMTMzZTg5OTFkNGMyZTA2NWFmM2ZiQGQ4MWI4Y2U5YjkxYTQwNzViNTE0Y2UzMzRkMTRkY2MyMjAyMjA0MDQxNTMyMTMzZTg5OTFkNGMyZTA2NWFmM2ZqBjYuNjYuMHJAZDgxYjhjZTliOTFhNDA3NWI1MTRjZTMzNGQxNGRjYzIyMDIyMDQwNDE1MzIxMzNlODk5MWQ0YzJlMDY1YWYzZnj9vaqSBg
```

![image-20220405085602728](https://img.dem0dem0.top/images/image-20220405085602728.png)

这也就不难看出和`base64`的关系，但是我们想知道到底是哪里对base64进行了调用，但是发现好像没用.....

然后猜测是不是和`key`有关系

```
分析一下:
io.grpc.internal.z1.d(BL:8)
io.grpc.cronet.b.T(BL:4)
io.grpc.cronet.b.N(BL:1)
io.grpc.cronet.b$c.g(BL:20)
io.grpc.internal.a.n(BL:3)
```

![image-20220405091513774](https://img.dem0dem0.top/images/image-20220405091513774.png)

![image-20220405092440560](https://img.dem0dem0.top/images/image-20220405092440560.png)

![image-20220405092506833](https://img.dem0dem0.top/images/image-20220405092506833.png)



![image-20220405105610319](https://img.dem0dem0.top/images/image-20220405105610319.png)

![image-20220405105722110](https://img.dem0dem0.top/images/image-20220405105722110.png)

我们抓个实例看看

![image-20220405092930175](https://img.dem0dem0.top/images/image-20220405092930175.png)

![image-20220405093729169](https://img.dem0dem0.top/images/image-20220405093729169.png)

确实是它，确实是它.

下面也来假装总结一下哈

```
1.x-bili-*-bin相关的请求头在com.bilibili.lib.moss.internal.impl.grpc.interceptor.a.a(io.grpc.ai)生成
x-bili-*-bin相关的请求头在io.grpc.cronet.b.T的进行了base64编码处理，然后设置
其他请求头user-agent、content-type、te也在io.grpc.cronet.b.T(org.chromium.net.BidirectionalStream$Builder)设置
```

这些都是直接hook的addheader,他们说要找找原始数据

![image-20220405160225336](https://img.dem0dem0.top/images/image-20220405160225336.png)

`android hooking watch class_method com.bilibili.lib.moss.utils.e.l --dump-args --dump-backtrace --dump-return`,

```
(agent) [bwks4r5krjd] Return Value: # com.bapis.bilibili.metadata.fawkes.FawkesReq@cf1ce3d2
appkey: "android"                                        
env: "prod"                                   
session_id: "a0238def" 
```

数据吐出来了。这个

接着是`playload`的分析首先发现一个很奇怪的包名`com.bapis.bilibili.app.playurl.v1.PlayURLMoss.playView`

> 暂时分析到这里吧，开始看看c的CRT那个问题
>
> 操 没忍住(下午又想分析了)

![image-20220406154325033](https://img.dem0dem0.top/images/image-20220406154325033.png)

![image-20220406154352822](https://img.dem0dem0.top/images/image-20220406154352822.png)

这符合逻辑，先生成原始的数据，再base64编码来`addHeader`.然后因为前面分析出来的`addCommonHeader`,对其进行hook，分析调用栈，发现其栈中相同的触发点在``com.bilibili.lib.moss.internal.impl.failover.a`中151行完成的`,我们猜测可能这里是一个发送请求的地方。然后就可以看到。然后很自然的拿到数据。但是这是编码后的数据。
