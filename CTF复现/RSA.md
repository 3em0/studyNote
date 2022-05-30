# RSA算法常用脚本



RSA算法中，有时候公私钥需要自己去提取。

```bash
openssl rsa -pubin -in pubkey.pem -text -modulus
rsautl -decrypt -inkey private.pem -in flag.enc -out flag
用16进制打开flag.enc即可
```

yafu神器:http://sourceforge.net/projects/yafu/

在线分解网站:http://www.factordb.com/

简单的解密流程

```
from Crypto.Util.number import long_to_bytes,bytes_to_long,getPrime,isPrime
import gmpy2
import primefac
def modinv(a,n):
    return primefac.modinv(a,n)%n
n=
c=
p=
q=
e=
d=modinv(e,(p-1)*(q-1))
m=pow(c,d,n)
print long_to_bytes(m)
```



## 0x01. e=2

```python
from libnum import *
import gmpy2
cipher = 7665003682830666456193894491015989641647854826647177873141984107202099081475984827806007287830472899616818080907276606744467453445908923054975393623509539
p = 85228565021128901853314934583129083441989045225022541298550570449389839609019
q = 111614714641364911312915294479850549131835378046002423977989457843071188836271
z = (p-1)*(q-1)
N = 9512761964126317383595189153785370209773377450836555004820867299207255734995592313771606654256571257782557661640172907340179614703554167269794404245928149
e = 2
yp = gmpy2.invert(p,q)
yq = gmpy2.invert(q,p)

mp = pow(cipher, (p + 1) / 4, p)
mq = pow(cipher, (q + 1) / 4, q)
a = (yp * p * mq + yq * q * mp) % N
b = N - int(a)
c = (yp * p * mq - yq * q * mp) % N
d = N - int(c)

for i in (a,b,c,d):
    s = '%x' % i
    if len(s) % 2 != 0:
        s = '0' + s
    print s.decode('hex')
p = gmpy2.mpz(p)
q = gmpy2.mpz(q)
N = gmpy2.mpz(n)

'''
cipher = pow(s2n(FLAG), 2, n)
print(cipher)
'''
cipher = 7665003682830666456193894491015989641647854826647177873141984107202099081475984827806007287830472899616818080907276606744467453445908923054975393623509539
```

## 0x02 公约数模数攻击

如果Alice和Bob之间通信，两次通信中，有一个大素数是相同的，那么我们就可以通过求大素数的最大公约数，解出两次的加密密钥。

```python
import primefac
from Crypto.Util.number import long_to_bytes,bytes_to_long,getPrime,isPrime
def modinv(a,n):
    return primefac.modinv(a,n)%n

#输入大素数n
n1=
n2=

p1=primefac.gcd(n1,n2)
q1 = n1/p1
q2 = n2/p2
输入两次的e
e1=
e2=
d1=modinv(e1,(p1-1)*(q1-1))
d2=modinv(e2,(p1-1)*(q2-1))
m1=pow(c1,d1,n1)
m2=pow(c2,d2,n2)
print long_to_bytes(m1)
print long_to_bytes(m2)
```

## 0x03 小指数明文爆破（小e

在遇到这样的情况下，使用这种方法：

**m的e次方小于n**（甚至在e=3时，还可能出现等于）

那么此时，我们采取的方式就时利用爆破**K**的值来达到求解的结果。原理时`k*n<m**3<(k+1)*n`上脚本

```
n=
e=3
c=
import primefac
from Crypto.Util.number import long_to_bytes,bytes_to_long,getPrime,isPrime
def modinv(a,n):
    return primefac.modinv(a,n)%n
import gmpy2
i=0
while 1:
	if(gmpy2.iroot(c+i*n,3)[1]==1):
		print long_to_bytes(gmpy2.iroot(c+i*n,3)[0])
		break
	i = i + 1
```

## 0x04 选择密文攻击

如果存在交互，可以对任意的密文进行解密的话，那么就可以时求一个C，再求一个C的逆。

## 0x05 LLL-attack(小e)

使用的条件:e=3,同时前面又会经常性地附上一句总所周知的问候语，密文的一部分已经被泄露，或者P或Q的一部分被泄露出来，在泄露的消息长度足够的时候，就可以使用Coppersmith method的方法求明文。

这个方法也适合 p泄露了2/3的情况。

代码链接：https://github.com/mimoo/RSA-and-LLL-attacks（使用sageonline

```
import time

debug = True

# display matrix picture with 0 and X
def matrix_overview(BB, bound):
    for ii in range(BB.dimensions()[0]):
        a = ('%02d ' % ii)
        for jj in range(BB.dimensions()[1]):
            a += '0' if BB[ii,jj] == 0 else 'X'
            a += ' '
        if BB[ii, ii] >= bound:
            a += '~'
        print a

def coppersmith_howgrave_univariate(pol, modulus, beta, mm, tt, XX):
    """
    Coppersmith revisited by Howgrave-Graham
    
    finds a solution if:
    * b|modulus, b >= modulus^beta , 0 < beta <= 1
    * |x| < XX
    """
    #
    # init
    #
    dd = pol.degree()
    nn = dd * mm + tt

    #
    # checks
    #
    if not 0 < beta <= 1:
        raise ValueError("beta should belongs in (0, 1]")

    if not pol.is_monic():
        raise ArithmeticError("Polynomial must be monic.")

    #
    # calculate bounds and display them
    #
    """
    * we want to find g(x) such that ||g(xX)|| <= b^m / sqrt(n)

    * we know LLL will give us a short vector v such that:
    ||v|| <= 2^((n - 1)/4) * det(L)^(1/n)

    * we will use that vector as a coefficient vector for our g(x)
    
    * so we want to satisfy:
    2^((n - 1)/4) * det(L)^(1/n) < N^(beta*m) / sqrt(n)
    
    so we can obtain ||v|| < N^(beta*m) / sqrt(n) <= b^m / sqrt(n)
    (it's important to use N because we might not know b)
    """
    if debug:
        # t optimized?
        print "\n# Optimized t?\n"
        print "we want X^(n-1) < N^(beta*m) so that each vector is helpful"
        cond1 = RR(XX^(nn-1))
        print "* X^(n-1) = ", cond1
        cond2 = pow(modulus, beta*mm)
        print "* N^(beta*m) = ", cond2
        print "* X^(n-1) < N^(beta*m) \n-> GOOD" if cond1 < cond2 else "* X^(n-1) >= N^(beta*m) \n-> NOT GOOD"
        
        # bound for X
        print "\n# X bound respected?\n"
        print "we want X <= N^(((2*beta*m)/(n-1)) - ((delta*m*(m+1))/(n*(n-1)))) / 2 = M"
        print "* X =", XX
        cond2 = RR(modulus^(((2*beta*mm)/(nn-1)) - ((dd*mm*(mm+1))/(nn*(nn-1)))) / 2)
        print "* M =", cond2
        print "* X <= M \n-> GOOD" if XX <= cond2 else "* X > M \n-> NOT GOOD"

        # solution possible?
        print "\n# Solutions possible?\n"
        detL = RR(modulus^(dd * mm * (mm + 1) / 2) * XX^(nn * (nn - 1) / 2))
        print "we can find a solution if 2^((n - 1)/4) * det(L)^(1/n) < N^(beta*m) / sqrt(n)"
        cond1 = RR(2^((nn - 1)/4) * detL^(1/nn))
        print "* 2^((n - 1)/4) * det(L)^(1/n) = ", cond1
        cond2 = RR(modulus^(beta*mm) / sqrt(nn))
        print "* N^(beta*m) / sqrt(n) = ", cond2
        print "* 2^((n - 1)/4) * det(L)^(1/n) < N^(beta*m) / sqrt(n) \n-> SOLUTION WILL BE FOUND" if cond1 < cond2 else "* 2^((n - 1)/4) * det(L)^(1/n) >= N^(beta*m) / sqroot(n) \n-> NO SOLUTIONS MIGHT BE FOUND (but we never know)"

        # warning about X
        print "\n# Note that no solutions will be found _for sure_ if you don't respect:\n* |root| < X \n* b >= modulus^beta\n"
    
    #
    # Coppersmith revisited algo for univariate
    #

    # change ring of pol and x
    polZ = pol.change_ring(ZZ)
    x = polZ.parent().gen()

    # compute polynomials
    gg = []
    for ii in range(mm):
        for jj in range(dd):
            gg.append((x * XX)**jj * modulus**(mm - ii) * polZ(x * XX)**ii)
    for ii in range(tt):
        gg.append((x * XX)**ii * polZ(x * XX)**mm)
    
    # construct lattice B
    BB = Matrix(ZZ, nn)

    for ii in range(nn):
        for jj in range(ii+1):
            BB[ii, jj] = gg[ii][jj]

    # display basis matrix
    if debug:
        matrix_overview(BB, modulus^mm)

    # LLL
    BB = BB.LLL()

    # transform shortest vector in polynomial    
    new_pol = 0
    for ii in range(nn):
        new_pol += x**ii * BB[0, ii] / XX**ii

    # factor polynomial
    potential_roots = new_pol.roots()
    print "potential roots:", potential_roots

    # test roots
    roots = []
    for root in potential_roots:
        if root[0].is_integer():
            result = polZ(ZZ(root[0]))
            if gcd(modulus, result) >= modulus^beta:
                roots.append(ZZ(root[0]))

    # 
    return roots

############################################
# Test on Stereotyped Messages
##########################################    

print "//////////////////////////////////"
print "// TEST 1"
print "////////////////////////////////"

# RSA gen options (for the demo)
//填写的位置
length_N = 1024  # size of the modulus
Kbits = 200      # size of the root
e = 3

# RSA gen (for the demo)
p = next_prime(2^int(round(length_N/2)))
q = next_prime(p)
N = p*q
ZmodN = Zmod(N);

# Create problem (for the demo)
K = ZZ.random_element(0, 2^Kbits)
Kdigits = K.digits(2)
M = [0]*Kbits + [1]*(length_N-Kbits); 
for i in range(len(Kdigits)):
    M[i] = Kdigits[i]
M = ZZ(M, 2)
C = ZmodN(M)^e

# Problem to equation (default)
P.<x> = PolynomialRing(ZmodN) #, implementation='NTL')
pol = (2^length_N - 2^Kbits + x)^e - C
dd = pol.degree()

# Tweak those
beta = 1                                # b = N
epsilon = beta / 7                      # <= beta / 7
mm = ceil(beta**2 / (dd * epsilon))     # optimized value
tt = floor(dd * mm * ((1/beta) - 1))    # optimized value
XX = ceil(N**((beta**2/dd) - epsilon))  # optimized value

# Coppersmith
start_time = time.time()
roots = coppersmith_howgrave_univariate(pol, N, beta, mm, tt, XX)

# output
print "\n# Solutions"
print "we want to find:",str(K)
print "we found:", str(roots)
print("in: %s seconds " % (time.time() - start_time))
print "\n"

############################################
# Test on Factoring with High Bits Known
##########################################
print "//////////////////////////////////"
print "// TEST 2"
print "////////////////////////////////"

# RSA gen
length_N = 1024;
p = next_prime(2^int(round(length_N/2)));
q = next_prime( round(pi.n()*p) );
N = p*q;

# qbar is q + [hidden_size_random]
hidden = 200;
diff = ZZ.random_element(0, 2^hidden-1)
qbar = q + diff; 

F.<x> = PolynomialRing(Zmod(N), implementation='NTL'); 
pol = x - qbar
dd = pol.degree()

# PLAY WITH THOSE:
beta = 0.5                             # we should have q >= N^beta
epsilon = beta / 7                     # <= beta/7
mm = ceil(beta**2 / (dd * epsilon))    # optimized
tt = floor(dd * mm * ((1/beta) - 1))   # optimized
XX = ceil(N**((beta**2/dd) - epsilon)) # we should have |diff| < X

# Coppersmith
start_time = time.time()
roots = coppersmith_howgrave_univariate(pol, N, beta, mm, tt, XX)

# output
print "\n# Solutions"
print "we want to find:", qbar - q
print "we found:", roots
print("in: %s seconds " % (time.time() - start_time))

```

1024bit的p只泄露576bit的情况下，也可以成功破解。网站:http://inaz2.hatenablog.com/entries/2016/01/20

```
# partial_p.sage

p = 0x00f23799c031b942026e420769b74d22fa2114428189139c43c366c6ab8367c6b3d6f821449aafb2058b0e6ed964fa0ad45fb306f96376e80823a72b58101919e50acad3b5e6d079e7ff9218ed6df6edbef536742714ce88b2e717f45af53ef0d04c89faf01c80b28e764973aba27726c85c0236e8756a865c03577722bac5e391
q = 0x00c9d24330fa4945cfe1e5d6912d6bde0231035a1cc8d8ae67d949347b895f8d579bce2adaf37c568957b17a6564dbf80d36d81e4622ab30e02132b0155aefbd3912a27c625a9b7b05bc72217039f5aa88c20cbf9871c3228e9d80d9106f94b11c1f50c40c96862b5cd6b6f781883dd2eff80a059d3ca027af6a03edeb34a7390f
n = p*q
e = 3

beta = 0.5
epsilon = beta^2/7

pbits = p.nbits()
kbits = floor(n.nbits()*(beta^2-epsilon))
pbar = p & (2^pbits-2^kbits)
print "upper %d bits (of %d bits) is given" % (pbits-kbits, pbits)

PR.<x> = PolynomialRing(Zmod(n))
f = x + pbar

print p
x0 = f.small_roots(X=2^kbits, beta=0.3)[0]  # find root < 2^kbits with factor >= n^0.3
print x0 + pbar
```

## 0x06 Wiener Attack（大e） & Boneh Durfee Attack

Wiener Attack(大e)攻击，衡量标准远远超过**65537**

参考地址:http://github.com/pablocelayes/rsa-wiener-attack（主要是RSAwienerHacker.py

## 0x07 共模攻击（同n和同m）

两次通信过程中，使用了相同的n，并且加密了两次相同的m

```python
import primefac
from Crypto.Util.number import long_to_bytes,bytes_to_long,getPrime,isPrime

def same_n_sttack(n1,e1,e2,c1,c2):
	def egcd(a,b):
        x,lastx=0,1
        y.lasty=1,0
        while(b != 0):
            q = a//b
            a,b=b,a%b
            x,lastx=lastx-q*x,x
            y,lasty=lasty-q*y,y
        return (lastx,lasty)
	s= egcd(e1,e2)
    s1 = s[0]
    s2 = s[1]
    if s1 < 0:
        s1 = -s1
        c1 = primefac.modinv(c1,n)
        if c1 < 0:
            c1+=n
     elif s2 < 0:
        s2 = -s2
        c2 = primefac.modinv(c2,n)
        if c2 < 0:
            c1+=n
     m = (pow(c1,s1,n)*pow(c2,s2,n))%n
    return m
```

## 0x08 广播攻击

多次选择的加密指数都比较低，而且几次的加密信息都是相同的。（假设时n次，那么对计算出的Cx就要开n次方）（待写

```
import gmpy2
from Crypto.Util.number import long_to_bytes,bytes_to_long,getPrime,isPrime
import primefac

#默认都使用3为加密指数，假设3次的加密的明文都是相同的
def broadcast_attack(data):
    def extended_gcd(a,b):
        x,y=0,1
        lastx,lasty=1,0
        while b:
            a,(q,b) = b,divmod(a,b)
            x,lastx=lastx-q*x,x
            y,lasty=lasty-q*y,y
        return (lastx,lasty,a)
    def chinese_remainder_theorem(items):
        N = 1
        for a,n in items:
            N*= n
        result=0
        for a,n in items:
            m = N/n
            r,s,d = extended_gcd(n,m)
            if d!= 1:
                N = N/n
                continue
            result += a*s*m
        return  result %N,N
    x,n=chinese_remainder_theorem(data)
    m=gmpy2.iroot(x,3)[0]
    return m
```

