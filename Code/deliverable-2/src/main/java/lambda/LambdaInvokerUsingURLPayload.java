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
        InputStream inputStream = LambdaInvokerUsingURLPayload.class.getResourceAsStream("configs/JavaConfig.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String json="";
        String line;
        while ((line = reader.readLine()) != null) {
            json += line;
        }
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String value = jsonObject.get("URL").getAsString();
        String functionUrl = value;
        String payload = payloadString;

        URL url = new URL(functionUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.write(payload.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
        int responseCode = con.getResponseCode();
        InputStream inputStream2 = con.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream2, StandardCharsets.UTF_8));

        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String jsonResponse = response.toString();
        return jsonResponse;
    }
}
