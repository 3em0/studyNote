# [day1] Tea算法

## 0x01 算法简介

​	TEA算法使用64位的明文分组和128位的密钥，使用feistel分组加框架，需要进行32轮循环得到最后的64位密文，其中magic number DELTA是由黄金分割点。是一种分组加密算法，它的实现非常简单，通常只需要很精短的几行代码。

​	现在在CTF的比赛中常常出现的一般有以下几种形式: `TEA`,`XTEA`,`XXTEA`.下面看一下各个算法的加密过程和解密。分析一下特征。

## 0x02 TEA

![TEA算法过程](https://img.dem0dem0.top/images/20200720005736509.png)

下面是c语言的实现过程

```c
#define DELTA 0x9e3779b9

void tea_encrypt(unsigned int* v, unsigned int* key) {
  unsigned int l = v[0], r = v[1], sum = 0;
  for (size_t i = 0; i < 32; i++) { //进行32次迭代加密，Tea算法作者的建议迭代次数
    l += (((r << 4) ^ (r >> 5)) + r) ^ (sum + key[sum & 3]);
    sum += DELTA; //累加Delta的值
    r += (((l << 4) ^ (l >> 5)) + l) ^ (sum + key[(sum >> 11) & 3]); //利用多次双位移和异或将明文与密钥扩散混乱，并将两个明文互相加密
  }
  v[0] = l;
  v[1] = r;
}

//利用可逆性将加密过程逆转
void tea_decrypt(unsigned int* v, unsigned int* key) {
  unsigned int l = v[0], r = v[1], sum = 0;
  sum = DELTA * 32; //32次迭代累加后delta的值
  for (size_t i = 0; i < 32; i++) {
    r -= (((l << 4) ^ (l >> 5)) + l) ^ (sum + key[(sum >> 11) & 3]);
    sum -= DELTA;
    l -= (((r << 4) ^ (r >> 5)) + r) ^ (sum + key[sum & 3]);
  }
  v[0] = l;
  v[1] = r;
}

```

其中最关键的特征大概在于`DELTA 0x9e3779b9(-0x61C88647h)`和`32轮的迭代`。这种简单的算法，我们在实际中可以使用ida的插件`在逆向程序的时候，可以利用ida的插件findcypto识别tea算法`.那么bypass就是魔改DELTA.

## 0X03 XTEA

同样是一个64位块的Feistel密码，使用`128位密钥，建议64轮`, 但四个子密钥采取不正规的方式进行混合以阻止密钥表攻击.

```c
#include <stdio.h>
#include <stdint.h>
 
/* take 64 bits of data in v[0] and v[1] and 128 bits of key[0] - key[3] */
 
void encipher(unsigned int num_rounds, uint32_t v[2], uint32_t const key[4]) {
    unsigned int i;
    uint32_t v0=v[0], v1=v[1], sum=0, delta=0x9E3779B9;
    for (i=0; i < num_rounds; i++) {
        v0 += (((v1 << 4) ^ (v1 >> 5)) + v1) ^ (sum + key[sum & 3]);
        sum += delta;
        v1 += (((v0 << 4) ^ (v0 >> 5)) + v0) ^ (sum + key[(sum>>11) & 3]);
    }
    v[0]=v0; v[1]=v1;
}
 
void decipher(unsigned int num_rounds, uint32_t v[2], uint32_t const key[4]) {
    unsigned int i;
    uint32_t v0=v[0], v1=v[1], delta=0x9E3779B9, sum=delta*num_rounds;
    for (i=0; i < num_rounds; i++) {
        v1 -= (((v0 << 4) ^ (v0 >> 5)) + v0) ^ (sum + key[(sum>>11) & 3]);
        sum -= delta;
        v0 -= (((v1 << 4) ^ (v1 >> 5)) + v1) ^ (sum + key[sum & 3]);
    }
    v[0]=v0; v[1]=v1;
}
 
int main()
{
    uint32_t v[2]={1,2};
    uint32_t const k[4]={2,2,3,4};
    unsigned int r=32;//num_rounds建议取值为32
    // v为要加密的数据是两个32位无符号整数
    // k为加密解密密钥，为4个32位无符号整数，即密钥长度为128位
    printf("加密前原始数据：%u %u\n",v[0],v[1]);
    encipher(r, v, k);
    printf("加密后的数据：%u %u\n",v[0],v[1]);
    decipher(r, v, k);
    printf("解密后的数据：%u %u\n",v[0],v[1]);
    return 0;
}
```

特征不变。`num_rounds`.还有就是4位的key。

## 0x04 XXTEA

![XXTEA cipher.svg的算法图](https://img.dem0dem0.top/images/20200720005804592.png)

```
#define MX (((z >> 5) ^ (y << 2)) + ((y >> 3) ^ (z << 4))) ^ ((sum ^ y) + (key[(p & 3) ^ e] ^ z))
#define DELTA 0x9e3779b9
//XXTEA 加密，在处理数据流中每个数据时利用了相邻数据，使用MX函数计算加密值
static uint32_t * xxtea_uint_encrypt(uint32_t * data, size_t len, uint32_t * key) {
    uint32_t n = (uint32_t)len - 1;
    // 6 和 52 是怎么来的？
    uint32_t z = data[n], y, p, q = 6 + 52 / (n + 1), sum = 0, e;
    if (n < 1) return data;
    while (0 < q--) {
        sum += DELTA;
        // 根据sum 计算得出0~3中的某一个数值, 用于MX中与p共同作用选择key数组中某个秘钥值
        e = sum >> 2 & 3;
        //遍历每个待加密的数据
        for (p = 0; p < n; p++) {
            //z的初值为data[len - 1]，即将数据数组当做是环形队列来处理的，首尾相连，当加密data[0]时，需要用到data[len - 1]，data[0]，data[0 + 1]，以及MX计算返回的的一个加密值，加密值与data[0]相加后达到加密的效果
            y = data[p + 1];
            z = data[p] += MX;
        }
        //当加密data[len-1]时，需要用到data[len - 2]，data[len-1]，data[0]，以及MX计算返回的的一个加密值，加密值与data[len-1]相加后达到加密的效果
        y = data[0];
        z = data[n] += MX;
    }
    return data;
}
//XXTEA 解密，把加密的步骤反过来即可得到解密的方法
static uint32_t * xxtea_uint_decrypt(uint32_t * data, size_t len, uint32_t * key) {
    uint32_t n = (uint32_t)len - 1;
    uint32_t z, y = data[0], p, q = 6 + 52 / (n + 1), sum = q * DELTA, e;
    if (n < 1) return data;
    while (sum != 0) {
        e = sum >> 2 & 3;
        for (p = n; p > 0; p--) {
            z = data[p - 1];
            y = data[p] -= MX;
        }
        z = data[n];
        y = data[0] -= MX;
        sum -= DELTA;
    }
    return data;
}
```

![image-20220511000925925](https://img.dem0dem0.top/images/image-20220511000925925.png)

可能是因为我使用的宏定义的原因，可以看出比较明显的特点。MX