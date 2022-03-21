package Handlers.CoordinationHandler;


import Handlers.ChatHandler.ClientResponseHandler;
import Models.Client;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.ChatService.ChatClientService;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NewIdentityHandler {
    private final Logger logger = Logger.getLogger(Handlers.ChatHandler.NewIdentityHandler.class);
    private final RequestHandler serverRequestHandler = new RequestHandler();
    private final ResponseHandler serverResponseHandler = new ResponseHandler();
    private final ClientResponseHandler clientResponseHandler = new ClientResponseHandler();

    public NewIdentityHandler() {
    }

    public boolean checkIdentityUnique(String identity) {
        boolean isIdentityUnique = true;
        // TODO: Check identity from all servers.
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (Objects.equals(currentServer.getServerID(), leaderServer.getServerID())) {
            ConcurrentHashMap<String, Client> clients = LeaderState.getInstance().getGlobalClients();
            for (Iterator<String> it = clients.keys().asIterator(); it.hasNext(); ) {
                String client = it.next();
                if (Objects.equals(client, identity)) {
                    isIdentityUnique = false;
                }
            }
        }
        return isIdentityUnique;
    }

    public JSONObject coordinatorNewClientIdentity(Client client, String identity, String serverID) {
        JSONObject response;
        if (checkIdentityUnique(identity)) {
            client.setIdentity(identity);
            client.setServer(serverID);
            client.setStatus("active");
            LeaderState.getInstance().addClientToGlobalList(client);
            logger.info("New identity creation accepted");
            response = this.serverResponseHandler.sendNewIdentityServerResponse("true", identity);
        } else {
            logger.info("New identity creation rejected");
            response = this.serverResponseHandler.sendNewIdentityServerResponse("false", identity);
        }
        return response;
    }
    public JSONObject moveToMainHall(Client client){
        JSONObject response;
        String roomID = "MainHall-" + System.getProperty("serverID");
        ServerState.getServerStateInstance().addClientToRoom(roomID, client);
        response = clientResponseHandler.moveToRoomResponse(client.getIdentity(), "", roomID);
        return response;
    }
    public Map<String, JSONObject> leaderApprovedNewClientIdentity(String isApproved, Client client, String identity){
        Map<String, JSONObject> responses = new HashMap<>();
        if(isApproved.equals("true")){
            logger.info("New identity creation accepted");
            client.setIdentity(identity);
            client.setServer(System.getProperty("serverID"));
            client.setStatus("active");
            ServerState.getServerStateInstance().clients.put(client.getIdentity(), client);
            responses.put("client-only", clientResponseHandler.sendNewIdentityResponse("true"));
            responses.put("broadcast", moveToMainHall(client));
        }else if(isApproved.equals("false")){
            ServerState.getServerStateInstance().clientServices.remove(identity);
            logger.info("New identity creation rejected");
            responses.put("client-only", clientResponseHandler.sendNewIdentityResponse("false"));
        }
        return responses;
    }
}