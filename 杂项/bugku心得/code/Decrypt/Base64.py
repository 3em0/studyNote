import base64
a = "TXpVek5UTTFNbVUzTURabE5qYz0"
print(type(a))
print(a.encode())
while 1:
     a=base64.b64decode(a)
     print(a)