def countWays(n):
    # 请在下面编写代码
    if n == 1:
        return 1
    elif n == 2:
        return 2
    else:
        return countWays(n-1) + countWays(n-2) * 2

print(countWays())