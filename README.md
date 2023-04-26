<h4 style="text-align: center;"> Addressing transient workload spikes with Cloud bursting in Apache Flink <br/>
Design Document</h4>
<hr />

**Problem statement:** 
  - **Problem**:  This project aims to address is to provide a solution for handling sudden spikes in workload in Apache Flink applications. This is an important problem because Apache Flink is a distributed stream processing framework that is designed to handle large amounts of data in real-time. However, when the input rate exceeds the processing capacity, the system can become overwhelmed, leading to increased latency, decreased performance, and even failures.
  - **Technique**:  The cloud bursting technique is an alternative to back-pressure, which is a mechanism that slows down the input rate when the system is overwhelmed. Instead, cloud bursting allows the excess workload to be offloaded to a cloud provider, such as AWS, where it can be processed in parallel. This can help to ensure that the system continues to operate at a high level of performance, even when there is a sudden spike in workload. 

**Source**:

Nexmark is a benchmark suite for queries over continuous data streams. This project is inspired by the [NEXMark research paper](https://web.archive.org/web/20100620010601/http://datalab.cs.pdx.edu/niagaraST/NEXMark/) and [Apache Beam Nexmark](https://beam.apache.org/documentation/sdks/java/testing/nexmark/).

These are multiple queries over a three entities model representing on online auction system:

Person represents a person submitting an item for auction and/or making a bid on an auction.
Auction represents an item under auction.
Bid represents a bid for an item under auction.

**Basic Architecture**:

![Architecture](assets/Architecture.png "Architecture")

**Run instructions**:

Environment -
- Java 8 
- Flink version - 1.16.0
- Intellij

Using Intellij - 
1. Build your Maven project using the command `mvn clean package`, which will generate a JAR file for the Flink pipeline, or create a maven run configuration in `Intellij` to run the command `clean package`
2. Install all dependencies using maven.
3. Create another application run configuration with `main` class as `operator.FlinkPipeline`. Select the `Add dependencies with 'provided' scope to classpath` option. Set the working directory as `team-6/Code/deliverable-2`.  
4. Build the project using the application run configuration to generate the jar files in the `target` folder.


Using Command Line (MacOS) - 
1. Install Maven `brew install maven`
2. Create ~/.mavenrc file and add line `export JAVA_HOME=<path>`. You can get the path using ` /usr/libexec/java\_home`. We have built the project with `java8` as the version, so that's the version we can assure you.
3. Go to `Code/deliverable-2` directory and run `mvn clean package`
4. This should create the `flink-java-project-0.1.jar` file under the `target` folder.

To run on Flink web UI -
1. Start the Flink cluster ( ./start-cluster.sh )
2. Go to http://localhost:8081 in your browser
3. Upload the jar file to the Flink web UI under the `submit a job` tab

**Source Configurations**:

To change configurations at the source, navigate to the file `NexmarkConfiguration.java` under `team-6/Code/deliverable-2`.

For instance, for changing the `input rate` change the configuration variable `firstEventRate` and `nextEventRate` and change the value of variable `ratePeriodSec` for changing the duration of the spike.

**Lambda Deployments**:

Although the lambda functions are already deployed with an exposed API gateway, the following are the steps to deploy the lambda functions in your account.

- **Setup IAM**:
1. Go to AWS account > IAM
2. Click on `Add user` and add a new user with `Programmatic Access`
3. Add permissions for `AWSLambdaFullAccess`
4. Save the `Access ID` and `Secret Access Key` for the new user.

- **Python**: 
1. Run `npm i -g serverless` in terminal.
2. Navigate to `team-6/Code/PythonCloudBurstOperator/cloud-burst-operator-python`
3. Run `serverless config credentials --provider aws --key <access-id> --secret <secret-access-key>`
4. Run `serverless deploy`

- **Java**: 
1. Install `AWS SDK` tools for Java Intellij.
2. Ensure `Docker` is installed and is properly running.
2. Navigate to `team-6/Code/CloudBurstOperator`
3. Right-click on the project root in Intellij and click on `Serverless Application sync` option.