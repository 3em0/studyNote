# Day3-IO学习

## 0x01 Java FileSystem

分类:

:a:java.io : UnixFileSystem和WinNTFileSystem

但是他们因为继承了FileSystem所以可以跨平台

:b: java.nio : 底层的native

![image-20211229220651550](https://img.dem0dem0.top/images/image-20211229220651550.png)

但确实 java的fileSystem只是对于文件操作的一个封装。

## 0x02 Java NIO.2

jdk7+实现一套全新的fileSystem.基于非阻塞实现的。

:red_circle:NIO的文件操作在不同的系统的最终实现类也是不一样的，比如Mac的实现类是: `sun.nio.fs.UnixNativeDispatcher`,而Windows的实现类是`sun.nio.fs.WindowsNativeDispatcher`。

:red_circle:我们可以采取这个绕过一些只防御了java.io.FIleSystem的waf。

## 0x03 FileInputStream FileOutputStream RandomAccessFile

`RandomAccessFile` 既可以读文件又可以写文件。

其他都是尝试测试代码，主要注意可能就是每个方法的调用链子。

FileInputStream

```
private native int readBytes(byte b[], int off, int len) throws IOException;
```

![image-20211229222052337](https://img.dem0dem0.top/images/image-20211229222052337.png)

 FileOutputStream

## 0x04 FileSystemProvider

读取文件的调用链

![image-20211229222810955](https://img.dem0dem0.top/images/image-20211229222810955.png)

```
sun.nio.ch.FileChannelImpl.read
sun.nio.ch.ChannelInputStream.read
sun.nio.ch.ChannelInputStream.read
sun.nio.ch.ChannelInputStream.read
java.nio.file.Files.read
java.nio.file.Files.readAllBytes
com.anbai.sec.filesystem.FilesDemo.main
```

## 0x05 Java 文件名空字节截断漏洞

我们通过上面基础的学习可以知道，java的文件操作实际还是通过c来出列，java只是起到一个封装作用。那么和php同理，\0字符在进行处理的时候还会有截断的作用。

- 影响版本

  <1.7

- 修复

  ![image-20211229231058862](https://img.dem0dem0.top/images/image-20211229231058862.png)
