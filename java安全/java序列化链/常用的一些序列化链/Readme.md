# 一些常用反序列化链子

> su18对于yso的链子的分析：https://su18.org/post/ysoserial-su18-4/#spring1

- [反序列化防护](https://github.com/Cryin/Paper/blob/master/%E6%B5%85%E8%B0%88Java%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E6%BC%8F%E6%B4%9E%E4%BF%AE%E5%A4%8D%E6%96%B9%E6%A1%88.md)

  > Hook resolveClass: [SerialKiller](https://github.com/ikkisoft/SerialKiller) 黑名单类型的防护 望梅止渴罢了(适用场景: `使用第三方的反序列化接口`)
  >
  > ValidatingObjectInputStream： 白名单
  >
  > 重写ObjectInputStream:[contrast-rO0](https://github.com/Contrast-Security-OSS/contrast-rO0) 轻量级的agent程序
  >
  > 使用ObjectInputFilter来校验反序列化的类 ： java9 之后开始支持
  
- [一部分xmldecoder](xmldecoder)

  > xmlstream的反序列化这个在很早之前我就已经分析过了，这里就不赘诉了。放一下exp就可以了。- 

- [C3P0](C3P0)

  > 链接池。 

- [添加了Click利用链](/Click) 

  > 这里机智的地方在于先加入INT属性，避免在序列化的时候执行命令

- [添加了Clojure](Clojure/)

  > 这的确不是一条能够人工挖掘出来的反序列化链条
  >
  > TODO: https://github.com/JackOfMostTrades/gadgetinspector
  
- [添加Rome](Rome/)

  > toString ==> 触发！**getReadMethod** ==> 触发get

- [添加Vaadin](Vaadin/)

  > 无脑嗦 感觉像是一个简化版的Rome。

- [添加了Groovy](Groovy/)

  > yso中使用PriorityQueue去触发Comparator.compare()一样的会触发ConvertedClosure.invokeCustom.
  >
  > InvocationHandler的entryset。
  
- [添加了Hibernate](Hibernate/)

  > 链子长了不起呀~~~

- [添加了spring](spring/)

  > [我只能说是动态代理界的扛把子]()

- [添加FileUpload](FileUpload/)

  > 简单的总结就是一个类吃遍天下了。
  >
  > 低版本任意写文件
  >
  > 高版本任意创建目录
  
- [AspectJWeaver](AspectJWeaver/)

  > [NonRCE?](https://mp.weixin.qq.com/s/yQ-00YaykUe41S0DdlgoiQ)

  
