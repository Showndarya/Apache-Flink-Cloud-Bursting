Date - 9 February 2023
Agenda - discuss and clarify the project initial doubts with Professor
Questions - 
Duration - 4:45 PM to 5:50 PM
Paticipants - All group members

Consider a subsystem of the problem as an example:

* Here assume that the source generates random sentences
* The map is simply a flatmap that breaks these sentences into words. Hence it just acts as a tokenizer.
* The sink is the destination

However, consider the scenario where traffic suddenly increases and tokenizer cannot keep up anymore - leading to slowing down of source due to backpressure.

-----------------             -----------------            -----------------
|               |             |                |           |                |
|     src       |   ------->  |      map       | ------->  |      sink      |
|               |             |                |           |                |
-----------------             -----------------            -----------------

There are two main ways to deal with this surge in traffic:
1) Add a bigger machine
2) Add another flatmap, give it a similar task and add it to source. Parse input and then partition the flatmap. When input is down, the flatmap operators can be reduced.

* For this use case, the second solution works because a flatmap has no memory.
* However, it poses overhead since it has to be brought up, channel has to be created and data has to be partitioned.
* Hence if traffic only increases for a very short duration, it can pose a problem due to the additional overhead.


So for our project we will consider a simple use case as below:

-----------------             -----------------            -----------------
|               |             |                |           |                |
|     src       |   ------->  |    flat map    | ------->  |      sink      |
|               |             |                |           |                |
-----------------             -----------------            -----------------
                                       |
                                       |
                                       |
                                       V
                                -----------------           
                                |               | 
                                |    lambda     |
                                |   function    |    
                                -----------------                                   
                   
Take only 1 use case-
* Consider a flatmap that just tokenizes text
* Register it on aws
* Detect when queue fills up for tokenizer(flat map)

When is a queue filled?
------------------------
Queue is internal to flink. Look at flink metrics. Flink metrics periodically reports: Input records per second, Output records per second for every task and for how long the operator is busy. It possibly can also tell us the size of the queue.

Configure flink to send the queue size to your function to determine when offloading is required.

Methods
-------
* Method 1: We can have src call the lambda function. This might be useful since the flat map has not been swamped yet.
* Method 2: Easiest way and probably more scalable for a bigger system is to offload the flatmap operator directly to lambda.

Prof suggested we can try both methods in our initial tests.

Step 1: Decide overload
Step 2: Decide how much to offload

Start with flatmap. For more complex concepts you have to order data after it is returned from lambda function. This is the disadvantage of the Method 1.

How to go about doing it?
--------------------------
1) Write a function to double the rate or half the rate of insource logic
2) Wait for some time and increase/decrease the rate
3) You have the source and should control the rate
4) You should introduce bottlenecks to the point where flink reports that backflow is getting generated -> basically try breaking the system
5) Once this is done, look at Flink metrics to see which indicates that this is happening. Then write a script to extract this metric.
6) Then take part of the load and divert to aws
7) Observe the errors if they are now not present

2 Initial Steps to do for implementation
----------------------------------------
* Check if flat map is getting spawned in aws and returning output
* Write a policy to estimate how much input queue to send to function. How much is the latency to send it to cloud and invoke the function there.

Total latency:
shipping data to cloud + bringing up container to run function + computing time + send data downstream

AWS free tier should be fine for our current experiments. Paid tier is not required.

Sources to generate random data (Benchmarks for Flink)
------------------------------------------------------
1) nexmark
2) yahoo streaming benchmark
                            
Points we are assuming and to be added to our initial design document:
* Not a fault tolerant or secure system
* We assume the operators are stateless

The second assumption is made because if this were not true, then the lamba function result would have to be returned to the operator where the processing was diverged from since state will be maintained locally.

TO-DO:
* Write lambda function in different languages and compare efficiency
* As per Prof. it is expected that Python might be faster than Java

This is because stateless operator speeds depend upon the language used.

Other points:
* Zookeeper is not needed and is usually used in distributed decision making like leader selection and polling - which is not required for our use case.

Task-Distribution
* It is to be done for the discussed basic model.

Future Work:
* Adding multiple operators: If we have multiple operators, the first task that is quite difficult is to identify which operator is causing the bottleneck. Hence we consider just one operator.
* Considering stateful operators: Stateful operators will have to have results returned to the processing operator itself since lambda functions are stateless. 
* Considering latency when diverged from source to lamba function:
