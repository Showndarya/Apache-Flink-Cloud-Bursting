import pandas as pd
import matplotlib.pyplot as plt
arr=[10,20,40,80,160,320,640,1280]
data=[]
for i in arr:
    df = pd.read_csv('../java latency '+str(i)+'.csv', header=None)
    df=df.drop(df.columns[-1],axis=1)
    df = df.transpose()
    df['avg'] = df.mean(axis=1)

    data.append(df['avg'])

plt.boxplot(data)
plt.xticks(range(1,9), arr)

# set the title and labels
plt.title('Box Plot for different batches java')
plt.xlabel('Size of Batch')
plt.ylabel('Latency (ms)')

# show the plot
plt.show()

avgdata=[d.mean() for d in data]
print(avgdata)
plt.plot(arr,avgdata)
# plt.xticks(arr)
plt.title('Average Latency for different batch size java')
plt.xlabel('Size of Batch')
plt.ylabel('Latency (ms)')




plt.show()


df1 = pd.read_csv('../java throughput.csv', header=None)
df1=df1.drop(df1.columns[-1],axis=1)
df1['avg'] = df1.mean(axis=1)
print(df1)
# ax=df1['avg'].plot(x=range(1,11),y=df1['avg'],kind='line', legend=False)
arrt = [str(i) for i in arr]
plt.bar(arrt,df1['avg'])
plt.title('Throughput vs batch')
plt.ylabel('Throughput (Events / second)')
plt.xlabel('Num of batches')
# plt.figure(figsize=(15,8))


plt.show()

# arr=[10,20,40,80,160,320,640,1280]
#
# data = []
# for i in arr:
#     df = pd.read_csv('../python latency '+str(i)+'.csv', header=None,na_values='')
#     df=df.drop(df.columns[-1],axis=1)
#     df = df.transpose()
#     df['avg'] = df.mean(axis=1)
#     data.append(df['avg'])
#
# # print(data)
# plt.boxplot(data)
# avgdata=[d.mean() for d in data]
# plt.xticks(range(1,9), arr)
#
# # set the title and labels
# plt.title('Box Plot for different batches python')
# plt.xlabel('Size of Batch')
# plt.ylabel('Latency (ms)')
#
# # show the plot
# plt.show()
# # print(avgdata)
# plt.plot(arr,avgdata)
# # plt.xticks(arr)
# plt.title('Average Latency for different batch size python')
# plt.xlabel('Size of Batch')
# plt.ylabel('Latency (ms)')
# plt.show()
#
# df1 = pd.read_csv('../python throughput.csv', header=None)
# df1=df1.drop(df1.columns[-1],axis=1)
# df1['avg'] = df1.mean(axis=1)
# # ax=df1['avg'].plot(x=range(1,11),y=df1['avg'],kind='line', legend=False)
# print(df1['avg'])
# arrt = [str(i) for i in arr]
# plt.bar(arrt,df1['avg'])
# plt.title('Throughput vs batch python')
# plt.ylabel('Throughput (Events / second)')
# plt.xlabel('Num of batches')
#
#
#
# plt.show()