package lambda;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import java.nio.charset.StandardCharsets;

public class LambdaInvokerUsingARN{

    public static void main(String[] args) {
        String functionArn = "arn:aws:lambda:us-east-1:974678534257:function:cloud-burst-HelloWorldFunction-u1dDzN3vJMUS";
        String accessKey = "AKIAWP4Y7KNACPNP6DHK";
//        String secretKey = "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";
//        AWSLambda lambda = AWSLambdaClientBuilder.standard()
//                .withRegion(Regions.US_EAST_1)
//                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
//                .build();
//        InvokeRequest request = new InvokeRequest()
//                .withFunctionArn(functionArn);
//        InvokeResult result = lambda.invoke(request);
//        String responsePayload = new String(result.getPayload().array(), StandardCharsets.UTF_8);
//        System.out.println("Response: " + responsePayload);
    }
}
