<h4 style="text-align: center;"> Addressing transient workload spikes with Cloud bursting in Apache Flink <br/>
Design Document | Team 6 </h4>
<hr />

1. **Problem statement:**
## 2. **Proposed Solution:**
> ### Components:
> + Source
    >   + Nexmark
>   + Yahoo Streaming Benchmark
> + Operator
    >   + Flatmap in flink(Java)
>   + Flatmap in flink(Python)
> + Cloud
    >   + AWS Lambda Function
>   + Azure Functions
> + Sink
    >   + Any operator or sink
>
> ### Explain:
> The data is generated from one of the source, and is processed in the operator. Finally,
> the result is emitted to the sink. Flink has metrics to sense the high traffic, and then it offload half
> of the workload to the lambda function on the cloud. After the computation is done in the lambda function,
> it sends the result back to the operator. Finally, the operator sends the data to the sink or another operator.
>
> ### Why is this suitable:
> 1. We choose flatmap as a start because it is stateless. We don't need to consider
     > about aggregation operation and order of the event.
> 2. We choose to send the event from operator to cloud and send back data from cloud to operator.
     > Because it is easier.
> 3. We choose to offload event to cloud, thus we can intuitively observe the difference of workload between
     > using only operator and using cloudburst technique.

3. **Expectations:**
4. **Experimental Plan:**
5. **Success Indicators:**
6. **Task assignment:**