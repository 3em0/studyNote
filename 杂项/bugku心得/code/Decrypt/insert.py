import binascii

s = 'hex2bin'
str_16 = binascii.b2a_hex(s.encode('utf-8'))  # 字符串转16进制
print("16进制：")
print(str_16)


def baseN(num, b):
    return ((num == 0) and "0") or \
           (baseN(num // b, b).lstrip("0") + "0123456789abcdefghijklmnopqrstuvwxyz"[num % b])


num_10 = int(str_16, 16)  # 16进制转10进制
print("10进制：")
print(num_10)

str_32 = baseN(1,16)  # 10进制转x进制
print("36进制：")
print(str_32)

num_10_2 = int(str_32, 32)  # 32进制转10进制
print("32转10进制：")
print(num_10_2)

num_16 = hex(num_10)  # 10进制转16进制数
print(num_16)

ss = str_16.decode('hex')  # 16进制串转字符串
print(ss)