package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.DataBase;
import bgu.spl.net.srv.User;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl implements Connections<String>{ //singelton

    private static class ConnectionsSingleton{
        private static ConnectionsImpl instance=new ConnectionsImpl();
    }

    private DataBase dataBase;
    private ConcurrentHashMap<Integer, ConnectionHandler<String>> connectionIdToConnectionHandler;
    private int connectionHandlerId;
    private ConcurrentHashMap<Integer, User> connectionIdToUsers;

    private ConnectionsImpl(){
        dataBase=dataBase.getInstance();
        connectionIdToConnectionHandler=new ConcurrentHashMap<>();
        connectionHandlerId=0;
        connectionIdToUsers=new ConcurrentHashMap<>();
    }

    public int getConnectionHandlerId() {
        return connectionHandlerId;
    }

    public static ConnectionsImpl getInstance(){
        return ConnectionsImpl.ConnectionsSingleton.instance;
    }

    public DataBase getDataBase() {
        return dataBase;
    }

    public ConcurrentHashMap<Integer, ConnectionHandler<String>> getConnectionIdToConnectionHandler() {
        return connectionIdToConnectionHandler;
    }

    public ConcurrentHashMap<Integer, User> getConnectionIdToUsers() {
        return connectionIdToUsers;
    }

    public void addingConnectionHandler(ConnectionHandler<String> connectionHandler){
        connectionIdToConnectionHandler.put(connectionHandlerId,connectionHandler);
        connectionHandlerId++;
    }

    public void decreasingConnectionHandlerId(){
        connectionHandlerId--;
    }


    @Override
    public boolean send(int connectionId, String msg) {
        try {
            connectionIdToConnectionHandler.get(connectionId).send(msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void broadcast(String msg) {
        for (User user:dataBase.getFromNameToUser().values()) {
            connectionIdToConnectionHandler.get(user.getConnectionId()).send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        try {
            connectionIdToConnectionHandler.get(connectionId).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}