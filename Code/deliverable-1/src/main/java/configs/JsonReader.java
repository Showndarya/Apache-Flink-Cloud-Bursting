package configs;

import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonReader {
    private String filePath;

    public JsonReader(String filePath) {
        this.filePath = filePath;
    }

    public String read(String key) {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            String value = (String) jsonObject.get(key);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
