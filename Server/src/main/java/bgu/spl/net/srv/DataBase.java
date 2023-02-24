package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DataBase { //singelton

    //TODO: synchronize this object with reader writers lock
    private static class DataBaseSingleton{
        private static DataBase instance=new DataBase();
    }

    private ConcurrentHashMap<String,User> fromNameToUser;
    private LinkedBlockingQueue<String> privateMessages;
    private LinkedBlockingQueue<String> posts;
    private String[] restrictedWordsList = new String[]{"Trump", "war"};


    private DataBase(){
        fromNameToUser=new ConcurrentHashMap<>();
        privateMessages=new LinkedBlockingQueue<>();
        posts=new LinkedBlockingQueue<>();
    }

    public static DataBase getInstance(){
        return DataBaseSingleton.instance;
    }

    public ConcurrentHashMap<String, User> getFromNameToUser() {
        return fromNameToUser;
    }

    public LinkedBlockingQueue<String> getPrivateMessages() {
        return privateMessages;
    }

    public LinkedBlockingQueue<String> getPosts() {
        return posts;
    }

    public String[] getRestrictedWordList() {
        return restrictedWordsList;
    }
}
