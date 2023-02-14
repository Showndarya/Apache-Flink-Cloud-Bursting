<h4 style="text-align: center;"> Addressing transient workload spikes with Cloud bursting in Apache Flink <br/>
Design Document | Team 6 </h4>
<hr />

1. **Problem statement:** 
    - **Problem**:  that this project aims to address is to provide a solution for handling sudden spikes in workload in Apache Flink applications. This is an important problem because Apache Flink is a distributed stream processing framework that is designed to handle large amounts of data in real-time. However, when the input rate exceeds the processing capacity, the system can become overwhelmed, leading to increased latency, decreased performance, and even failures.
    - **Technique**:  The cloud bursting technique is an alternative to back-pressure, which is a mechanism that slows down the input rate when the system is overwhelmed. Instead, cloud bursting allows the excess workload to be offloaded to a cloud provider, such as AWS, where it can be processed in parallel. This can help to ensure that the system continues to operate at a high level of performance, even when there is a sudden spike in workload.
    - **Target audience**:  people who will benefit from solving this problem include organizations that use Apache Flink for real-time data processing, particularly in cases where the workload can fluctuate rapidly. This solution can help to improve the reliability and performance of these systems, and make them more scalable and cost-effective. Additionally, this solution can benefit developers and data scientists who use Apache Flink, by providing them with a more robust and flexible tool for processing large amounts of data in real-time.
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
   
    We choose to implement a simple architecture first and incrementally add functionalities and variations to it later. The first version is expected to be delivered in the mid-term which comprises only a basic functional Flink pipeline and a node connecting to AWS Lambda Function. This approach is fast to kick start and flexible for future evolutions. During the first phase of this project, we expect every member of our team will have a  rudimentary understanding of Flink and Lambda Function regardless of their roles and there should be no blocker during this process.

   An alternative approach is developing the fully functional cloud burst mechanism from scratch. The advantage of this approach is that it reduces the overall workload. However, this introduces more complexity in project management and might produce more workloads in late stage development.

4. **Experimental Plan:** <br />
    > Assumptions: 
    - The system is fault tolerant. 
    - The system is very secure and doesn't need any additional security features <br />
      We do plan to incorporate all of the above in our future work
   
   > Step 1
   
   We will start with a subsystem of the problem,
   ![basic design](basic%20design.png)
   For the above figure, we have the following steps - 
   - We consider a flatmap that just tokenizes text as our operator
   - Find out the metrics affecting the streaming pipeline
   - Register our lambda function on aws
   - Connect our lambda function to our operator
   - Detect when queue fills up for tokenizer(flat map) and direct half of the load to our lambda function
   - Merge the results produced by the operator and the lambda function
   - Measure the latency of offloading the work to lambda function
   > Step 2 
   
   After implementing the first prototype in step 1, we plan to evaluate our streaming pipeline by -
      - Connecting the lambda function to the source or to the sink instead of the operator
      - Considering different language implementations on the lambda function(like python, Java)
      - Working with more complicated dataflow graphs, including stateful and stateless operators


5. **Success Indicators:**
    - Data flow pipline in flink works as it was intended without data/packet loss.
    - Cloud bursting technique should improve the performance of the entire dataflow with respect to latency and act as an alternative to back pressure.
    - Architecture tuned enough to make the best decisions on when to offload the data packets to the cloud function without incurring more processing delay than the original architecture.
    - Devise a method to handle data merge and data propagation with respect to both stateful and stateless operators without loss of order or data.
    - Cloud function instances are created/destroyed based on requirement, reducing cost and resource utilization
6. **Task assignment:**