# JAVA Hash碰撞

## 0x01 出处

`Objects.hashCode(str) == secret.getKey().hashCode() && !secret.getKey().equals(str)`

这里的`secret`获取一个随机的字符串，然后str也是`String`类型的，所以他们的`hashCode`函数是一致的，计算方法一致，那么按道理来说，这样的`hash`函数不应该有强碰撞性质?很难找到一个在`m1!=m2`时，`hash(m1)==hash(m2)`的情况存在。那这里是不是不能绕过呢？

## 0x02 分析

我们来查看`String.hashCode`方法，注释中已经写的很清楚了

```tex
s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
```

所以只要找到满足这个公式的两边的就行了。

不难想到的几个做法

**\0**

比如`hello`.和`\0hello`;

```
h*31^4...   和 0 * 31^5 + h* 31^4
```

第二种做法，假设我们不适用这样的方法。

**真实的碰撞**

我的想法是，最好从原字符串的高位（高位所乘的系数较小，可以直接找到）出发，比如

```
x*31+y 
```

找到一样的字符串是很简单，就可以通过变换高位来实现。

**网上的一些做法**

参考：https://stackoverflow.com/questions/12925988/how-to-generate-strings-that-share-the-same-hashcode-in-java

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HashCUtil {

    private static String[] base = new String[] {"ZVAgxVUG06nTdoaN", "ZVAgxVUG06nTdo"};

    public static List<String> generateN(int n)
    {
        if(n <= 0)
        {
            return null;
        }

        List<String> list = generateOne(null);
        for(int i = 1; i < n; ++i)
        {
            list = generateOne(list);
        }

        return list;
    }


    public static List<String> generateOne(List<String> strList)
    {   
        if((null == strList) || (0 == strList.size()))
        {
            strList = new ArrayList<String>();
            for(int i = 0; i < base.length; ++i)
            {
                strList.add(base[i]);
            }

            return strList;
        }

        List<String> result = new ArrayList<String>();

        for(int i = 0; i < base.length; ++i)
        {
            for(String str: strList)
            {   
                result.add(base[i]  + str);
            }
        }

        return result;      
    }
    public static void testN() throws Exception {
        List<String> l = HashCUtil.generateN(3);
        for(int i = 0; i < l.size(); ++i){
            System.out.println(l.get(i) + "---" + l.get(i).hashCode());
        }
    }
    public static String samehash(String s, int level) throws UnsupportedEncodingException {
        if (s.length() < 2)
            return s;
        String sub2 = s.substring(0, 2);
        char c0 = sub2.charAt(0);
        char c1 = sub2.charAt(1);
        c0 = (char) (c0 + level);
        c1 = (char) (c1 - 31 * level);
        String newsub2 = new String(new char[] { c0, c1 });
        String re =  newsub2 + s.substring(2);
        System.out.println(re.hashCode());
        System.out.println(s.hashCode());
        System.out.println(URLEncoder.encode( re, "UTF-8" ));
        if(Objects.hashCode(re) == s.hashCode() && !s.equals(re)){
            System.out.println("get");
        }
        return re;
    }

    public static void main(String[] args) throws Exception {
        HashCUtil.samehash("w66JHZm3akKM8c73",1);
    }
}
```

