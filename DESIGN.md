<h4 style="text-align: center;"> Addressing transient workload spikes with Cloud bursting in Apache Flink <br/>
Design Document | Team 6 </h4>
<hr />

1. **Problem statement:** 
2. **Proposed Solution:**
3. **Expectations:**
4. **Experimental Plan:**
5. **Success Indicators:**
- Data flow pipline in flink works as it was intended without data/packet loss.
- Cloud bursting technique should improve the performance of the entire dataflow with respect to latency as an alternative back pressure which slows down the entire dataflow.
- Architecture tuned enough to make the best decisions on when to offload the data packets to the cloud function without incurring more processing delay than the original architecture.
- Devise a method to handle data merge and data propagation with respect to both stateful and stateless operators without loss of order or data.
- Cloud function instances are created/destroyed based on requirement, reducing cost and resource utilization
6. **Task assignment:**