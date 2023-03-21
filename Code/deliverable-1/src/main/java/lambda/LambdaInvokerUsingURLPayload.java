package lambda;

import configs.JsonReader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class LambdaInvokerUsingURLPayload {


    public static String invoke_lambda(String payloadString) throws Exception {
//        String json1Path = LambdaInvokerUsingURLPayload.class.getClassLoader().getResource("configs/" + "JavaConfig.json").getPath();
        String configFilePath = "configs/JavaConfig.json";

        // Get the absolute path to the config file
        Path configPath = Paths.get("Code","deliverable-1", "src", "main", "java", configFilePath);
        String absoluteConfigPath = configPath.toFile().getAbsolutePath();

        JsonReader reader = new JsonReader(absoluteConfigPath);
        String value = reader.read("URL");
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
