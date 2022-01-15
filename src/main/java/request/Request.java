package request;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private String requestLine;
    private Map<String, String> headers = new HashMap<>();
    private InputStream body;
    private Map<String, String> params = new HashMap<>();

    public Request(InputStream inputStream) throws IOException {
        var in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        if ((line = in.readLine())!=null) {
            this.requestLine = line;
            //List<NameValuePair> list = URLEncodedUtils.parse(line, StandardCharsets.UTF_8);
            String[] requestLineSplit = line.split(" ");
            this.method = requestLineSplit[0];
            this.params = getQueryParams();
            //if (params.isEmpty()) {
            if(requestLineSplit[1].contains("?")){
                this.path = requestLineSplit[1].split("\\?")[0];
            } else {
                this.path = requestLineSplit[1];
            }

            /*for(NameValuePair nameValuePair:list){
                System.out.println("1");
            }*/
            /*String[] requestLine = line.split(" ");
            this.method = requestLine[0];*/
            //String stringPath = requestLine[1];

            /*if(stringPath.contains("?")){
                int i = requestLine[1].indexOf('?');
                this.path = stringPath.substring(0, i);
                String stringParams = stringPath.substring(i+1);
                if(stringParams.contains("&")){
                    String[] splitParams = stringParams.split("&");
                    for (String splitParam:splitParams){
                        String[] param = splitParam.split("=");
                        this.params.put(param[0], param[1]);
                    }
                } else {
                    String[] param = stringParams.split("=");
                    this.params.put(param[0], param[1]);
                }
            } else {
                this.path = requestLine[1];
            }*/
            //this.path = requestLine[1];
            String headerName;
            String headerValue;
            while (!(line = in.readLine()).isEmpty()) {
                int splitIndex = line.indexOf(':');
                headerName = line.substring(0, splitIndex);
                headerValue = line.substring(splitIndex + 2);
                this.headers.put(headerName, headerValue);
            }
            this.body = inputStream;
        }
    }

    public String getQueryParam(String name) {
        List<NameValuePair> nameValuePairList = URLEncodedUtils.parse(requestLine, StandardCharsets.UTF_8);
        for (NameValuePair nameValuePair : nameValuePairList) {
            if (nameValuePair.getName().equals(name)) {
                return nameValuePair.getValue();
            }
        }
        return null;
    }

    public Map<String, String> getQueryParams() {
        List<NameValuePair> nameValuePairList = URLEncodedUtils.parse(requestLine, StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        for (NameValuePair nameValuePair : nameValuePairList) {
            params.put(nameValuePair.getName(), nameValuePair.getValue());
        }
        return params;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
