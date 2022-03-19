package Services.CoordinationService;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CoordinationService implements Runnable{
    private final Socket coordinationSocket;
    Logger logger = Logger.getLogger(CoordinationService.class);
    private final JSONParser parser = new JSONParser();

    public CoordinationService(Socket coordinationSocket){
        this.coordinationSocket = coordinationSocket;
    }

    @Override
    public void run() {
        while (true){
            try {
                assert coordinationSocket != null;
                BufferedReader in = new BufferedReader(new InputStreamReader(coordinationSocket.getInputStream(), StandardCharsets.UTF_8));
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                System.out.println("Receiving: " + message.toJSONString());
            } catch (IOException | ParseException e) {
                logger.error("Exception occurred" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
