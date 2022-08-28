from urllib.parse import urlparse
from urllib.parse import urlsplit
# 该方法可以实现URL的识别和分段
result = urlparse('http://www.baidu.com/index.html;user?id=5#comment')
# 这里我们利用urlparse()方法进行了一个URL的解析。首先，输出了解析结果的类型，然后将结果也输出出来。
a = urlsplit("http://www.baidu.com/index.html;user?id=5#comment")
print(result.hostname)
print(list(a)[1])