<h4 style="text-align: center;"> Addressing transient workload spikes with Cloud bursting in Apache Flink <br/>
Design Document | Team 6 </h4>
<hr />

1. **Problem statement:** 
    - **Problem**:  that this project aims to address is to provide a solution for handling sudden spikes in workload in Apache Flink applications. This is an important problem because Apache Flink is a distributed stream processing framework that is designed to handle large amounts of data in real-time. However, when the input rate exceeds the processing capacity, the system can become overwhelmed, leading to increased latency, decreased performance, and even failures.
    - **Technique**:  The cloud bursting technique is an alternative to back-pressure, which is a mechanism that slows down the input rate when the system is overwhelmed. Instead, cloud bursting allows the excess workload to be offloaded to a cloud provider, such as AWS, where it can be processed in parallel. This can help to ensure that the system continues to operate at a high level of performance, even when there is a sudden spike in workload.
    - **Target audience**:  people who will benefit from solving this problem include organizations that use Apache Flink for real-time data processing, particularly in cases where the workload can fluctuate rapidly. This solution can help to improve the reliability and performance of these systems, and make them more scalable and cost-effective. Additionally, this solution can benefit developers and data scientists who use Apache Flink, by providing them with a more robust and flexible tool for processing large amounts of data in real-time.
2. **Proposed Solution:**
3. **Expectations:**
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

| Task            | Subtask                                               |  Assigned To  |
| :--------------- |:----------------------------------------------------- |:-------------:|
| Prerequisites:  | 1)how to generate generic data type for flatmap       |  All          |
| src->tokenizer->fmap          | 2)read through flink documentation      |  Team         |
|  | 3)get input events in flatmap irrespective of source                 |  Members      | 
|                 |                                                       |               |
|                 |                                                       |               |
| Bash script     | 1)generate events at specific interval based on need  |  Sakshi       |
|                 |                                                       |               |
|                 |                                                       |               |
| Process Function| 1)how to build the function                           |  All          |
|                 | 2)restrictions of the function                        |  Team         |
|                 | 3))similarity to flatmap -> are they interchangeable? |  Members      |
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

