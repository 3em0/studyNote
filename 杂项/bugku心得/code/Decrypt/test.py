from random import *
import tornado
def minPathCost_Memo(cost, i, j, memo):
    # 请在下面编写代码
    for x in range(1,i+1):
        memo[x][0] = memo[x-1][0] + cost[x][0]
    for y in range(1,j+1):
        memo[0][y] = memo[0][y-1] + cost[0][y]
    for x in range(1,i+1):
        for y in range(1,j+1):
            memo[x][y] = min(memo[x-1][y], memo[x][y-1]) + cost[x][y]
    return memo[i][j]

randseeds = [9, 99, 999, 9999, 99999]
for trial in range(5):
    seed(randseeds[trial])
    cost = []
    for i in range(100):
        temp = []
        for j in range(100):
            temp.append(randint(1, 10))
        cost.append(temp)
memo = []
for i in range(100):
    memo.append([0] * 100)
print(minPathCost_Memo(cost, 99, 99, memo))