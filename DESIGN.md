<h4 style="text-align: center;"> Addressing transient workload spikes with Cloud bursting in Apache Flink <br/>
Design Document | Team 6 </h4>
<hr />

## 1. **Problem statement:** 
  - **Problem**:  that this project aims to address is to provide a solution for handling sudden spikes in workload in Apache Flink applications. This is an important problem because Apache Flink is a distributed stream processing framework that is designed to handle large amounts of data in real-time. However, when the input rate exceeds the processing capacity, the system can become overwhelmed, leading to increased latency, decreased performance, and even failures.
  - **Technique**:  The cloud bursting technique is an alternative to back-pressure, which is a mechanism that slows down the input rate when the system is overwhelmed. Instead, cloud bursting allows the excess workload to be offloaded to a cloud provider, such as AWS, where it can be processed in parallel. This can help to ensure that the system continues to operate at a high level of performance, even when there is a sudden spike in workload. 
  - **Target audience**:  people who will benefit from solving this problem include organizations that use Apache Flink for real-time data processing, particularly in cases where the workload can fluctuate rapidly. This solution can help to improve the reliability and performance of these systems, and make them more scalable and cost-effective. Additionally, this solution can benefit developers and data scientists who use Apache Flink, by providing them with a more robust and flexible tool for processing large amounts of data in real-time.
## 2. **Proposed Solution:**
> ### Components:
  + Source
    + Nexmark
    + Yahoo Streaming Benchmark
  + Operator
    + Flatmap in flink(Java)
    + Flatmap in flink(Python)
  + Cloud
    + AWS Lambda Function
    + Azure Functions
  + Sink
    + Any operator or sink

> ### Explain:
  - The data is generated from one of the source, and is processed in the operator.   
  - Finally, the result is emitted to the sink. Flink metrics can be used to detect the traffic of the input event stream. 
  - Based on what we know, we might use metrics types including **Gauge or Meters** to measure information such as JVM heap usage, event input rate to detect the traffic in the operator. 
  - Based on these metrics, we might do many experiment to determine the suitable offloading portion which gives us the balance between resources (used for AWS lambda function) and processing speed (throughput, latency etc.).     
  - For invoking the cloud function, we will use the AWS SDK for Java to dynamically invoke the AWS Lambda function. In case of the first milestone, we will send tokenize the string i.e. perform the same function as the flatmap operator.
  - Thus, we will send the string we want to offload to the AWS and call the invoke method. After getting the result, we do the deserialization of the  InvokeResult object in the operator and send the result to the downstream.

> ### Why is this suitable:
  1. We choose flatmap as a start because it is stateless. We don't need to consider
  aggregation operation and order of the event.
  2. We choose to send the event from operator to cloud and send back data from cloud to operator.
  Because it is easier.
  3. We choose to offload event to cloud, thus we can intuitively observe the difference of workload between
  using only operator and using cloudburst technique.

## 3. **Expectations:**

  - When a bursting workload happens, we expect our solution will detect the bursting input stream and locate the bottleneck node.
  - Then our solution will decide whether cloud bursting is necessary and will spawn lambda functions to handle the excess load.
  - We expect our solution will reduce the burden on the bottleneck node and achieve better performance than backpressure and is more efficient and scalable.
  - Specifically, we expect our solution will successfully solve the congestion during bursting moment and compared with backpressure, it will have
    1. smaller latency.
    2. greater throughput.
    3. faster processing time on bottleneck node. 

## 4. **Experimental Plan:** <br />
  > ### Assumptions: 
  - The system is fault tolerant
  - The system is very secure and doesn't need any additional security features <br />
  - Based on the progress of the project, we might add or remove some metrics/experiments
    We do plan to incorporate all of the above in our future work.
  > ### Step 1
   
   We will start with a subsystem of the problem,
   ![basic design](basic%20design.png)<br />
   For the above figure, we have the following steps - <br />
    Experimental Setup - <br />
   - We consider a flat map that tokenizes text as our operator. For the first milestone, we consider that our operators are stateless.
   - We generate random data using Flink's 'RandomSource' as a source, to generate random data, and Flink's 'ThrottleFunction' to control the rate at which the data is emitted.
   - We will use the following metrics provided by Flink to detect if the operator is overloaded -
     - Input/Output Rates: An operator's input/output rates can be monitored to detect if the operator receives more data than it can handle. If the input rate exceeds the output rate, it may indicate that the operator is overloaded.
     - Processing Time: The processing time metric measures the time an operator takes to process a single record. If the processing time is high, it may indicate that the operator is overloaded.
     - Memory Usage: Memory usage metrics can monitor the amount of memory an operator uses. If an operator is using too much memory, it may indicate that it is overloaded.
     - Backpressure: Flink's backpressure mechanism can detect whether an operator receives more data than it can process. If an operator is experiencing backpressure, it may indicate that it is overloaded.
   - Register our lambda function on AWS
   - Connect our lambda function to flat map operator
    Experiments - <br />
    The below experiments are proposed based on the above solution and are subject to change as we progress. <br />
   - By comparing the metrics defined above, we can detect whether the flatMap function is processing records at the same rate at which they are arriving or whether it is falling behind. We detect the threshold at which the operator starts having backpressure and formulate an expression based on the number of nodes, operator type, and the metrics defined above. <br />
     The threshold might vary based on the - 
     1. the dataflow graph 
     2. sources
     3. input rate
     We record the processing time the flat map uses using Flink's built-in histogram metric. If the processing time histogram exceeds a certain threshold, we launch our lambda function and offload work to it. <br />
     While offloading, we need to experiment with the following parameters -
     - Latency Test - We define latency based on the - 
       1. Time taken to send data to cloud lambda instance,
       2. Bringing up the aws container to run the lamda function on,
       3. Carry out the computation, 
       4. Sending the data downstream,
       5. Merge the results produced by the operator and the lambda function. 
       We compare this latency against the scenario when we are not using cloud bursting at all.
     - Throughput Test -Analyzing the effect of using cloud bursting on throughput. We calculate the effect of cloud bursting on the amount of backpressure the pipeline faces on increased loads compared to the original pipeline(without cloud bursting).<br />
       To perform a throughput and latency test on a Flink job, we will use Flink's built-in benchmarking tool('Benchmark') to measure the processing rate and latency of a Flink job under different conditions. The 'BenchmarkConfig' class is used to configure the benchmarking tool with the desired parameters, such as the job name, the number of task managers and the parallelism of the job, the number of warmup and benchmark iterations, and the interval between iterations. The Benchmark.runWithConfig() method runs the Flink job and measures its performance using the configured benchmarking parameters. The resulting BenchmarkResult object contains the measured throughput and latency of the job.
   > ### Step 2

  - After working with one stateless operator, we will experiment with stateful operators and move on to graphs with operator chaining of both stateless and stateful and, finally, with more complicated dataflow graphs such as with multiple operators, along with trying to reduce CPU/memory utilization.

## 5. **Success Indicators:**
  - Data flow pipline in flink works as it was intended without data/packet loss.
  - Cloud bursting technique should improve the performance of the entire dataflow with respect to latency and act as an alternative to back pressure.
  - Architecture tuned enough to make the best decisions on when to offload the data packets to the cloud function without incurring more processing delay than the original architecture.
  - Devise a method to handle data merge and data propagation with respect to both stateful and stateless operators without loss of order or data.
  - Cloud function instances are created/destroyed based on requirement, reducing cost and resource utilization
  - > ### Intermidiate milestones:
    - Setting up the basic Flink pipeline with 1 stateles operator and connecting the operator to the lambda function to experiment with the performance and scenarios.
    - Extending the Flink pipeline setup to test the setup against 1 stateful operator.
    - Experimenting with the setup of cloud bursting with a combination of stateful and stateless operators (operator chaining). Identifying the operator causing the bottleneck before attaching the lambda function would be an additional step
    - Experimenting with the setup with complicated dataflow graphs to estimate throughput improvement, latency improvement and overall performance impact of the cloud bursting technique.

## 6. **Task assignment:**

| **Task**            | **Subtask**                                               |  **Assigned To**  |
| :--------------- |:----------------------------------------------------- |:-------------:|
| Prerequisites:  | 1)how to generate generic data type for flat map       |  All          |
| src->tokenizer->fmap          | 2)read through flink documentation      |  Team         |
|  | 3)get input events in flat map irrespective of source                 |  Members      | 
|                 |                                                       |               |
|                 |                                                       |               |
| Bash script     | 1)generate events at specific interval based on need  |  Sakshi       |
|                 |                                                       |               |
|                 |                                                       |               |
| Process Function| 1)how to build the function                           |  All          |
|                 | 2)restrictions of the function                        |  Team         |
|                 | 3))similarity to flat map -> are they interchangeable? |  Members      |
|                 |                                                       |               |
|                 |                                                       |               |
| Flink Pipeline  | 1)setup Flink pipeline -> jar required for step       |  Yujie Yan    |
|                 | 2)test the initial pipeline                           |  Ye Tian      |
|                 |                                                       |               |
|                 |                                                       |               |
| Metrics         | 1)get metrics from Flink documentation                |  Sakshi Sharma|
|                 | 2)gather metric data under normal conditions          |  Sanath Bhimsen|
|                 | 3)generate back pressure in pipeline                  |               |
|                 | 4)gather second set of metrics                        |               |
|                 |                                                       |               |
|                 |                                                       |               |
| Cloud Integration| 1)setup cloud resources                               |Showndarya Madhavan|
|                 | 2)setup integration with Flink->Cross platform        |Anwesha Saha   |
|                 | 3)connections to 3rd party systems                    |               |
|                 | 4)expose lambda to outside environment                |               |
|                 |                                                       |               |
|                 |                                                       |               |
| Final Integration| 1)stop cloud and verify data goes to flink           |  All          |
| Create->Test->Overload->Test| 2)stop flink and verify data goes to cloud|  Team         |
|                 | 3)ensure no loss of data                              |  Members      |
|                 |                                                       |               |
|                 |                                                       |               |
| Experiment ->   | 1)model performs load balancing by offloading to aws lambda when overloaded|  All          |
| Identify success indicators are met|                                    |  Team         |
|                 |                                                       |  Members      |
|                 |                                                       |               |

> ###  Notes:
- The task distribution was made keeping in mind the strengths and weaknesses of each team member. 
- All of us collectively have some Java coding experience whereas Flink and Stream processing is a new area that we are delving into. 
- On the basis of this, we decided to perform some of the tasks as a group. 
- The tasks assigned individually are the ones we believe can be performed parallely where multiple group members perform separate tasks which can then be integrated. 
- Having said this, we have considered that any of us might face difficulty in the area we have assigned ourselves and will redistribute or discuss accordingly whenever required. 