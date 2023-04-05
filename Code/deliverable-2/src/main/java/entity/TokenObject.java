package entity;

import java.util.List;

public class TokenObject {
    private List<List<String>> data;

    @Override
    public String toString() {
        return "TokenObject{" +
                "data=" + data +
                '}';
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }

    public TokenObject(List<List<String>> data) {
        this.data = data;
    }
}
