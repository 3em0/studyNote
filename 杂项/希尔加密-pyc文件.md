# 这是一个脚本贴

放上几个超强脚本，就是那种不用动脑子，就可以随便解题的脚本

## 希尔密码 已知明文攻击

```
import math
import string
import sys

import numpy as np
from sympy import Matrix


def menu():
    while True:
        print("---- Hill Cipher ----\n")
        print("1) Encrypt a Message.")
        print("2) Decipher a Message.")
        print("3) Force a Ciphertext (Known Plaintext Attack).")
        print("4) Quit.\n")
        try:
            choice = int(input("Select a function to run: "))
            if 1 <= choice <= 4:
                return choice
            else:
                print("\nYou must enter a number from 1 to 4\n")
        except ValueError:
            print("\nYou must enter a number from 1 to 4\n")
        input("Press Enter to continue.\n")


# Create two dictionaries, english alphabet to numbers and numbers to english alphabet, and returns them
def get_alphabet():
    alphabet = {}
    for character in string.ascii_uppercase:
        alphabet[character] = string.ascii_uppercase.index(character)

    reverse_alphabet = {}
    for key, value in alphabet.items():
        reverse_alphabet[value] = key

    return alphabet, reverse_alphabet


# Get input from the user and checks if respects the alphabet
def get_text_input(message, alphabet):
    while True:
        text = input(message)
        text = text.upper()
        if all(keys in alphabet for keys in text):
            return text
        else:
            print("\nThe text must contain only characters from the english alphabet ([A to Z] or [a to z]).")


# Check if the key is a square in length
def is_square(key):
    key_length = len(key)
    if 2 <= key_length == int(math.sqrt(key_length)) ** 2:
        return True
    else:
        return False


# Create the matrix k for the key
def get_key_matrix(key, alphabet):
    k = list(key)
    m = int(math.sqrt(len(k)))
    for (i, character) in enumerate(k):
        k[i] = alphabet[character]

    return np.reshape(k, (m, m))


# Create the matrix of m-grams of a text, if needed, complete the last m-gram with the last letter of the alphabet
def get_text_matrix(text, m, alphabet):
    matrix = list(text)
    remainder = len(text) % m
    for (i, character) in enumerate(matrix):
        matrix[i] = alphabet[character]
    if remainder != 0:
        for i in range(m - remainder):
            matrix.append(25)

    return np.reshape(matrix, (int(len(matrix) / m), m)).transpose()


# Encrypt a Message and returns the ciphertext matrix
def encrypt(key, plaintext, alphabet):
    m = key.shape[0]
    m_grams = plaintext.shape[1]

    # Encrypt the plaintext with the key provided k, calculate matrix c of ciphertext
    ciphertext = np.zeros((m, m_grams)).astype(int)
    for i in range(m_grams):
        ciphertext[:, i] = np.reshape(np.dot(key, plaintext[:, i]) % len(alphabet), m)
    return ciphertext


# Transform a matrix to a text, according to the alphabet
def matrix_to_text(matrix, order, alphabet):
    if order == 't':
        text_array = np.ravel(matrix, order='F')
    else:
        text_array = np.ravel(matrix)
    text = ""
    for i in range(len(text_array)):
        text = text + alphabet[text_array[i]]
    return text


# Check if the key is invertible and in that case returns the inverse of the matrix
def get_inverse(matrix, alphabet):
    alphabet_len = len(alphabet)
    if math.gcd(int(round(np.linalg.det(matrix))), alphabet_len) == 1:
        matrix = Matrix(matrix)
        return np.matrix(matrix.inv_mod(alphabet_len))
    else:
        return None


# Decrypt a Message and returns the plaintext matrix
def decrypt(k_inverse, c, alphabet):
    return encrypt(k_inverse, c, alphabet)


def get_m():
    while True:
        try:
            m = int(input("Insert the length of the grams (m): "))
            if m >= 2:
                return m
            else:
                print("\nYou must enter a number m >= 2\n")
        except ValueError:
            print("\nYou must enter a number m >= 2\n")


# Force a Ciphertext (Known Plaintext Attack)
def plaintext_attack(c, p_inverse, alphabet):
    return encrypt(c, p_inverse, alphabet)


def main():
    while True:
        # Ask the user what function wants to run
        choice = menu()

        # Get two dictionaries, english alphabet to numbers and numbers to english alphabet
        alphabet, reverse_alphabet = get_alphabet()

        # Run the function selected by the user
        if choice == 1:
            # Asks the user the plaintext and the key for the encryption and checks the input
            plaintext = get_text_input("\nInsert the text to be encrypted: ", alphabet)
            key = get_text_input("Insert the key for encryption: ", alphabet)

            if is_square(key):
                # Get the key matrix k
                k = get_key_matrix(key, alphabet)
                print("\nKey Matrix:\n", k)

                # Get the m-grams matrix p of the plaintext
                p = get_text_matrix(plaintext, k.shape[0], alphabet)
                print("Plaintext Matrix:\n", p)

                input("\nPress Enter to begin te encryption.")
                # Encrypt the plaintext
                c = encrypt(k, p, alphabet)

                # Transform the ciphertext matrix to a text of the alphabet
                ciphertext = matrix_to_text(c, "t", reverse_alphabet)

                print("\nThe message has been encrypted.\n")
                print("Generated Ciphertext: ", ciphertext)
                print("Generated Ciphertext Matrix:\n", c, "\n")
            else:
                print("\nThe length of the key must be a square and >= 2.\n")

        elif choice == 2:
            # Asks the user the ciphertext and the key for the encryption and checks the input
            ciphertext = get_text_input("\nInsert the ciphertext to be decrypted: ", alphabet)
            key = get_text_input("Insert the key for decryption: ", alphabet)

            if is_square(key):
                # Get the key matrix k
                k = get_key_matrix(key, alphabet)

                # Check if the key is invertible and in that case returns the inverse of the matrix
                k_inverse = get_inverse(k, alphabet)

                if k_inverse is not None:
                    # Get the m-grams matrix c of the ciphertext
                    c = get_text_matrix(ciphertext, k_inverse.shape[0], alphabet)

                    print("\nKey Matrix:\n", k)
                    print("Ciphertext Matrix:\n", c)

                    input("\nPress Enter to begin the decryption.")

                    # Decrypt the ciphertext
                    p = decrypt(k_inverse, c, alphabet)

                    # Transform the ciphertext matrix to a text of the alphabet
                    plaintext = matrix_to_text(p, "t", reverse_alphabet)

                    print("\nThe message has been decrypted.\n")
                    print("Generated Plaintext: ", plaintext)
                    print("Generated Plaintext Matrix:\n", p, "\n")
                else:
                    print("\nThe matrix of the key provided is not invertible.\n")
            else:
                print("\nThe key must be a square and size >= 2.\n")

        elif choice == 3:
            # Asks the user the text and the ciphertext to use them for the plaintext attack
            plaintext = get_text_input("\nInsert the plaintext for the attack: ", alphabet)
            ciphertext = get_text_input("Insert the ciphertext of the plaintext for the attack: ", alphabet)

            # Asks the user the length of the grams
            m = get_m()

            if len(plaintext) / m >= m:
                # Get the m-grams matrix p of the plaintext and takes the firsts m
                p = get_text_matrix(plaintext, m, alphabet)
                p = p[:, 0:m]

                # Check if the matrix of the plaintext is invertible and in that case returns the inverse of the matrix
                p_inverse = get_inverse(p, alphabet)

                if p_inverse is not None:
                    # Get the m-grams matrix c of the ciphertext
                    c = get_text_matrix(ciphertext, m, alphabet)
                    c = c[:, 0:m]

                    if c.shape[1] == p.shape[0]:
                        print("\nCiphertext Matrix:\n", c)
                        print("Plaintext Matrix:\n", p)

                        input("\nPress Enter to begin the attack.")

                        # Force the ciphertext provided
                        k = plaintext_attack(c, p_inverse, alphabet)

                        # Transform the key matrix to a text of the alphabet
                        key = matrix_to_text(k, "k", reverse_alphabet)

                        print("\nThe key has been found.\n")
                        print("Generated Key: ", key)
                        print("Generated Key Matrix:\n", k, "\n")
                    else:
                        print("\nThe number of m-grams for plaintext and ciphertext are different.\n")
                else:
                    print("\nThe matrix of the plaintext provided is not invertible.\n")
            else:
                print("\nThe length of the plaintext must be compatible with the length of the grams (m).\n")
        elif choice == 4:
            sys.exit(0)
        input("Press Enter to continue.\n")


if __name__ == '__main__':
    main()

```

### pyc文件字节码反解码

```
import argparse
import logging
import marshal
import opcode
import os
import py_compile
import sys
import math
import string
import types

if sys.version_info < (3, 6):
    sys.exit("Stegosaurus requires Python 3.6 or later")


class MutableBytecode():
    def __init__(self, code):
        self.originalCode = code
        self.bytes = bytearray(code.co_code)
        self.consts = [MutableBytecode(const) if isinstance(const, types.CodeType) else const for const in code.co_consts]


def _bytesAvailableForPayload(mutableBytecodeStack, explodeAfter, logger=None):
    for mutableBytecode in reversed(mutableBytecodeStack):
        bytes = mutableBytecode.bytes
        consecutivePrintableBytes = 0
        for i in range(0, len(bytes)):
            if chr(bytes[i]) in string.printable:
                consecutivePrintableBytes += 1
            else:
                consecutivePrintableBytes = 0

            if i % 2 == 0 and bytes[i] < opcode.HAVE_ARGUMENT:
                if consecutivePrintableBytes >= explodeAfter:
                    if logger:
                        logger.debug("Skipping available byte to terminate string leak")
                    consecutivePrintableBytes = 0
                    continue
                yield (bytes, i + 1)


def _createMutableBytecodeStack(mutableBytecode):
    def _stack(parent, stack):
        stack.append(parent)

        for child in [const for const in parent.consts if isinstance(const, MutableBytecode)]:
            _stack(child, stack)

        return stack

    return _stack(mutableBytecode, [])


def _dumpBytecode(header, code, carrier, logger):
    try:
        f = open(carrier, "wb")
        f.write(header)
        marshal.dump(code, f)
        logger.info("Wrote carrier file as %s", carrier)
    finally:
        f.close()


def _embedPayload(mutableBytecodeStack, payload, explodeAfter, logger):
    payloadBytes = bytearray(payload, "utf8")
    payloadIndex = 0
    payloadLen = len(payloadBytes)

    for bytes, byteIndex in _bytesAvailableForPayload(mutableBytecodeStack, explodeAfter):
        if payloadIndex < payloadLen:
            bytes[byteIndex] = payloadBytes[payloadIndex]
            payloadIndex += 1
        else:
            bytes[byteIndex] = 0

    print("Payload embedded in carrier")


def _extractPayload(mutableBytecodeStack, explodeAfter, logger):
    payloadBytes = bytearray()

    for bytes, byteIndex in _bytesAvailableForPayload(mutableBytecodeStack, explodeAfter):
        byte = bytes[byteIndex]
        if byte == 0:
            break
        payloadBytes.append(byte)

    payload = str(payloadBytes, "utf8")

    print("Extracted payload: {}".format(payload))


def _getCarrierFile(args, logger):
    carrier = args.carrier
    _, ext = os.path.splitext(carrier)

    if ext == ".py":
        carrier = py_compile.compile(carrier, doraise=True)
        logger.info("Compiled %s as %s for use as carrier", args.carrier, carrier)

    return carrier


def _initLogger(args):
    handler = logging.StreamHandler()
    handler.setFormatter(logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'))

    logger = logging.getLogger("stegosaurus")
    logger.addHandler(handler)

    if args.verbose:
        if args.verbose == 1:
            logger.setLevel(logging.INFO)
        else:
            logger.setLevel(logging.DEBUG)

    return logger


def _loadBytecode(carrier, logger):
    try:
        f = open(carrier, "rb")
        header = f.read(12)
        code = marshal.load(f)
        logger.debug("Read header and bytecode from carrier")
    finally:
        f.close()

    return (header, code)


def _logBytesAvailableForPayload(mutableBytecodeStack, explodeAfter, logger):
    for bytes, i in _bytesAvailableForPayload(mutableBytecodeStack, explodeAfter, logger):
        logger.debug("%s (%d)", opcode.opname[bytes[i - 1]], bytes[i])


def _maxSupportedPayloadSize(mutableBytecodeStack, explodeAfter, logger):
    maxPayloadSize = 0

    for bytes, i in _bytesAvailableForPayload(mutableBytecodeStack, explodeAfter):
        maxPayloadSize += 1

    logger.info("Found %d bytes available for payload", maxPayloadSize)

    return maxPayloadSize


def _parseArgs():
    argParser = argparse.ArgumentParser()
    argParser.add_argument("carrier", help="Carrier py, pyc or pyo file")
    argParser.add_argument("-p", "--payload", help="Embed payload in carrier file")
    argParser.add_argument("-r", "--report", action="store_true", help="Report max available payload size carrier supports")
    argParser.add_argument("-s", "--side-by-side", action="store_true", help="Do not overwrite carrier file, install side by side instead.")
    argParser.add_argument("-v", "--verbose", action="count", help="Increase verbosity once per use")
    argParser.add_argument("-x", "--extract", action="store_true", help="Extract payload from carrier file")
    argParser.add_argument("-e", "--explode", type=int, default=math.inf, help="Explode payload into groups of a limited length if necessary")
    args = argParser.parse_args()

    return args


def _toCodeType(mutableBytecode):
    return types.CodeType(
        mutableBytecode.originalCode.co_argcount,
        mutableBytecode.originalCode.co_kwonlyargcount,
        mutableBytecode.originalCode.co_nlocals,
        mutableBytecode.originalCode.co_stacksize,
        mutableBytecode.originalCode.co_flags,
        bytes(mutableBytecode.bytes),
        tuple([_toCodeType(const) if isinstance(const, MutableBytecode) else const for const in mutableBytecode.consts]),
        mutableBytecode.originalCode.co_names,
        mutableBytecode.originalCode.co_varnames,
        mutableBytecode.originalCode.co_filename,
        mutableBytecode.originalCode.co_name,
        mutableBytecode.originalCode.co_firstlineno,
        mutableBytecode.originalCode.co_lnotab,
        mutableBytecode.originalCode.co_freevars,
        mutableBytecode.originalCode.co_cellvars
        )


def _validateArgs(args, logger):
    def _exit(msg):
        msg = "Fatal error: {}\nUse -h or --help for usage".format(msg)
        sys.exit(msg)

    allowedCarriers = {".py", ".pyc", ".pyo"}

    _, ext = os.path.splitext(args.carrier)

    if ext not in allowedCarriers:
        _exit("Carrier file must be one of the following types: {}, got: {}".format(allowedCarriers, ext))

    if args.payload is None:
        if not args.report and not args.extract:
            _exit("Unless -r or -x are specified, a payload is required")

    if args.extract or args.report:
        if args.payload:
            logger.warn("Payload is ignored when -x or -r is specified")
        if args.side_by_side:
            logger.warn("Side by side is ignored when -x or -r is specified")

    if args.explode and args.explode < 1:
        _exit("Values for -e must be positive integers")

    logger.debug("Validated args")


def main():
    args = _parseArgs()
    logger = _initLogger(args)

    _validateArgs(args, logger)

    carrier = _getCarrierFile(args, logger)
    header, code = _loadBytecode(carrier, logger)

    mutableBytecode = MutableBytecode(code)
    mutableBytecodeStack = _createMutableBytecodeStack(mutableBytecode)
    _logBytesAvailableForPayload(mutableBytecodeStack, args.explode, logger)

    if args.extract:
        _extractPayload(mutableBytecodeStack, args.explode, logger)
        return

    maxPayloadSize = _maxSupportedPayloadSize(mutableBytecodeStack, args.explode, logger)

    if args.report:
        print("Carrier can support a payload of {} bytes".format(maxPayloadSize))
        return

    payloadLen = len(args.payload)
    if payloadLen > maxPayloadSize:
        sys.exit("Carrier can only support a payload of {} bytes, payload of {} bytes received".format(maxPayloadSize, payloadLen))

    _embedPayload(mutableBytecodeStack, args.payload, args.explode, logger)
    _logBytesAvailableForPayload(mutableBytecodeStack, args.explode, logger)

    if args.side_by_side:
        logger.debug("Creating new carrier file name for side-by-side install")
        base, ext = os.path.splitext(carrier)
        carrier = "{}-stegosaurus{}".format(base, ext)

    code = _toCodeType(mutableBytecode)

    _dumpBytecode(header, code, carrier, logger)


if __name__ == "__main__":
    main()

```

### rsa大素数分解之后不互素

```
#-*- coding:utf-8 -*-
# 当指数e和Phi(n)不互素时
from Crypto.Util.number import *

import sympy

def gcd(a,b):
    if a < b:
        a,b = b,a
    while b != 0:
        tem = a % b
        a = b
        b = tem
    return a

def invalidExponent(p,q,e,c):
    phiN = (p - 1) * (q - 1)
    n = p * q
    GCD = gcd(e, phiN)
    if (GCD == 1):
        return "Public exponent is valid....."
    d = inverse(e//GCD,phiN)
    c = pow(c, d, n)
    plaintext = sympy.root(c, GCD)
    plaintext = long_to_bytes(plaintext)
    return plaintext


def main():
    e = 0x20002

    c = 69775954010477827342655007357413905879265207201140046408669586721885526123784907133716642304622235420317538384169817488136355157658329703705226141938991105912868209447036610553660972001461632840370922684108791263764483626927583087998066070299767122268085587208956687449243493403662943691619787801332549149107

    p = 12470704223521630361963826771946763220892587623191431207923413178791149916874153397100361890510496084700189763294677638398021009427510131598570281465633547

    q = 12470704223521630361963826771946763220892587623191431207923413178791149916874153397100361890510496084700189763294677638398021009427510131598570281465633283

    plaintext = invalidExponent(p,q,e,c)
    print plaintext

main()

```

