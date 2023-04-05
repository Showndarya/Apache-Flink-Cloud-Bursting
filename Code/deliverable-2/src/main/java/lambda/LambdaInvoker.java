package lambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import java.nio.charset.StandardCharsets;

public class LambdaInvoker {

    public static void main(String[] args) {
        String functionName = "my-function";
        String payload = "{\"name\":\"Alice\"}";
        AWSLambda lambda = AWSLambdaClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        InvokeRequest request = new InvokeRequest()
                .withFunctionName(functionName)
                .withPayload(payload);
        InvokeResult result = lambda.invoke(request);
        String responsePayload = new String(result.getPayload().array(), StandardCharsets.UTF_8);
        System.out.println("Response: " + responsePayload);
    }


}
