<h4 style="text-align: center;"> Addressing transient workload spikes with Cloud bursting in Apache Flink <br/>
Design Document | Team 6 </h4>
<hr />

1. **Problem statement:** 
2. **Proposed Solution:**
3. **Expectations:**
4. **Experimental Plan:**
5. **Success Indicators:**
6. **Task assignment:**

| Task            | Subtask                                               |  Assigned To  |
| :--------------- |:----------------------------------------------------- |:-------------:|
| Prerequisites:  | 1)how to generate generic data type for flatmap       |  All          |
| src->tokenizer->fmap          | 2)read through flink documentation      |  Team         |
|  | 3)get input events in flatmap irrespective of source                 |  Members      | 
|                 |                                                       |               |
|                 |                                                       |               |
| bash script     | 1)generate events at specific interval based on need  |  Sakshi       |
|                 |                                                       |               |
|                 |                                                       |               |
| Process Function| 1)how to build the function                           |  All          |
|                 | 2)restrictions of the function                        |  Team         |
|                 | 3))similarity to flatmap -> are they interchangeable? |  Member       |
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
|                 |                                                       |               |
|                 |                                                       |               |