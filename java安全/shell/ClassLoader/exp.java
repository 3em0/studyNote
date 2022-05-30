URL[] urls = {new URL("http://localhost:8000/")};
URLClassLoader loader = URLClassLoader.newInstance(urls);
Class c = loader.loadClass("Testabc");
c.newInstance();
/*
3种模式
1. 不以 / 结尾  默认jar文件
2. 以 / 结尾 协议不是file 找class文件
3. 以 / 结尾 协议是file 本地找class文件
