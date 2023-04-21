import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv('../new warm start latency10.csv', header=None)
df = df.transpose()
df['avg'] = df.mean(axis=1)
print(df)
ax=df['avg'].plot(kind='line', legend=False)
ax.set_title('Latency through num of batches')
ax.set_ylabel('Latency (ms)')
ax.set_xlabel('Num of batches')

plt.xticks(range(0, len(df.index), 10))
plt.show()


df1 = pd.read_csv('../new warm start throughput10.csv', header=None)
df1 = df1.transpose()
df1['avg'] = df1.mean(axis=1)
print(df1)
ax=df1['avg'].plot(kind='line', legend=False)
ax.set_title('Throughput through num of batches')
ax.set_ylabel('Throughput (Events / second)')
ax.set_xlabel('Num of batches')

plt.xticks(range(0, len(df.index), 10))
plt.show()

print(df['avg'].mean(axis=0))