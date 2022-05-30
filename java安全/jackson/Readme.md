# JackSon

> http://www.lmxspace.com/2019/07/30/Jackson-%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E6%B1%87%E6%80%BB/
>
> https://b1ue.cn/archives/189.html

## 探测

```
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.enableDefaultTyping();
String jsonResult = "[\"java.util.HashSet\",[[\"java.net.URL\",\"http://1wc3gw.dnslog.cn\"]]]";
objectMapper.readValue(jsonResult,Object.class);
["java.net.InetSocketAddress","nqigwr.dnslog.cn"]
["java.net.InetAddress","ap6d50.dnslog.cn"]
```

## 利用方式

其他payload: https://github.com/threedr3am/learnjavabug/tree/master/jackson/src/main/java/com/threedr3am/bug/jackson

1. TemplatesImpl

   http://www.lmxspace.com/2019/07/30/Jackson-%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E6%B1%87%E6%80%BB/

   ```
   https://github.com/shengqi158/Jackson-databind-RCE-PoC
   ```

2. c3p0

   http://redteam.today/2020/04/18/c3p0%E7%9A%84%E4%B8%89%E4%B8%AAgadget/

3. CVE [CVE](/src/main/java/com/dem0/cve)

## 特殊的机制-多态类型绑定
- Global default typing  
四种注解的含义
>- JAVA_LANG_OBJECT: only affects properties of type Object.class  
>
>  只影响Object.class的对象
>
>- OBJECT_AND_NON_CONCRETE: affects Object.class and all non-concrete types (abstract classes, interfaces)
>
>  影响Class 和 抽象类 接口 默认
>
>- NON_CONCRETE_AND_ARRAYS: same as above, and all array types of the same (direct elements are non-concrete types or Object.class)
>
>  上面的+上面的array
>
>- NON_FINAL: affects all types that are not declared ‘final’, and array types of non-final element types.
>
>  除了 final
JAVA_LANG_OBJECT: 会输出类的相关信息 并且在最后会还原

OBJECT_AND_NON_CONCRETE:   接口的类也被反序列化了

> ```
> //{"age":10,"name":"com.l1nk3r.jackson.l1nk3r","object":["com.l1nk3r.jackson.l1nk3r",{"length":100}],"sex":["com.l1nk3r.jackson.MySex",{"sex":0}]} 
> ```

NON_CONCRETE_AND_ARRAYS： 和描述一样

- @JsonTypeInfo 注解

  > ```
  > @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  > 只输出参数相关的内容和类什么的无关
  > @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  > 包含的类的相关信息，并会进行相关调用
  > {"name":"l1nk3r","age":100,"obj":{"@class":"com.l1nk3r.jackson.Height","h":100}}
  > @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
  > 和 id.CLASS 的区别在 @class 变成了 @c 
  > @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
  > 不携带类的名字 没有利用
  > @JsonTypeInfo(use = JsonTypeInfo.Id.COSTOM)
  > 这个得自己写解析器
  > ```

## OBJECT_AND_NON_CONCRETE 解析

根据typeid寻找反序列化

![image-20220111194949841](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220111194949841.png)

然后调用`deser.deserialize()`

![image-20220111195030212](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220111195030212.png)

![image-20220111195052313](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220111195052313.png)

然后使用

![image-20220111195114719](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220111195114719.png)

这是对于最外层的People类的。

对于sex。

![image-20220111195515087](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220111195515087.png)

调用这个方法进去，

![image-20220111195629953](https://gitee.com/ddem0/typora-pic/raw/master/images/image-20220111195629953.png)

然后经过中间的处理设置Type然后再反序列化，调用构造方法。并且再这个也是一个反射调用`setter`

