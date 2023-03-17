package lambda;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LambdaInvokerUsingURL {

    public static void main(String[] args) throws Exception {
        String functionUrl = "https://bm4bhvxuug3pxxl6q2plsdg2540ssaih.lambda-url.us-east-1.on.aws/";
        URL url = new URL(functionUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Access-Control-Allow-Origin", "*");
        con.setRequestProperty("Access-Control-Allow-Methods", "GET, POST, PUT");
        con.setRequestProperty("Access-Control-Allow-Headers", "Content-Type");

        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println("Response: " + response.toString());
    }
}
