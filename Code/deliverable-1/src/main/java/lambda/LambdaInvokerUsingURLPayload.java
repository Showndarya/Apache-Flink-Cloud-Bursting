package lambda;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LambdaInvokerUsingURLPayload {

    public static String invoke_lambda(String payloadString) throws Exception {
        //String payloadString = "nothing now";
        String functionUrl = "https://bm4bhvxuug3pxxl6q2plsdg2540ssaih.lambda-url.us-east-1.on.aws/";
        String payload = "{\n" +
                "  \"body\": " + payloadString + "\n" +
                "}";
        URL url = new URL(functionUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//        con.setRequestMethod("GET");
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.write(payload.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
        int responseCode = con.getResponseCode();
//        System.out.println("response code", String.valueOf(responseCode));
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println("Response: " + response);
        return response.toString();
    }
}
