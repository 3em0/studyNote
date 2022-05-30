#include<stdio.h>
#include<stdlib.h>
#include<stdint.h>
#define MX (((z >> 5) ^ (y << 2)) + ((y>>3) ^(z<<4))) ^ ((sum ^y)+(key[(p&3) ^e]^z))
#define DELTA 0x9e3779b9
static uint32_t * xxtea_uint_encrypt(uint32_t *data,size_t len, uint32_t * key){
    uint32_t n = (uint32_t)len-1;
    uint32_t z = data[n],y,p,q=6+52/(n+1), sum=0,e;
    if(n < 1) return data;
    while (0 < q--)
    {
        sum += DELTA;
        // sum选择0~3 用于mX与p选择key
        e = sum >> 2 & 3;
        //遍历每个待加密的数据
        for (p  = 0; p < n; p++)
        {
            //z的初值为data[len -1],即将数据数组当作是环形队列来处理的，首尾相连，当加密data[0]时，需要用到data[len - 1]，data[0]，data[0 + 1]，以及MX计算返回的的一个加密值，加密值与data[0]相加后达到加密的效果
            y = data[p+1];
            z = data[p] += MX;
        }
        y = data[0];
        z = data[n] += MX;
    }

}

//逆转过来就是我们要的答案
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

int main()
{
    uint32_t v[2]={123,456};
    uint32_t key[4]={0x11,0x22,0x33,0x44};
    int n= 2; //n的绝对值表示v的长度，取正表示加密，取负表示解密
    // v为要加密的数据是两个32位无符号整数
    // k为加密解密密钥，为4个32位无符号整数，即密钥长度为128位
    printf("加密前原始数据：%u %u\n",v[0],v[1]);
    xxtea_uint_encrypt(v, n, key);
    printf("加密后的数据：%u %u\n",v[0],v[1]);
    xxtea_uint_decrypt(v, n, key);
    printf("解密后的数据：%u %u\n",v[0],v[1]);
    return 0;
}