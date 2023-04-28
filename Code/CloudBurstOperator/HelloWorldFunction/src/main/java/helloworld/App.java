package helloworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.google.gson.Gson;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private boolean isWarmed = false;

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        Gson gson = new Gson();

        if (!isWarmed) {
            // Do warm-up logic here
            try {
                String pageContents = getPageContents("https://www.example.com");
                // Do something with page contents to simulate
                // workload
            } catch (IOException e) {
                // Handle error
            }
            isWarmed = true;
        }

        String json =input.getBody();
        List<String> data = gson.fromJson(json, ArrayList.class);

        List<String> responses = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\W");
        for (String text : data) {
            String[] values = pattern.split(text);
            responses.addAll(Arrays.asList(values));
        }

        String output = String.format("\"%s\"", responses);
        return response
                .withStatusCode(200)
                .withBody(output);
    }

    private String getPageContents(String address) throws IOException{
        URL url = new URL(address);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
