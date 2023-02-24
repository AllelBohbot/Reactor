package bgu.spl.net.srv;
import java.util.concurrent.LinkedBlockingQueue;


public class User {

    private int connectionId;
    private Boolean logged;
    private LinkedBlockingQueue<String> followers;
    private LinkedBlockingQueue<String> followAfter;
    private LinkedBlockingQueue<String> blockList;
    private LinkedBlockingQueue<String> messages;
    private int numPost;
    private String userName;
    private String birthdate;
    private String password;


    public User(int connectionId, String userName, String birthdate, String password) {
        this.connectionId = connectionId;
        this.userName = userName;
        this.birthdate = birthdate;
        this.password = password;
        followers=new LinkedBlockingQueue<>();
        followAfter=new LinkedBlockingQueue<>();
        blockList=new LinkedBlockingQueue<>();
        messages=new LinkedBlockingQueue<>();
        logged = false;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public Boolean getLogged() {
        return logged;
    }

    public LinkedBlockingQueue<String> getFollowers() {
        return followers;
    }

    public LinkedBlockingQueue<String> getFollowAfter() {
        return followAfter;
    }

    public LinkedBlockingQueue<String> getMessages() {
        return messages;
    }

    public int getNumPost(){
        return numPost;
    }

    public String getUserName() {
        return userName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getPassword() {
        return password;
    }

    public void setLogged(Boolean logged) {
        this.logged = logged;
    }

    public void incNumPost() {
        this.numPost ++;
    }

    public void updateBlockList(String username){
        if (!blockList.contains(username))
            blockList.add(username);
    }

    public LinkedBlockingQueue<String> getBlockList() {
        return blockList;
    }

    public void updateMessages(String message)
    {
        messages.add(message);
    }
}
