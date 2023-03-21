package lambda;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class LambdaInvokerUsingURLPayload {

    public static String invoke_lambda(String payloadString) throws Exception {
//        String json1Path = LambdaInvokerUsingURLPayload.class.getClassLoader().getResource("configs/" + "JavaConfig.json").getPath();
        String path = "Code/deliverable1/src/main/java/configs/JavaConfig.json";
        String base = "Code/deliverable1/";
        String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();

        String json = "";
        try (BufferedReader br = new BufferedReader(new FileReader(relative))) {
            String line;
            while ((line = br.readLine()) != null) {
                json += line;
            }
        }
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String value = jsonObject.get("URL").getAsString();
        String functionUrl = value;
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
