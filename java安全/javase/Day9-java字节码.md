> 熬夜刷java，我爱了

https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html

# 0x01 java class文件格式

```java
ClassFile {
    u4             magic;
    u2             minor_version;
    u2             major_version;
    u2             constant_pool_count;
    cp_info        constant_pool[constant_pool_count-1];
    u2             access_flags;
    u2             this_class;
    u2             super_class;
    u2             interfaces_count;
    u2             interfaces[interfaces_count];
    u2             fields_count;
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
```

这是jvm虚拟机规范中规定的class文件的固定格式

在JVM规范中`u1`、`u2`、`u4`分别表示的是1、2、4个字节的无符号数.

首先开始`magic`是用来区分`class`文件格式的,它有固定的值`0xCAFEBABE`.接下来就是minor_version和major_version表示版本.

```tex
For a class file whose major_version is 56 or above, the minor_version must be 0 or 65535.
For a class file whose major_version is between 45 and 55 inclusive, the minor_version may be any value.
```

遵循上面这个标准.

`constant_pool_count`常量池计数器,表示常量池中的数量+1,其中long和double类型占据两个字节

`cp_info        constant_pool[constant_pool_count-1];`

其中cp_info是一种数据结构

```java
cp_info {
   u1 tag;
   u1 info[];
}
```

其中的tag表示存储的类型.

**Table 4.4-B. Constant pool tags (by tag)**

| Constant Kind                 | Tag  | `class` file format | Java SE |
| ----------------------------- | ---- | ------------------- | ------- |
| `CONSTANT_Utf8`               | 1    | 45.3                | 1.0.2   |
| `CONSTANT_Integer`            | 3    | 45.3                | 1.0.2   |
| `CONSTANT_Float`              | 4    | 45.3                | 1.0.2   |
| `CONSTANT_Long`               | 5    | 45.3                | 1.0.2   |
| `CONSTANT_Double`             | 6    | 45.3                | 1.0.2   |
| `CONSTANT_Class`              | 7    | 45.3                | 1.0.2   |
| `CONSTANT_String`             | 8    | 45.3                | 1.0.2   |
| `CONSTANT_Fieldref`           | 9    | 45.3                | 1.0.2   |
| `CONSTANT_Methodref`          | 10   | 45.3                | 1.0.2   |
| `CONSTANT_InterfaceMethodref` | 11   | 45.3                | 1.0.2   |
| `CONSTANT_NameAndType`        | 12   | 45.3                | 1.0.2   |
| `CONSTANT_MethodHandle`       | 15   | 51.0                | 7       |
| `CONSTANT_MethodType`         | 16   | 51.0                | 7       |
| `CONSTANT_Dynamic`            | 17   | 55.0                | 11      |
| `CONSTANT_InvokeDynamic`      | 18   | 51.0                | 7       |
| `CONSTANT_Module`             | 19   | 53.0                | 9       |
| `CONSTANT_Package`            | 20   | 53.0                | 9       |

`access_flags`访问标志,表示某个对象的访问权限

| 标志名         | 十六进制值 | 描述                                                   |
| -------------- | ---------- | ------------------------------------------------------ |
| ACC_PUBLIC     | 0x0001     | 声明为public                                           |
| ACC_FINAL      | 0x0010     | 声明为final                                            |
| ACC_SUPER      | 0x0020     | 废弃/仅JDK1.0.2前使用                                  |
| ACC_INTERFACE  | 0x0200     | 声明为接口                                             |
| ACC_ABSTRACT   | 0x0400     | 声明为abstract                                         |
| ACC_SYNTHETIC  | 0x1000     | 声明为synthetic，表示该class文件并非由Java源代码所生成 |
| ACC_ANNOTATION | 0x2000     | 标识注解类型                                           |
| ACC_ENUM       | 0x4000     | 标识枚举类型                                           |

`this_class`当前类的名字

`super_class`父类的名`java/lang/Object`类的`super_class`的为0,其他的都必须有一个索引.
`interfaces_count`当前类实现和继承的接口数目

`u2 interfaces[interfaces_count];`表示的是所有接口数组

`u2 fields_count;`表示的是当前class中的成员变量个数

`field_info fields[fields_count];`表示的是当前类的所有成员变量，`field_info`表示的是成员变量对象。

`filed_info`数据结构

```java
field_info {
   u2 access_flags; 表示的是成员变量的修饰符；
   u2 name_index; 表示的是成员变量的名称；
   u2 descriptor_index;表示的是成员变量的描述符；
   u2 attributes_count;表示的是成员变量的属性数量
   attribute_info attributes[attributes_count]; 表示的是成员变量的属性信息；
}
```

 `u2             methods_count;` `method_info    methods[methods_count];` 和上面的差不多相同

```
Person类中定义了3个字段 age、name、gender它们是类成员变量，但它们不全是属性；那什么是属性？
属性的定义规则是：set/get方法名，去掉set/get后，将剩余部分首字母小写得到的字符串就是这个类的属性。
```

`attribute_info attributes[attributes_count];`表示的是当前class文件的所有属性，`attribute_info`是一个非常复杂的数据结构，存储着各种属性信息。

**`attribute_info`数据结构：**

```
attribute_info {
   u2 attribute_name_index;
   u4 attribute_length;
   u1 info[attribute_length];
}
```

`u2 attribute_name_index;`表示的是属性名称索引，读取`attribute_name_index`值所在常量池中的名称可以得到属性名称。

**Java15属性表：**

| 属性名称                                       | 章节    |
| ---------------------------------------------- | ------- |
| ConstantValue Attribute                        | §4.7.2  |
| Code Attribute                                 | §4.7.3  |
| StackMapTable Attribute                        | §4.7.4  |
| Exceptions Attribute                           | §4.7.5  |
| InnerClasses Attribute                         | §4.7.6  |
| EnclosingMethod Attribute                      | §4.7.7  |
| Synthetic Attribute                            | §4.7.8  |
| Signature Attribute                            | §4.7.9  |
| SourceFile Attribute                           | §4.7.10 |
| SourceDebugExtension Attribute                 | §4.7.11 |
| LineNumberTable Attribute                      | §4.7.12 |
| LocalVariableTable Attribute                   | §4.7.13 |
| LocalVariableTypeTable Attribute               | §4.7.14 |
| Deprecated Attribute                           | §4.7.15 |
| RuntimeVisibleAnnotations Attribute            | §4.7.16 |
| RuntimeInvisibleAnnotations Attribute          | §4.7.17 |
| RuntimeVisibleParameterAnnotations Attribute   | §4.7.18 |
| RuntimeInvisibleParameterAnnotations Attribute | §4.7.19 |
| RuntimeVisibleTypeAnnotations Attribute        | §4.7.20 |
| RuntimeInvisibleTypeAnnotations Attribute      | §4.7.21 |
| AnnotationDefault Attribute                    | §4.7.22 |
| BootstrapMethods Attribute                     | §4.7.23 |
| MethodParameters Attribute                     | §4.7.24 |
| Module Attribute                               | §4.7.25 |
| ModulePackages Attribute                       | §4.7.26 |
| ModuleMainClass Attribute                      | §4.7.27 |
| NestHost Attribute                             | §4.7.28 |
| NestMembers Attribute                          | §4.7.29 |

`属性对象`

属性表是动态的，新的JDK版本可能会添加新的属性值。每一种属性的数据结构都不相同，所以读取到属性名称后还需要根据属性的类型解析不同属性表中的值。比如`Code Attribute`中存储了类方法的异常表、字节码指令集、属性信息等重要信息。

# 0x02 ClassByteCodeParser

 解析魔数

```java
this.dis = new DataInputStream(in);
// u4 magic;
int magic = dis.readInt();
// 校验文件魔数
if (0xCAFEBABE == magic) {
this.magic = magic;
```

解析版本

```java
// u2 minor_version
this.minor = dis.readUnsignedShort();

// u2 major_version;
this.major = dis.readUnsignedShort();
```

解析常量池`parseConstantPool();`方法实现

```java
u2             constant_pool_count;
cp_info        constant_pool[constant_pool_count-1];
```

分为下面几步

- 读取常量池的数量`u2             constant_pool_count;`

  ```
  cp_info {
     u1 tag;
     u1 info[];
  }
  ```

- 读取tag，根据不同的tag进行解析

- 解析常量池中的对象

- 链接常量池中的对象

```java
	private void parseConstantPool() throws IOException {
		// u2 constant_pool_count;
		this.poolCount = dis.readUnsignedShort();

		// cp_info constant_pool[constant_pool_count-1];
		for (int i = 1; i <= poolCount - 1; i++) {
//          cp_info {
//              u1 tag;
//              u1 info[];
//          }

			int      tag      = dis.readUnsignedByte();
			Constant constant = Constant.getConstant(tag);

			if (constant == null) {
				throw new RuntimeException("解析常量池异常，无法识别的常量池类型：" + tag);
			}

			// 解析常量池对象
			parseConstantItems(constant, i);

			// Long和Double是宽类型，占两位
			if (CONSTANT_LONG == constant || CONSTANT_DOUBLE == constant) {
				i++;
			}
		}

		// 链接常量池中的引用
		linkConstantPool();
	}

```

处理

```
private void parseConstantItems(Constant constant, int index) throws IOException {
		Map<String, Object> map = new LinkedHashMap<>();

		switch (constant) {
			case CONSTANT_UTF8:
//                  CONSTANT_Utf8_info {
//                      u1 tag;
//                      u2 length;
//                      u1 bytes[length];
//                  }

				int length = dis.readUnsignedShort();
				byte[] bytes = new byte[length];
				dis.read(bytes);

				map.put("tag", CONSTANT_UTF8);
				map.put("value", new String(bytes, UTF_8));
				break;
			case CONSTANT_INTEGER:
//                  CONSTANT_Integer_info {
//                      u1 tag;
//                      u4 bytes;
//                  }

				map.put("tag", CONSTANT_INTEGER);
				map.put("value", dis.readInt());
				break;
			case CONSTANT_FLOAT:
//                  CONSTANT_Float_info {
//                      u1 tag;
//                      u4 bytes;
//                  }

				map.put("tag", CONSTANT_FLOAT);
				map.put("value", dis.readFloat());
				break;
			case CONSTANT_LONG:
//                  CONSTANT_Long_info {
//                      u1 tag;
//                      u4 high_bytes;
//                      u4 low_bytes;
//                  }

				map.put("tag", CONSTANT_LONG);
				map.put("value", dis.readLong());
				break;
			case CONSTANT_DOUBLE:
//                  CONSTANT_Double_info {
//                      u1 tag;
//                      u4 high_bytes;
//                      u4 low_bytes;
//                  }

				map.put("tag", CONSTANT_DOUBLE);
				map.put("value", dis.readDouble());
				break;
			case CONSTANT_CLASS:
//                  CONSTANT_Class_info {
//                      u1 tag;
//                      u2 name_index;
//                  }

				map.put("tag", CONSTANT_CLASS);
				map.put("nameIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_STRING:
//                  CONSTANT_String_info {
//                      u1 tag;
//                      u2 string_index;
//                  }

				map.put("tag", CONSTANT_STRING);
				map.put("stringIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_FIELD_REF:
//                  CONSTANT_Fieldref_info {
//                      u1 tag;
//                      u2 class_index;
//                      u2 name_and_type_index;
//                  }

				map.put("tag", CONSTANT_FIELD_REF);
				map.put("classIndex", dis.readUnsignedShort());
				map.put("nameAndTypeIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_METHOD_REF:
//                  CONSTANT_Methodref_info {
//                      u1 tag;
//                      u2 class_index;
//                      u2 name_and_type_index;
//                  }

				map.put("tag", CONSTANT_METHOD_REF);
				map.put("classIndex", dis.readUnsignedShort());
				map.put("nameAndTypeIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_INTERFACE_METHOD_REF:
//                  CONSTANT_InterfaceMethodref_info {
//                      u1 tag;
//                      u2 class_index;
//                      u2 name_and_type_index;
//                  }

				map.put("tag", CONSTANT_INTERFACE_METHOD_REF);
				map.put("classIndex", dis.readUnsignedShort());
				map.put("nameAndTypeIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_NAME_AND_TYPE:
//                  CONSTANT_NameAndType_info {
//                      u1 tag;
//                      u2 name_index;
//                      u2 descriptor_index;
//                  }

				map.put("tag", CONSTANT_NAME_AND_TYPE);
				map.put("nameIndex", dis.readUnsignedShort());
				map.put("descriptorIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_METHOD_HANDLE:
//                  CONSTANT_MethodHandle_info {
//                      u1 tag;
//                      u1 reference_kind;
//                      u2 reference_index;
//                  }

				map.put("tag", CONSTANT_METHOD_HANDLE);
				map.put("referenceKind", dis.readUnsignedByte());
				map.put("referenceIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_METHOD_TYPE:
//                  CONSTANT_MethodType_info {
//                      u1 tag;
//                      u2 descriptor_index;
//                  }

				map.put("tag", CONSTANT_METHOD_TYPE);
				map.put("descriptorIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_DYNAMIC:
//                  CONSTANT_Dynamic_info {
//                      u1 tag;
//                      u2 bootstrap_method_attr_index;
//                      u2 name_and_type_index;
//                  }

				map.put("tag", CONSTANT_DYNAMIC);
				map.put("bootstrapMethodAttrIdx", dis.readUnsignedShort());
				map.put("nameAndTypeIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_INVOKE_DYNAMIC:
//                  CONSTANT_InvokeDynamic_info {
//                      u1 tag;
//                      u2 bootstrap_method_attr_index;
//                      u2 name_and_type_index;
//                  }

				map.put("tag", CONSTANT_INVOKE_DYNAMIC);
				map.put("bootstrapMethodAttrIdx", dis.readUnsignedShort());
				map.put("nameAndTypeIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_MODULE:
//                  CONSTANT_Module_info {
//                      u1 tag;
//                      u2 name_index;
//                  }

				map.put("tag", CONSTANT_MODULE);
				map.put("nameIndex", dis.readUnsignedShort());
				break;
			case CONSTANT_PACKAGE:
//                  CONSTANT_Package_info {
//                      u1 tag;
//                      u2 name_index;
//                  }

				map.put("tag", CONSTANT_PACKAGE);
				map.put("nameIndex", dis.readUnsignedShort());
				break;
		}

		constantPoolMap.put(index, map);
	}
```

最后的处理链接是因为:常量池很多存储了对于其他变量的引用，需要对他进行解析。

,为了能够直观的看到常量池ID为1的对象信息我们就必须要将所有使用索引方式链接的映射关系改成直接字符串引用。

```java
	private void linkConstantPool() {
		for (Integer id : constantPoolMap.keySet()) {
			Map<String, Object> valueMap = constantPoolMap.get(id);

			if (!valueMap.containsKey("value")) {
				Map<String, Object> newMap = new LinkedHashMap<>();

				for (String key : valueMap.keySet()) {
					if (key.endsWith("Index")) {
						Object value = recursionValue((Integer) valueMap.get(key));

						if (value != null) {
							String newKey = key.substring(0, key.indexOf("Index"));

							newMap.put(newKey + "Value", value);
						}
					}
				}

				valueMap.putAll(newMap);
			}
```

```java
/**
 * 通过常量池中的索引ID和名称获取常量池中的值
 *
 * @param index 索引ID
 * @return 常量池对象值
 */
private Object getConstantPoolValue(int index) {
     if (constantPoolMap.containsKey(index)) {
        Map<String, Object> dataMap  = constantPoolMap.get(index);
        Constant            constant = (Constant) dataMap.get("tag");

        switch (constant) {
           case CONSTANT_UTF8:
           case CONSTANT_INTEGER:
           case CONSTANT_FLOAT:
           case CONSTANT_LONG:
           case CONSTANT_DOUBLE:
              return dataMap.get("value");
           case CONSTANT_CLASS:
           case CONSTANT_MODULE:
           case CONSTANT_PACKAGE:
              return dataMap.get("nameValue");
           case CONSTANT_STRING:
              return dataMap.get("stringValue");
           case CONSTANT_FIELD_REF:
           case CONSTANT_METHOD_REF:
           case CONSTANT_INTERFACE_METHOD_REF:
              return dataMap.get("classValue") + "." + dataMap.get("nameAndTypeValue");
           case CONSTANT_NAME_AND_TYPE:
           case CONSTANT_METHOD_TYPE:
              return dataMap.get("descriptorValue");
           case CONSTANT_METHOD_HANDLE:
              return dataMap.get("referenceValue");
           case CONSTANT_DYNAMIC:
           case CONSTANT_INVOKE_DYNAMIC:
              return dataMap.get("bootstrapMethodAttrValue") + "." + dataMap.get("nameAndTypeValue");
           default:
              break;
        }
     }

     return null;
}
```

后面这个方法是为了方便直接获取常量池中的数据而封装的。

- 访问标志

  ```java
  this.accessFlags = dis.readUnsignedShort();
  ```

- 类名称

  ```
  this.thisClass = (String) getConstantPoolValue(dis.readUnsignedShort());
  ```

- 类的父类名称解析

  注意index=0的时候，Obejct没有父类

  ```java
  int superClassIndex = dis.readUnsignedShort();
  // 当解析Object类的时候super_class为0
  if (superClassIndex != 0) {
     this.superClass = (String) getConstantPoolValue(superClassIndex);
  } else {
     this.superClass = "java/lang/Object";
  }
  ```

- 接口解析

  ```
  u2 interfaces_count;
  u2 interfaces[interfaces_count];
  ```

  ```java
  // u2 interfaces_count;
  this.interfacesCount = dis.readUnsignedShort();
  
  // 创建接口Index数组
  this.interfaces = new String[interfacesCount];
  
  // u2 interfaces[interfaces_count];
  for (int i = 0; i < interfacesCount; i++) {
      int index = dis.readUnsignedShort();
  
      // 设置接口名称
      this.interfaces[i] = (String) getConstantPoolValue(index);
  }
  ```

- 变量/成员方法解析

  ```java
  // u2 fields_count;
  this.fieldsCount = dis.readUnsignedShort();
  
  // field_info fields[fields_count];
  for (int i = 0; i < this.fieldsCount; i++) {
      //        field_info {
      //          u2 access_flags;
      //          u2 name_index;
      //          u2 descriptor_index;
      //          u2 attributes_count;
      //          attribute_info attributes[attributes_count];
      //        }
      this.fieldList.add(readFieldOrMethod());
  }
  ```

- 属性解析

  https://zhishihezi.net/endpoint/richtext/43a3ddd294c129cc60389fc491d44c38?event=436b34f44b9f95fd3aa8667f1ad451b173526ab5441d9f64bd62d183bed109b0ea1aaaa23c5207a446fa6de9f588db3958e8cd5c825d7d5216199d64338d9d00c167a590fe7993863a9dc2252cd392e842bc8a14c5c53993a3fc9f72b0aeb13fb587c648e7046c336d61878438df1249f87cbe30a7202f8bf6ab9bcc58ad1c5e0381bc6e50e0cf0c7c805c09000f5f0c80c800e88fe4766e244ea31a9e07b2639c7c8884959340bf9b5975c577ce6243c8a09419cb9eb6183d1456258bf87328787fb1fb85c348d420a1d6b09f7d3f0815853c717a7ed1675dc36adddb8da53542c004db542e66a1a33c2ab4c554576589a16ed6dca72e89cf74091a89180688#0

  直接参考文章。

  `Code`属性用于表示成员方法的代码部分，`Code`中包含了指令集（`byte数组`），JVM调用成员方法时实际上就是执行的`Code`中的指令，而反编译工具则是把`Code`中的指令翻译成了Java代码

# 0x03  Java虚拟机指令集

https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-6.html#jvms-6.5

速查

**Java虚拟机指令表**

| 十六进制 | 助记符          | 指令说明                                                     |
| -------- | --------------- | ------------------------------------------------------------ |
| 0x00     | nop             | 什么都不做                                                   |
| 0x01     | aconst_null     | 将null推送至栈顶                                             |
| 0x02     | iconst_m1       | 将int型-1推送至栈顶                                          |
| 0x03     | iconst_0        | 将int型0推送至栈顶                                           |
| 0x04     | iconst_1        | 将int型1推送至栈顶                                           |
| 0x05     | iconst_2        | 将int型2推送至栈顶                                           |
| 0x06     | iconst_3        | 将int型3推送至栈顶                                           |
| 0x07     | iconst_4        | 将int型4推送至栈顶                                           |
| 0x08     | iconst_5        | 将int型5推送至栈顶                                           |
| 0x09     | lconst_0        | 将long型0推送至栈顶                                          |
| 0x0a     | lconst_1        | 将long型1推送至栈顶                                          |
| 0x0b     | fconst_0        | 将float型0推送至栈顶                                         |
| 0x0c     | fconst_1        | 将float型1推送至栈顶                                         |
| 0x0d     | fconst_2        | 将float型2推送至栈顶                                         |
| 0x0e     | dconst_0        | 将double型0推送至栈顶                                        |
| 0x0f     | dconst_1        | 将double型1推送至栈顶                                        |
| 0x10     | bipush          | 将单字节的常量值(-128~127)推送至栈顶                         |
| 0x11     | sipush          | 将一个短整型常量值(-32768~32767)推送至栈顶                   |
| 0x12     | ldc             | 将int, float或String型常量值从常量池中推送至栈顶             |
| 0x13     | ldc_w           | 将int, float或String型常量值从常量池中推送至栈顶（宽索引）   |
| 0x14     | ldc2_w          | 将long或double型常量值从常量池中推送至栈顶（宽索引）         |
| 0x15     | iload           | 将指定的int型本地变量推送至栈顶                              |
| 0x16     | lload           | 将指定的long型本地变量推送至栈顶                             |
| 0x17     | fload           | 将指定的float型本地变量推送至栈顶                            |
| 0x18     | dload           | 将指定的double型本地变量推送至栈顶                           |
| 0x19     | aload           | 将指定的引用类型本地变量推送至栈顶                           |
| 0x1a     | iload_0         | 将第一个int型本地变量推送至栈顶                              |
| 0x1b     | iload_1         | 将第二个int型本地变量推送至栈顶                              |
| 0x1c     | iload_2         | 将第三个int型本地变量推送至栈顶                              |
| 0x1d     | iload_3         | 将第四个int型本地变量推送至栈顶                              |
| 0x1e     | lload_0         | 将第一个long型本地变量推送至栈顶                             |
| 0x1f     | lload_1         | 将第二个long型本地变量推送至栈顶                             |
| 0x20     | lload_2         | 将第三个long型本地变量推送至栈顶                             |
| 0x21     | lload_3         | 将第四个long型本地变量推送至栈顶                             |
| 0x22     | fload_0         | 将第一个float型本地变量推送至栈顶                            |
| 0x23     | fload_1         | 将第二个float型本地变量推送至栈顶                            |
| 0x24     | fload_2         | 将第三个float型本地变量推送至栈顶                            |
| 0x25     | fload_3         | 将第四个float型本地变量推送至栈顶                            |
| 0x26     | dload_0         | 将第一个double型本地变量推送至栈顶                           |
| 0x27     | dload_1         | 将第二个double型本地变量推送至栈顶                           |
| 0x28     | dload_2         | 将第三个double型本地变量推送至栈顶                           |
| 0x29     | dload_3         | 将第四个double型本地变量推送至栈顶                           |
| 0x2a     | aload_0         | 将第一个引用类型本地变量推送至栈顶                           |
| 0x2b     | aload_1         | 将第二个引用类型本地变量推送至栈顶                           |
| 0x2c     | aload_2         | 将第三个引用类型本地变量推送至栈顶                           |
| 0x2d     | aload_3         | 将第四个引用类型本地变量推送至栈顶                           |
| 0x2e     | iaload          | 将int型数组指定索引的值推送至栈顶                            |
| 0x2f     | laload          | 将long型数组指定索引的值推送至栈顶                           |
| 0x30     | faload          | 将float型数组指定索引的值推送至栈顶                          |
| 0x31     | daload          | 将double型数组指定索引的值推送至栈顶                         |
| 0x32     | aaload          | 将引用型数组指定索引的值推送至栈顶                           |
| 0x33     | baload          | 将boolean或byte型数组指定索引的值推送至栈顶                  |
| 0x34     | caload          | 将char型数组指定索引的值推送至栈顶                           |
| 0x35     | saload          | 将short型数组指定索引的值推送至栈顶                          |
| 0x36     | istore          | 将栈顶int型数值存入指定本地变量                              |
| 0x37     | lstore          | 将栈顶long型数值存入指定本地变量                             |
| 0x38     | fstore          | 将栈顶float型数值存入指定本地变量                            |
| 0x39     | dstore          | 将栈顶double型数值存入指定本地变量                           |
| 0x3a     | astore          | 将栈顶引用型数值存入指定本地变量                             |
| 0x3b     | istore_0        | 将栈顶int型数值存入第一个本地变量                            |
| 0x3c     | istore_1        | 将栈顶int型数值存入第二个本地变量                            |
| 0x3d     | istore_2        | 将栈顶int型数值存入第三个本地变量                            |
| 0x3e     | istore_3        | 将栈顶int型数值存入第四个本地变量                            |
| 0x3f     | lstore_0        | 将栈顶long型数值存入第一个本地变量                           |
| 0x40     | lstore_1        | 将栈顶long型数值存入第二个本地变量                           |
| 0x41     | lstore_2        | 将栈顶long型数值存入第三个本地变量                           |
| 0x42     | lstore_3        | 将栈顶long型数值存入第四个本地变量                           |
| 0x43     | fstore_0        | 将栈顶float型数值存入第一个本地变量                          |
| 0x44     | fstore_1        | 将栈顶float型数值存入第二个本地变量                          |
| 0x45     | fstore_2        | 将栈顶float型数值存入第三个本地变量                          |
| 0x46     | fstore_3        | 将栈顶float型数值存入第四个本地变量                          |
| 0x47     | dstore_0        | 将栈顶double型数值存入第一个本地变量                         |
| 0x48     | dstore_1        | 将栈顶double型数值存入第二个本地变量                         |
| 0x49     | dstore_2        | 将栈顶double型数值存入第三个本地变量                         |
| 0x4a     | dstore_3        | 将栈顶double型数值存入第四个本地变量                         |
| 0x4b     | astore_0        | 将栈顶引用型数值存入第一个本地变量                           |
| 0x4c     | astore_1        | 将栈顶引用型数值存入第二个本地变量                           |
| 0x4d     | astore_2        | 将栈顶引用型数值存入第三个本地变量                           |
| 0x4e     | astore_3        | 将栈顶引用型数值存入第四个本地变量                           |
| 0x4f     | iastore         | 将栈顶int型数值存入指定数组的指定索引位置                    |
| 0x50     | lastore         | 将栈顶long型数值存入指定数组的指定索引位置                   |
| 0x51     | fastore         | 将栈顶float型数值存入指定数组的指定索引位置                  |
| 0x52     | dastore         | 将栈顶double型数值存入指定数组的指定索引位置                 |
| 0x53     | aastore         | 将栈顶引用型数值存入指定数组的指定索引位置                   |
| 0x54     | bastore         | 将栈顶boolean或byte型数值存入指定数组的指定索引位置          |
| 0x55     | castore         | 将栈顶char型数值存入指定数组的指定索引位置                   |
| 0x56     | sastore         | 将栈顶short型数值存入指定数组的指定索引位置                  |
| 0x57     | pop             | 将栈顶数值弹出 (数值不能是long或double类型的)                |
| 0x58     | pop2            | 将栈顶的一个（long或double类型的)或两个数值弹出（其它）      |
| 0x59     | dup             | 复制栈顶数值并将复制值压入栈顶                               |
| 0x5a     | dup_x1          | 复制栈顶数值并将两个复制值压入栈顶                           |
| 0x5b     | dup_x2          | 复制栈顶数值并将三个（或两个）复制值压入栈顶                 |
| 0x5c     | dup2            | 复制栈顶一个（long或double类型的)或两个（其它）数值并将复制值压入栈顶 |
| 0x5d     | dup2_x1         | <待补充>                                                     |
| 0x5e     | dup2_x2         | <待补充>                                                     |
| 0x5f     | swap            | 将栈最顶端的两个数值互换(数值不能是long或double类型的)       |
| 0x60     | iadd            | 将栈顶两int型数值相加并将结果压入栈顶                        |
| 0x61     | ladd            | 将栈顶两long型数值相加并将结果压入栈顶                       |
| 0x62     | fadd            | 将栈顶两float型数值相加并将结果压入栈顶                      |
| 0x63     | dadd            | 将栈顶两double型数值相加并将结果压入栈顶                     |
| 0x64     | isub            | 将栈顶两int型数值相减并将结果压入栈顶                        |
| 0x65     | lsub            | 将栈顶两long型数值相减并将结果压入栈顶                       |
| 0x66     | fsub            | 将栈顶两float型数值相减并将结果压入栈顶                      |
| 0x67     | dsub            | 将栈顶两double型数值相减并将结果压入栈顶                     |
| 0x68     | imul            | 将栈顶两int型数值相乘并将结果压入栈顶                        |
| 0x69     | lmul            | 将栈顶两long型数值相乘并将结果压入栈顶                       |
| 0x6a     | fmul            | 将栈顶两float型数值相乘并将结果压入栈顶                      |
| 0x6b     | dmul            | 将栈顶两double型数值相乘并将结果压入栈顶                     |
| 0x6c     | idiv            | 将栈顶两int型数值相除并将结果压入栈顶                        |
| 0x6d     | ldiv            | 将栈顶两long型数值相除并将结果压入栈顶                       |
| 0x6e     | fdiv            | 将栈顶两float型数值相除并将结果压入栈顶                      |
| 0x6f     | ddiv            | 将栈顶两double型数值相除并将结果压入栈顶                     |
| 0x70     | irem            | 将栈顶两int型数值作取模运算并将结果压入栈顶                  |
| 0x71     | lrem            | 将栈顶两long型数值作取模运算并将结果压入栈顶                 |
| 0x72     | frem            | 将栈顶两float型数值作取模运算并将结果压入栈顶                |
| 0x73     | drem            | 将栈顶两double型数值作取模运算并将结果压入栈顶               |
| 0x74     | ineg            | 将栈顶int型数值取负并将结果压入栈顶                          |
| 0x75     | lneg            | 将栈顶long型数值取负并将结果压入栈顶                         |
| 0x76     | fneg            | 将栈顶float型数值取负并将结果压入栈顶                        |
| 0x77     | dneg            | 将栈顶double型数值取负并将结果压入栈顶                       |
| 0x78     | ishl            | 将int型数值左移位指定位数并将结果压入栈顶                    |
| 0x79     | lshl            | 将long型数值左移位指定位数并将结果压入栈顶                   |
| 0x7a     | ishr            | 将int型数值右（符号）移位指定位数并将结果压入栈顶            |
| 0x7b     | lshr            | 将long型数值右（符号）移位指定位数并将结果压入栈顶           |
| 0x7c     | iushr           | 将int型数值右（无符号）移位指定位数并将结果压入栈顶          |
| 0x7d     | lushr           | 将long型数值右（无符号）移位指定位数并将结果压入栈顶         |
| 0x7e     | iand            | 将栈顶两int型数值作“按位与”并将结果压入栈顶                  |
| 0x7f     | land            | 将栈顶两long型数值作“按位与”并将结果压入栈顶                 |
| 0x80     | ior             | 将栈顶两int型数值作“按位或”并将结果压入栈顶                  |
| 0x81     | lor             | 将栈顶两long型数值作“按位或”并将结果压入栈顶                 |
| 0x82     | ixor            | 将栈顶两int型数值作“按位异或”并将结果压入栈顶                |
| 0x83     | lxor            | 将栈顶两long型数值作“按位异或”并将结果压入栈顶               |
| 0x84     | iinc            | 将指定int型变量增加指定值（i++, i--, i+=2）                  |
| 0x85     | i2l             | 将栈顶int型数值强制转换成long型数值并将结果压入栈顶          |
| 0x86     | i2f             | 将栈顶int型数值强制转换成float型数值并将结果压入栈顶         |
| 0x87     | i2d             | 将栈顶int型数值强制转换成double型数值并将结果压入栈顶        |
| 0x88     | l2i             | 将栈顶long型数值强制转换成int型数值并将结果压入栈顶          |
| 0x89     | l2f             | 将栈顶long型数值强制转换成float型数值并将结果压入栈顶        |
| 0x8a     | l2d             | 将栈顶long型数值强制转换成double型数值并将结果压入栈顶       |
| 0x8b     | f2i             | 将栈顶float型数值强制转换成int型数值并将结果压入栈顶         |
| 0x8c     | f2l             | 将栈顶float型数值强制转换成long型数值并将结果压入栈顶        |
| 0x8d     | f2d             | 将栈顶float型数值强制转换成double型数值并将结果压入栈顶      |
| 0x8e     | d2i             | 将栈顶double型数值强制转换成int型数值并将结果压入栈顶        |
| 0x8f     | d2l             | 将栈顶double型数值强制转换成long型数值并将结果压入栈顶       |
| 0x90     | d2f             | 将栈顶double型数值强制转换成float型数值并将结果压入栈顶      |
| 0x91     | i2b             | 将栈顶int型数值强制转换成byte型数值并将结果压入栈顶          |
| 0x92     | i2c             | 将栈顶int型数值强制转换成char型数值并将结果压入栈顶          |
| 0x93     | i2s             | 将栈顶int型数值强制转换成short型数值并将结果压入栈顶         |
| 0x94     | lcmp            | 比较栈顶两long型数值大小，并将结果（1，0，-1）压入栈顶       |
| 0x95     | fcmpl           | 比较栈顶两float型数值大小，并将结果（1，0，-1）压入栈顶；当其中一个数值为NaN时，将-1压入栈顶 |
| 0x96     | fcmpg           | 比较栈顶两float型数值大小，并将结果（1，0，-1）压入栈顶；当其中一个数值为NaN时，将1压入栈顶 |
| 0x97     | dcmpl           | 比较栈顶两double型数值大小，并将结果（1，0，-1）压入栈顶；当其中一个数值为NaN时，将-1压入栈顶 |
| 0x98     | dcmpg           | 比较栈顶两double型数值大小，并将结果（1，0，-1）压入栈顶；当其中一个数值为NaN时，将1压入栈顶 |
| 0x99     | ifeq            | 当栈顶int型数值等于0时跳转                                   |
| 0x9a     | ifne            | 当栈顶int型数值不等于0时跳转                                 |
| 0x9b     | iflt            | 当栈顶int型数值小于0时跳转                                   |
| 0x9c     | ifge            | 当栈顶int型数值大于等于0时跳转                               |
| 0x9d     | ifgt            | 当栈顶int型数值大于0时跳转                                   |
| 0x9e     | ifle            | 当栈顶int型数值小于等于0时跳转                               |
| 0x9f     | if_icmpeq       | 比较栈顶两int型数值大小，当结果等于0时跳转                   |
| 0xa0     | if_icmpne       | 比较栈顶两int型数值大小，当结果不等于0时跳转                 |
| 0xa1     | if_icmplt       | 比较栈顶两int型数值大小，当结果小于0时跳转                   |
| 0xa2     | if_icmpge       | 比较栈顶两int型数值大小，当结果大于等于0时跳转               |
| 0xa3     | if_icmpgt       | 比较栈顶两int型数值大小，当结果大于0时跳转                   |
| 0xa4     | if_icmple       | 比较栈顶两int型数值大小，当结果小于等于0时跳转               |
| 0xa5     | if_acmpeq       | 比较栈顶两引用型数值，当结果相等时跳转                       |
| 0xa6     | if_acmpne       | 比较栈顶两引用型数值，当结果不相等时跳转                     |
| 0xa7     | goto            | 无条件跳转                                                   |
| 0xa8     | jsr             | 跳转至指定16位offset位置，并将jsr下一条指令地址压入栈顶      |
| 0xa9     | ret             | 返回至本地变量指定的index的指令位置（一般与jsr, jsr_w联合使用） |
| 0xaa     | tableswitch     | 用于switch条件跳转，case值连续（可变长度指令）               |
| 0xab     | lookupswitch    | 用于switch条件跳转，case值不连续（可变长度指令）             |
| 0xac     | ireturn         | 从当前方法返回int                                            |
| 0xad     | lreturn         | 从当前方法返回long                                           |
| 0xae     | freturn         | 从当前方法返回float                                          |
| 0xaf     | dreturn         | 从当前方法返回double                                         |
| 0xb0     | areturn         | 从当前方法返回对象引用                                       |
| 0xb1     | return          | 从当前方法返回void                                           |
| 0xb2     | getstatic       | 获取指定类的静态域，并将其值压入栈顶                         |
| 0xb3     | putstatic       | 为指定的类的静态域赋值                                       |
| 0xb4     | getfield        | 获取指定类的实例域，并将其值压入栈顶                         |
| 0xb5     | putfield        | 为指定的类的实例域赋值                                       |
| 0xb6     | invokevirtual   | 调用实例方法                                                 |
| 0xb7     | invokespecial   | 调用超类构造方法，实例初始化方法，私有方法                   |
| 0xb8     | invokestatic    | 调用静态方法                                                 |
| 0xb9     | invokeinterface | 调用接口方法                                                 |
| 0xba     | --              |                                                              |
| 0xbb     | new             | 创建一个对象，并将其引用值压入栈顶                           |
| 0xbc     | newarray        | 创建一个指定原始类型（如int, float, char…）的数组，并将其引用值压入栈顶 |
| 0xbd     | anewarray       | 创建一个引用型（如类，接口，数组）的数组，并将其引用值压入栈顶 |
| 0xbe     | arraylength     | 获得数组的长度值并压入栈顶                                   |
| 0xbf     | athrow          | 将栈顶的异常抛出                                             |
| 0xc0     | checkcast       | 检验类型转换，检验未通过将抛出ClassCastException             |
| 0xc1     | instanceof      | 检验对象是否是指定的类的实例，如果是将1压入栈顶，否则将0压入栈顶 |
| 0xc2     | monitorenter    | 获得对象的锁，用于同步方法或同步块                           |
| 0xc3     | monitorexit     | 释放对象的锁，用于同步方法或同步块                           |
| 0xc4     | wide            | <待补充>                                                     |
| 0xc5     | multianewarray  | 创建指定类型和指定维度的多维数组（执行该指令时，操作栈中必须包含各维度的长度值），并将其引用值压入栈顶 |
| 0xc6     | ifnull          | 为null时跳转                                                 |
| 0xc7     | ifnonnull       | 不为null时跳转                                               |
| 0xc8     | goto_w          | 无条件跳转（宽索引）                                         |
| 0xc9     | jsr_w           | 跳转至指定32位offset位置，并将jsr_w下一条指令地址压入栈顶    |

# 0x04 java类字节码编辑-ASM

https://asm.ow2.io/asm4-guide.pdf

## MethodVisitor和AdviceAdapter

- `AdviceAdapter`类实现了一些非常有价值的方法，如：`onMethodEnter`（方法进入时回调方法）、`onMethodExit`（方法退出时回调方法），如果我们自己实现很容易掉进坑里面，因为这两个方法都是根据条件推算出来的。比如我们如果在构造方法的第一行直接插入了我们自己的字节码就可能会发现程序一运行就会崩溃，因为Java语法中限制我们第一行代码必须是`super(xxx)`。

- 使用`AdviceAdapter`可以直接调用`mv.newLocal(type)`计算出本地变量存储的位置，为我们省去了许多不必要的麻烦。

这样就通过asm实现了遍历一个类的基础信息

注意依赖

```java
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
```



```java
public class ASMVisitorTest {
    public static void main(String[] args) throws IOException {
        String className="com.anbai.sec.bytecode.TestHelloWorld";
        //创建classReader对象，用来解析类对象 可以传入 类名 二进制 输入流
        ClassReader cr = new ClassReader(className);
        System.out.println(
                "解析的类名: " + cr.getClassName() + ", 父类: " + cr.getSuperName() +
                        ",实现的接口: " + Arrays.toString(cr.getInterfaces())
        );
        System.out.println("-----------------------------------------------------------------------------");
        //使用自定义的ClassVisitor访问这对象，访问该类文件的结构
        cr.accept(new ClassVisitor(ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                System.out.println(
                        "变量描述符: " + access + "\t 类名:" + name + "\t 父类名: " + superName +
                                "\t 实现的接口:" + Arrays.toString(interfaces)
                );
                System.out.println("-----------------------------------------------------------------------------");
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                System.out.println(
                        "变量描述符: " + access + "\t 变量名称:" + name + "\t 描述符:" + descriptor+ "\t 默认值:"+ value
                );
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                System.out.println(
                        "变量描述符: " + access + "\t 方法名称:" + name + "\t 描述符:" + descriptor +
                                "抛出的异常： " + Arrays.toString(exceptions)
                );
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        },EXPAND_FRAMES);//该标识用于设置扩展栈帧图。默认栈图以它们原始格式（V1_6以下使用扩展格式，其他使用压缩格式）被访问
    }
}
```

## 修改类名/方法名称/描述符号

注意修改了类和及其属性之后，需要重新计算`max_stack`和`max_locals`，但是如果手撸的化，一般都会撸脱皮，所以我们可以使用它自带的方法来达到效果. `COMPUTE_FRAMES`

```java
package com.anbai.sec.bytecode.asm;


import org.apache.commons.io.FileUtils;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;

public class ASMWriterTest {
    public static void main(String[] args) throws IOException {
        String className="com.anbai.sec.bytecode.TestHelloWorld";
        final String newclassName="JavaAsmTest";

        ClassReader cr = new ClassReader(className);
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES);
        cr.accept(new ClassVisitor(ASM9,cw) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if(name.equals("hello")){
                    access = access & ~ACC_PUBLIC | ACC_PRIVATE;//如果是public属性，就改成private
                    return super.visitMethod(access, "hi", descriptor, signature, exceptions);//将hello方法改成了hi
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        },ClassReader.EXPAND_FRAMES);
        File classFilePath = new File(new File(System.getProperty("user.dir"), "javaweb-sec-source/javase/src/main/java/com/anbai/sec/bytecode/asm/"), newclassName + ".class");
        byte[] classBytes = cw.toByteArray();
        FileUtils.writeByteArrayToFile(classFilePath,classBytes);
    }
}
```

```
private String hi(String content) {
        String str = "Hello:";
        return str + content;
    }
```

## 修改类方法字节码

利用场景: 

- RASP: 需要在java底层api方法执行之前插入自身的检测代码，实现动态拦截恶意攻击
- APM: 统计代码的执行时间

https://www.cnblogs.com/beansoft/p/15495933.html 可以通过idea的插件来操作，可以说是yyds了!.

![image-20220107112225134](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220107112225134.png)

用插件之后，就可以缺啥补啥了，来构建新的方法，生成全新的class文件。

# 0x05 java类字节码编辑-Javassist

## API和特殊标识符

| 类            | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| ClassPool     | ClassPool是一个存储CtClass的容器，如果调用`get`方法会搜索并创建一个表示该类的CtClass对象 |
| CtClass       | CtClass表示的是从ClassPool获取的类对象，可对该类就行读写编辑等操作 |
| CtMethod      | 可读写的类方法对象                                           |
| CtConstructor | 可读写的类构造方法对象                                       |
| CtField       | 可读写的类成员变量对象                                       |

`Javassist`使用了内置的标识符来表示一些特定的含义，如：`$_`表示返回值。我们可以在动态插入类代码的时候使用这些特殊的标识符来表示对应的对象。

| 表达式            | 描述                                      |
| ----------------- | ----------------------------------------- |
| `$0, $1, $2, ...` | `this`和方法参数                          |
| `$args`           | `Object[]`类型的参数数组                  |
| `$$`              | 所有的参数，如`m($$)`等价于`m($1,$2,...)` |
| `$cflow(...)`     | cflow变量                                 |
| `$r`              | 返回类型，用于类型转换                    |
| `$w`              | 包装类型，用于类型转换                    |
| `$_`              | 方法返回值                                |
| `$sig`            | 方法签名，返回`java.lang.Class[]`数组类型 |
| `$type`           | 返回值类型，`java.lang.Class`类型         |
| `$class`          | 当前类，`java.lang.Class`类型             |

## 读取类/成员变量/方法信息

```java
package com.anbai.sec.bytecode.javassist;

import javassist.*;

import java.util.Arrays;

public class JavassistClassVisTest {
    public static void main(String[] args) throws NotFoundException {
        String className = "com.anbai.sec.bytecode.TestHelloWorld";
        //先创建classpool
        ClassPool classPool = ClassPool.getDefault();

        CtClass ctClass = classPool.get(className);
        System.out.println(
                "类名:" + ctClass.getName() + ", 父类: " + ctClass.getSuperclass().getName() +
                        ", 实现的接口: " + Arrays.toString(ctClass.getInterfaces())
        );
        System.out.println("--------------------------------------------------------");

        //获取所有构造方法
        CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();

        //获取所有的成员变量
        CtField[] ctFields = ctClass.getDeclaredFields();

        //获取所有的成员方法
        CtMethod[] ctMethods = ctClass.getDeclaredMethods();

        //输出所有成员方法
        for (CtMethod ctMethod:
             ctMethods) {
            System.out.println(ctMethod.getMethodInfo());
        }

        System.out.println("--------------------------------------------------------");

        for (CtField ctField: ctFields
             ) {
            System.out.println(ctField.getFieldInfo());
        }
        System.out.println("--------------------------------------------------------");
    }
}

```

## 修改类方法

```java
  public static void main(String[] args) throws NotFoundException, CannotCompileException, IOException {
        String className = "com.anbai.sec.bytecode.TestHelloWorld";
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get(className);
        CtMethod hello = ctClass.getDeclaredMethod("hello", new CtClass[]{classPool.get("java.lang.String")});
        hello.setModifiers(Modifier.PRIVATE);
        hello.insertBefore("System.out.println($1);"); //输出hello方法的第一个参数
        hello.insertAfter("System.out.println($_); return \"Return:\" + $_;");
        File classFilePath = new File(new File(System.getProperty("user.dir"), "javaweb-sec-source/javase/src/main/java/com/anbai/sec/bytecode/"), "TestHelloWorld.class");
        byte[] bytes = ctClass.toBytecode();
        FileUtils.writeByteArrayToFile(classFilePath,bytes);
```

## 动态创建java类

几乎就是`makeClass`然后利用`CtMethod+CtFiled`等类的`make`方法和`ctClass`的`addxxx`方法，来添加到代码

# 0x06 java class反编译

等后面学习java逆向的时候再来吧。暂时需求不大。

https://zhishihezi.net/endpoint/richtext/1afed581c026296036d7f448c5e8ba34?event=436b34f44b9f95fd3aa8667f1ad451b173526ab5441d9f64bd62d183bed109b0ea1aaaa23c5207a446fa6de9f588db3958e8cd5c825d7d5216199d64338d9d005ed286fb570812d9c7e97a465fb54fc35493b08171b71a6e4c81021980de9ca692a60eea2b74f513456a6aa08c7d8c08892e78996afac0302a321db71903ed0488fc10c50dc657df41756023d5db9c7ac0e3d0b81aed217f0712631bb713d3fd41904d2cc41916cb059c22ac6f50276c89b456755f080a389fc8e1a7e6ba6962d522f305553f26e863cc39f8ab9ec0521ce7f00e407263fd8e6791da9139929f58f047f67ad12006164a6a30e00aeae64d7a5146973fd9f218b41467b4cb3617#0

# 0x07 java Agent机制

运行模式:

- `-javaagent`API实现方式或者`-agentpath/-agentlib`参数
- `attach`，可以对于正在运行的java进程附加agent。

## java agent

和普通的class没有什么区别，只是普通的class文件以`main`方法作为程序的入口，agent以`premain`(Agent模式)和`agentmain`(Attach模式)作为Agent程序的入口。二者参数上 一模一样。

必须以jar包的形式运行或加载，并且包含`/META-INF/MANIFEST.MF`,该文件必须定义好究竟以哪个模式来运行，如果需要agent来修改jvm修改过的字节码，那么还需要设置`Can-Retransform-Classes: true` 或者`Can-Redefine-Classes: true`

## Instrumentation

这是检测运行在`JVM程序`中的`java api`，我们可以实现以下功能。(摘抄自大哥的盒子)

1. 动态添加或移除自定义的`ClassFileTransformer`（`addTransformer/removeTransformer`），JVM会在类加载时调用Agent中注册的`ClassFileTransformer`；
2. 动态修改`classpath`（`appendToBootstrapClassLoaderSearch`、`appendToSystemClassLoaderSearch`），将Agent程序添加到`BootstrapClassLoader`和`SystemClassLoaderSearch`（对应的是`ClassLoader类的getSystemClassLoader方法`，默认是`sun.misc.Launcher$AppClassLoader`）中搜索；
3. 动态获取所有`JVM`已加载的类(`getAllLoadedClasses`)；
4. 动态获取某个类加载器已实例化的所有类(`getInitiatedClasses`)。
5. 重定义某个已加载的类的字节码(`redefineClasses`)。
6. 动态设置`JNI`前缀(`setNativeMethodPrefix`)，可以实现`Hook native`方法。(安卓逆向常用)
7. 重新加载某个已经被JVM加载过的类字节码`retransformClasses`)。

## ClassFileTransformer

```java
/*
 * 灵蜥Java Agent版 [Web应用安全智能防护系统]
 * ----------------------------------------------------------------------
 * Copyright © 安百科技（北京）有限公司
 */
package com.anbai.sec.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * Creator: yz
 * Date: 2020/1/2
 */
public class CrackLicenseAgent {

    /**
     * 需要被Hook的类
     */
    private static final String HOOK_CLASS = "com.anbai.sec.agent.CrackLicenseTest";

    /**
     * Java Agent模式入口
     *
     * @param args 命令参数
     * @param inst Instrumentation
     */
    public static void premain(String args, final Instrumentation inst) {
        loadAgent(args, inst);
    }

    /**
     * Java Attach模式入口
     *
     * @param args 命令参数
     * @param inst Instrumentation
     */
    public static void agentmain(String args, final Instrumentation inst) {
        loadAgent(args, inst);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            List<VirtualMachineDescriptor> list = VirtualMachine.list();

            for (VirtualMachineDescriptor desc : list) {
                System.out.println("进程ID：" + desc.id() + "，进程名称：" + desc.displayName());
            }

            return;
        }

        // Java进程ID
        String pid = args[0];

        try {
            // 注入到JVM虚拟机进程
            VirtualMachine vm = VirtualMachine.attach(pid);

            // 获取当前Agent的jar包路径
            URL agentURL = CrackLicenseAgent.class.getProtectionDomain().getCodeSource().getLocation();
            String agentPath = new File(agentURL.toURI()).getAbsolutePath();

            // 注入Agent到目标JVM
            vm.loadAgent(agentPath);
            vm.detach();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载Agent
     *
     * @param arg  命令参数
     * @param inst Instrumentation
     */
    private static void loadAgent(String arg, final Instrumentation inst) {
        // 创建ClassFileTransformer对象
        ClassFileTransformer classFileTransformer = createClassFileTransformer();

        // 添加自定义的Transformer，第二个参数true表示是否允许Agent Retransform，
        // 需配合MANIFEST.MF中的Can-Retransform-Classes: true配置
        inst.addTransformer(classFileTransformer, true);

        // 获取所有已经被JVM加载的类对象
        Class[] loadedClass = inst.getAllLoadedClasses();

        for (Class clazz : loadedClass) {
            String className = clazz.getName();

            if (inst.isModifiableClass(clazz)) {
                // 使用Agent重新加载HelloWorld类的字节码
                if (className.equals(HOOK_CLASS)) {
                    try {
                        inst.retransformClasses(clazz);
                    } catch (UnmodifiableClassException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static ClassFileTransformer createClassFileTransformer() {
        return new ClassFileTransformer() {

            /**
             * 类文件转换方法，重写transform方法可获取到待加载的类相关信息
             *
             * @param loader              定义要转换的类加载器；如果是引导加载器，则为 null
             * @param className           类名,如:java/lang/Runtime
             * @param classBeingRedefined 如果是被重定义或重转换触发，则为重定义或重转换的类；如果是类加载，则为 null
             * @param protectionDomain    要定义或重定义的类的保护域
             * @param classfileBuffer     类文件格式的输入字节缓冲区（不得修改）
             * @return 字节码byte数组。
             */
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {

                // 将目录路径替换成Java类名
                className = className.replace("/", ".");

                // 只处理com.anbai.sec.agent.CrackLicenseTest类的字节码
                if (className.equals(HOOK_CLASS)) {
                    try {
                        ClassPool classPool = ClassPool.getDefault();

                        // 使用javassist将类二进制解析成CtClass对象
                        CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

                        // 使用CtClass对象获取checkExpiry方法，类似于Java反射机制的clazz.getDeclaredMethod(xxx)
                        CtMethod ctMethod = ctClass.getDeclaredMethod(
                                "checkExpiry", new CtClass[]{classPool.getCtClass("java.lang.String")}
                        );

                        // 在checkExpiry方法执行前插入输出License到期时间代码
                        ctMethod.insertBefore("System.out.println(\"License到期时间：\" + $1);");

                        // 修改checkExpiry方法的返回值，将授权过期改为未过期
                        ctMethod.insertAfter("return false;");

                        // 修改后的类字节码
                        classfileBuffer = ctClass.toBytecode();
                        File classFilePath = new File(new File(System.getProperty("user.dir"), "javaweb-sec-source/javasec-agent/src/main/java/com/anbai/sec/agent/"), "CrackLicenseTest.class");

                        // 写入修改后的字节码到class文件
                        FileOutputStream fos = new FileOutputStream(classFilePath);
                        fos.write(classfileBuffer);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return classfileBuffer;
            }
        };
    }

}
```

假如我以`agent`的方法运行

```bash
java -javaagent:target/javasec-agent.jar -classpath target/test-classes/ com.anbai.sec.agent.CrackLicenseTest
```

一定要记得指定`classpath`那么程序现在开始运行了。

![image-20220107214522397](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220107214522397.png)

首先创建一个`classfiletransformer`

![image-20220107214607254](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220107214607254.png)

重新构建的时候一定要记得好好复写`transform`方法

```
 /**
 * 类文件转换方法，重写transform方法可获取到待加载的类相关信息
 *
 * @param loader              定义要转换的类加载器；如果是引导加载器，则为 null
 * @param className           类名,如:java/lang/Runtime
 * @param classBeingRedefined 如果是被重定义或重转换触发，则为重定义或重转换的类；如果是类加载，则为 null
 * @param protectionDomain    要定义或重定义的类的保护域
 * @param classfileBuffer     类文件格式的输入字节缓冲区（不得修改）
 * @return 字节码byte数组。
 */
```

首先判断是不是我们要hook的类

![image-20220107214751926](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220107214751926.png)

然后将字节码转换为ctclass进行处理

![image-20220107214835764](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220107214835764.png)

进行更改

![image-20220107214939850](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220107214939850.png)

进行重置

![image-20220107214928720](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220107214928720.png)

这也是一个正常的hook过程，一会来实现一个简单的hook。



假如 我们用 `Attach`模式

用java自带的代码查看当前jvm里面运行的类

```java
List<VirtualMachineDescriptor> list = VirtualMachine.list();

for (VirtualMachineDescriptor desc : list) {
    System.out.println("进程ID：" + desc.id() + "，进程名称：" + desc.displayName());
}
```

注入的过程

```java
// Java进程ID
String pid = args[0];

// 设置Agent文件的绝对路径
String agentPath = "/xxx/agent.jar";

// 注入到JVM虚拟机进程
VirtualMachine vm = VirtualMachine.attach(pid);

// 注入Agent到目标JVM
vm.loadAgent(agentPath);
vm.detach();
```

运行

```bash
java -classpath $JAVA_HOME/lib/tools.jar:target/javasec-agent.jar com.anbai.sec.agent.CrackLicenseAgent
```

# 0x08 动态加载字节码

前面有很多的关于动态操作字节码的操作，这部分就是看了p神的`java`漫谈之后的一点感受，记录一下加载字节码的几种方式。

## URL加载远程

正常情况下，Java会根据配置项 sun.boot.class.path 和 java.class.path 中列举到的基础路径（这 些路径是经过处理后的 java.net.URL 类）来寻找.class文件来加载，而这个基础路径有分为三种情况： 

- URL未以斜杠 / 结尾，则认为是一个JAR文件，使用 JarLoader 来寻找类，即为在Jar包中寻 找.class文件 
- URL以斜杠 / 结尾，且协议名是 file ，则使用 FileLoader 来寻找类，即为在本地文件系统中寻 找.class文件 
- URL以斜杠 / 结尾，且协议名不是 file ，则使用最基础的 Loader 来寻找类

我们来测试一下

```java
package com.dem0.bytecode;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class UrlClassLoade {
    public static void main( String[] args ) {
        try {
            URL[] urls = {new URL("http://localhost:8000/")};
            URLClassLoader loader = URLClassLoader.newInstance(urls);
            Class c = loader.loadClass("Testabc");
            c.newInstance();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

```

他会现在本地寻找这个类，找不到才去url找

![image-20220110232807901](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220110232807901.png)

## 利用ClassLoader#defineClass直接加载字节码

![image-20220110234025197](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220110234025197.png)

```java
public class Define0class {
    public static void main(String[] args) throws NoSuchMethodException, ParseException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        byte[] a = Base64.decode("yv66vgAAADQAMAoACwAXCQAYABkIABoKABsAHAoAHQAeCAAfCgAdACAHACEIACIHACMHACQBAAY8aW5pdD4BAAMoKVYBAARDb2RlAQAPTGluZU51bWJlclRhYmxlAQAEbWFpbgEAFihbTGphdmEvbGFuZy9TdHJpbmc7KVYBAAg8Y2xpbml0PgEADVN0YWNrTWFwVGFibGUHACEBAApTb3VyY2VGaWxlAQAMVGVzdGFiYy5qYXZhDAAMAA0HACUMACYAJwEABUhlbGxwBwAoDAApACoHACsMACwALQEABGNhbGMMAC4ALwEAE2phdmEvbGFuZy9FeGNlcHRpb24BAAExAQAHVGVzdGFiYwEAEGphdmEvbGFuZy9PYmplY3QBABBqYXZhL2xhbmcvU3lzdGVtAQADb3V0AQAVTGphdmEvaW8vUHJpbnRTdHJlYW07AQATamF2YS9pby9QcmludFN0cmVhbQEAB3ByaW50bG4BABUoTGphdmEvbGFuZy9TdHJpbmc7KVYBABFqYXZhL2xhbmcvUnVudGltZQEACmdldFJ1bnRpbWUBABUoKUxqYXZhL2xhbmcvUnVudGltZTsBAARleGVjAQAnKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1Byb2Nlc3M7ACEACgALAAAAAAADAAEADAANAAEADgAAAB0AAQABAAAABSq3AAGxAAAAAQAPAAAABgABAAAAAQAJABAAEQABAA4AAAAlAAIAAQAAAAmyAAISA7YABLEAAAABAA8AAAAKAAIAAAAKAAgACwAIABIADQABAA4AAABTAAIAAQAAABa4AAUSBrYAB1enAAxLsgACEgm2AASxAAEAAAAJAAwACAACAA8AAAAWAAUAAAAEAAkABwAMAAUADQAGABUACAATAAAABwACTAcAFAgAAQAVAAAAAgAW");
        Class hello = (Class) defineClass.invoke(ClassLoader.getSystemClassLoader(), "Testabc", a, 0, a.length);
        hello.newInstance();
    }
}
```

我感觉可以再深入一点

![image-20220110234054966](https://gitee.com/Cralwer/typora-pic/raw/master/images/image-20220110234054966.png)

因为后面好像是`define0`

这个时候也可以复习一下自定义ClassLoader来加载。

```java
@Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        // 只处理TestHelloWorld类
        if (name.equals(TEST_CLASS_NAME)) {
            // 调用JVM的native方法定义TestHelloWorld类
            return defineClass(TEST_CLASS_NAME, TEST_CLASS_BYTES, 0, TEST_CLASS_BYTES.length);
        }

        return super.findClass(name);
    }
```

可以特殊处理一些类来达到效果

## `TemplatesImpl`加载字节码攻击

这个类的使用必须继承了`AbstractTranslet`

```java
package com.dem0.bytecode;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;

import com.unboundid.util.Base64;

import java.lang.reflect.Field;
import java.text.ParseException;


public class TemplateImplLoad {
    private static final String TEST_CLASS_NAME = "Testabc";
    private static byte[] TEST_CLASS_BYTES = new byte[0];

    static {
        try {
            TEST_CLASS_BYTES = Base64.decode("yv66vgAAADQAMAoACwAXCQAYABkIABoKABsAHAoAHQAeCAAfCgAdACAHACEIACIHACMHACQBAAY8aW5pdD4BAAMoKVYBAARDb2RlAQAPTGluZU51bWJlclRhYmxlAQAEbWFpbgEAFihbTGphdmEvbGFuZy9TdHJpbmc7KVYBAAg8Y2xpbml0PgEADVN0YWNrTWFwVGFibGUHACEBAApTb3VyY2VGaWxlAQAMVGVzdGFiYy5qYXZhDAAMAA0HACUMACYAJwEABUhlbGxwBwAoDAApACoHACsMACwALQEABGNhbGMMAC4ALwEAE2phdmEvbGFuZy9FeGNlcHRpb24BAAExAQAHVGVzdGFiYwEAEGphdmEvbGFuZy9PYmplY3QBABBqYXZhL2xhbmcvU3lzdGVtAQADb3V0AQAVTGphdmEvaW8vUHJpbnRTdHJlYW07AQATamF2YS9pby9QcmludFN0cmVhbQEAB3ByaW50bG4BABUoTGphdmEvbGFuZy9TdHJpbmc7KVYBABFqYXZhL2xhbmcvUnVudGltZQEACmdldFJ1bnRpbWUBABUoKUxqYXZhL2xhbmcvUnVudGltZTsBAARleGVjAQAnKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1Byb2Nlc3M7ACEACgALAAAAAAADAAEADAANAAEADgAAAB0AAQABAAAABSq3AAGxAAAAAQAPAAAABgABAAAAAQAJABAAEQABAA4AAAAlAAIAAQAAAAmyAAISA7YABLEAAAABAA8AAAAKAAIAAAAKAAgACwAIABIADQABAA4AAABTAAIAAQAAABa4AAUSBrYAB1enAAxLsgACEgm2AASxAAEAAAAJAAwACAACAA8AAAAWAAUAAAAEAAkABwAMAAUADQAGABUACAATAAAABwACTAcAFAgAAQAVAAAAAgAW");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    };
    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception{
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj,value);
    }

public static void main(String[] args) throws Exception {
        TemplatesImpl obj = new TemplatesImpl();
        //setFieldValue 设置私有属性
        setFieldValue(obj, "_bytecodes", new byte[][] {TEST_CLASS_BYTES});
        setFieldValue(obj, "_name", "TemplateImplLoad");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        obj.newTransformer();

    }
}

```

## BCEL

在Java 8u251的更新中，这个ClassLoader被移除了，所以之后只能 且用且珍惜了。

构造:

```java
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.Utility;
import com.sun.org.apache.bcel.internal.Repository;
public class HelloBCEL {
public static void main(String []args) throws Exception {
JavaClass cls = Repository.lookupClass(evil.Hello.class);
String code = Utility.encode(cls.getBytes(), true);
System.out.println(code);
}
}
```

使用直接Loader即可。

