package request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private InputStream body;
    private Map<String, String> params = new HashMap<>();

    public Request(InputStream inputStream) throws IOException {
        var in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        if ((line = in.readLine())!=null){
            String[] requestLine = line.split(" ");
            this.method = requestLine[0];
            String stringPath = requestLine[1];
            if(this.method.equals("GET") & stringPath.contains("?"))
            {
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
            }
            //this.path = requestLine[1];
            String headerName;
            String headerValue;
            while (!(line=in.readLine()).isEmpty()){
                int splitIndex=line.indexOf(':');
                headerName = line.substring(0, splitIndex);
                headerValue = line.substring(splitIndex+2);
                this.headers.put(headerName,headerValue);
            }
            this.body=inputStream;
        }
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
