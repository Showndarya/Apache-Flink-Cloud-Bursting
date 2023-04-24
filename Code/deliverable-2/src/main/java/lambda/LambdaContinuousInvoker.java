package lambda;//import com.amazonaws.services.lambda.AWSLambda;
//import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
//import com.amazonaws.services.lambda.model.InvokeRequest;
//import com.amazonaws.services.lambda.model.InvokeResult;
//
//public class LambdaContinuousInvoker {
//
//    public static void main(String[] args) {
//        AWSLambda client = AWSLambdaClientBuilder.standard().build();
//        String functionArn = "arn:aws:lambda:us-east-1:123456789012:function:myFunction";
//        String input = "dummy_initial_input";
//
//        //we will invoke lambda function each time till lambda sends us an end of line character
//        while (true) {
//            InvokeRequest request = new InvokeRequest()
//                    .withFunctionArn(functionArn)
//                    .withPayload(input);
//
//            InvokeResult result = client.invoke(request);
//            String output = new String(result.getPayload().array());
//
//            System.out.println(output);
//
//            // Check if the end of line character has been sent
//            if (output.contains("?/?")) {
//                break;
//            }
//
//            // Get new input after each iteration
//            input = getNewInput();
//        }
//    }
//
//    private static String getNewInput() {
//        //write logic here to get fresh inputs
//        return "new input";
//    }
//}
