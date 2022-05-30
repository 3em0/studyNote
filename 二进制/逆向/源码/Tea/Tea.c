#include<stdlib.h>
#include<stdio.h>
#define DELTA  0x9e3779B9

void tea_encrypt(unsigned int * v, unsigned int *key){
    //v 待加密的字符 key: 密钥(128位)
    unsigned int l = v[0], r= v[1], sum = 0;
    for (size_t i = 0; i < 32; i++)// 32次加密迭代
    {
         l += (((r << 4) ^ (r >>5)) + r) ^ (sum + key[sum & 3]);
         sum += DELTA;//可能被识别成为0x61C88647h
         r += (((l << 4) ^ (l >> 5)) + l) ^ (sum + key[(sum >> 11) & 3]);
    }
    v[0] = l;
    v[1] = r;  
}

// 因为tea算法和des差不多 都是可逆的

void tea_decrypt(unsigned int *v , unsigned int *key){
    unsigned int l = v[0], r= v[1], sum = 32 * DELTA;
    for (size_t i = 0; i < 32; i++)// 32次加密迭代
    {
         r -= (((l << 4) ^ (l >> 5)) + l) ^ (sum + key[(sum >> 11) & 3]);
         sum -= DELTA;
         l -= (((r << 4) ^ (r >>5)) + r) ^ (sum + key[sum & 3]);
    }
    v[0] = l;
    v[1] = r; 
}
int main(int argc, char const *argv[])
{
    unsigned int v[2]={123,456},key[4]={0x11,0x22,0x33,0x44};
    printf("%u,%u\n",v[0],v[1]);
    tea_encrypt(v,key);
    printf("%u,%u\n",v[0],v[1]);
    tea_decrypt(v,key);
    printf("%u,%u\n",v[0],v[1]);
    return 0;
}
h