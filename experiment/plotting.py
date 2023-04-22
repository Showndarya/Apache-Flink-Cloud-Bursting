import pandas as pd
import matplotlib.pyplot as plt
arr=[1,5,10,15,20]
for i in arr:
    df = pd.read_csv('../java latency '+str(i)+'.csv', header=None)
    df = df.transpose()
    df['avg'] = df.mean(axis=1)
    print(df)
    ax=df['avg'].plot(kind='line', legend=False)
    ax.set_title('Latency vs batch of '+str(i))
    ax.set_ylabel('Latency (ms)')
    ax.set_xlabel('Num of batches')

    plt.xticks(range(0, len(df.index), int(len(df.index)/10)))
    plt.show()


    df1 = pd.read_csv('../java throughput '+str(i)+'.csv', header=None)
    df1 = df1.transpose()
    df1['avg'] = df1.mean(axis=1)
    print(df1)
    ax=df1['avg'].plot(kind='line', legend=False)
    ax.set_title('Throughput vs batch of '+str(i))
    ax.set_ylabel('Throughput (Events / second)')
    ax.set_xlabel('Num of batches')

    plt.xticks(range(0, len(df.index), int(len(df.index)/10)))
    plt.show()
