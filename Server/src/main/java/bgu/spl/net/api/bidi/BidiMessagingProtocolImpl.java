package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.User;
import org.omg.CORBA.NO_IMPLEMENT;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.text.SimpleDateFormat;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<String>{

    private int connectionId;
    private ConnectionsImpl connections;

    @Override
    public void start(int connectionId, Connections<String> connections) {
            this.connectionId = connectionId;
            this.connections = (ConnectionsImpl) connections;
    }

    @Override
    public void process(String message) {
        String opcode = message.substring(0, 2);
        message = message.substring(2);
        String name;
        String password;

        // Register
        if (opcode.equals("01")) {
            name = message.substring(0, message.indexOf('\0'));
            message = message.substring(message.indexOf('\0') + 1);

            if (connections.getDataBase().getFromNameToUser().get(name)!=null)
                sendError(opcode);

            // Parses the register message, Create new user object and adds it to the hashmaps
            else {
                password = message.substring(0, message.indexOf('\0'));
                message = message.substring(message.indexOf('\0') + 1);
                String birthDate = message.substring(0, message.indexOf('\0'));
                User user = new User(connectionId,name,birthDate,password);
                connections.getDataBase().getFromNameToUser().put(name,user);
                connections.getConnectionIdToUsers().put(connectionId,user);
                sendAck(opcode,"");
            }

        // Login
        }else if(opcode.equals("02")){
            name=message.substring(0, message.indexOf('\0'));
            message=message.substring(message.indexOf('\0') + 1);
            password=message.substring(0,message.indexOf('\0'));
            message=message.substring(message.indexOf('\0') + 1);
            String captcha=message;
            if(!connections.getDataBase().getFromNameToUser().containsKey(name) || connections.getDataBase().getFromNameToUser().get(name).getLogged() || !Objects.equals(connections.getDataBase().getFromNameToUser().get(name).getPassword(), password) || captcha.equals("0"))
                sendError(opcode);
            else{
                if(connections.getDataBase().getFromNameToUser().get(name).getConnectionId()!=connectionId){
                    //aid function that replace the old connectionHandler with the new one
                    update(connections.getDataBase().getFromNameToUser().get(name).getConnectionId());
                }
                connections.getDataBase().getFromNameToUser().get(name).setLogged(true);
                LinkedBlockingQueue<String>awaitingMessages=connections.getDataBase().getFromNameToUser().get(name).getMessages();

                sendAck(opcode,""); //in this func we call send of connections that calls send of connections handler

                while (!awaitingMessages.isEmpty()){
                    connections.send(connectionId, awaitingMessages.remove());
                }
            }

        // Logout
        }else if(opcode.equals("03")){
            if(connections.getConnectionIdToUsers().get(connectionId)==null || !connections.getConnectionIdToUsers().get(connectionId).getLogged()){
                sendError(opcode);

            // Set the user login status to false and trying to close the connection handler
            }else {
                connections.getConnectionIdToUsers().get(connectionId).setLogged(false);
                sendAck(opcode,"");
                connections.disconnect(connectionId);
            }

        // Follow/Unfollow
        }else if(opcode.equals("04")){
            String status=message.substring(0,1);
            message=message.substring(1);
            if(connections.getConnectionIdToUsers().get(connectionId)==null|| !connections.getConnectionIdToUsers().get(connectionId).getLogged() || connections.getDataBase().getFromNameToUser().get(message)==null){
                sendError(opcode);
            }else{
                String ack=message+'\0';

                // Follow
                if(status.equals("0")){
                    if((connections.getConnectionIdToUsers().get(connectionId).getFollowAfter()).contains(message)) {
                        sendError(opcode);
                    }
                    // Adds the message's user to the sender follow after list and the sender to the message's user followers
                    else {
                        connections.getConnectionIdToUsers().get(connectionId).getFollowAfter().add(message);
                        connections.getDataBase().getFromNameToUser().get(message).getFollowers().add(connections.getConnectionIdToUsers().get(connectionId).getUserName());
                        sendAck(opcode,ack);
                    }

                // Unfollow
                }else {
                    if(!connections.getConnectionIdToUsers().get(connectionId).getFollowAfter().contains(message))
                        sendError(opcode);

                    // Remove the message's user from the sender follow after list and the sender frome the message's user followers
                    else{
                        connections.getConnectionIdToUsers().get(connectionId).getFollowAfter().remove(message);
                        connections.getDataBase().getFromNameToUser().get(message).getFollowers().remove(connections.getConnectionIdToUsers().get(connectionId).getUserName());
                        sendAck(opcode,ack);
                    }
                }
            }

        // Post
        }else if(opcode.equals("05")) {
            if( connections.getConnectionIdToUsers().get(connectionId)==null||!connections.getConnectionIdToUsers().get(connectionId).getLogged()){
                sendError(opcode);
        }else {
                ArrayList<String> sendingList = new ArrayList<>();
                message = message.substring(0, message.length() - 1);
                message = filter(message);

                // Adds every follower of the Post sender to a sending list
                for (String userName : connections.getConnectionIdToUsers().get(connectionId).getFollowers()) {
                    sendingList.add(userName);
                }
                String tempMessage = message;

                // Checks for mentioned users
                while (tempMessage.contains("@")) {
                    String mentioned;
                    tempMessage = tempMessage.substring(tempMessage.indexOf("@") + 1);
                    if(tempMessage.contains(" ")) {
                        mentioned = tempMessage.substring(0, tempMessage.indexOf(" "));
                        tempMessage = tempMessage.substring(tempMessage.indexOf(" ") + 1);
                    }
                    else
                        mentioned=tempMessage;
                    // If exists, add them to the sending list
                    if (connections.getDataBase().getFromNameToUser().get(mentioned)!=null && !sendingList.contains(mentioned))
                        sendingList.add(mentioned);
                }

                // Send notification for every student in the sending list
                for (int i = 0; i < sendingList.size(); i++) {
                    sendNotifications("Public", connections.getConnectionIdToUsers().get(connectionId).getUserName(), sendingList.get(i), message);
                }

                // Raise the user post number and add the message to the database
                connections.getConnectionIdToUsers().get(connectionId).incNumPost();
                connections.getDataBase().getPosts().add(message);
                sendAck(opcode, "");
                }

        // PM
        } else if (opcode.equals("06")){
            String destinationUser=message.substring(0,message.indexOf('\0'));
            message=message.substring(message.indexOf('\0')+1,message.length()-1);
            if(connections.getConnectionIdToUsers().get(connectionId)==null || !connections.getConnectionIdToUsers().get(connectionId).getLogged() || connections.getDataBase().getFromNameToUser().get((destinationUser))==null || !connections.getConnectionIdToUsers().get(connectionId).getFollowAfter().contains(destinationUser))
                sendError(opcode);

            // Adds the message to the database, parses and pass it to notification
            else{
                connections.getDataBase().getPrivateMessages().add(message);
                message=message.substring(0,message.indexOf('\0'));
                sendNotifications("PM",connections.getConnectionIdToUsers().get(connectionId).getUserName(),destinationUser,message);
                sendAck(opcode,"");
            }

        // Logstat
        } else if(opcode.equals("07")){
            if(connections.getConnectionIdToUsers().get(connectionId)==null || !connections.getConnectionIdToUsers().get(connectionId).getLogged())
                sendError(opcode);

            // Runs on the users list and adds every logged-in user logstat information to a string to create a logstat ack message
            else{
                String userInformation="";
                for (User currUser:connections.getDataBase().getFromNameToUser().values()) {
                    if(currUser.getLogged() && !connections.getConnectionIdToUsers().get(connectionId).getBlockList().contains(currUser.getUserName())){
                        userInformation += "10 "+ opcode+" "+calculateAge(currUser.getBirthdate())+" "+currUser.getNumPost()+ " " +currUser.getFollowers().size()+" "+currUser.getFollowAfter().size()+"\n";
                    }
                }
                userInformation=userInformation.substring(6,userInformation.length()-1);
                sendAck(opcode,userInformation);
            }

        // Stat
        } else if(opcode.equals("08")){
            ArrayList<String>users=new ArrayList<>();
            String userInformation="";
            message = message.substring(0, message.indexOf('\0'));
            boolean containBlockUser = false;

            if(connections.getConnectionIdToUsers().get(connectionId)==null || !connections.getConnectionIdToUsers().get(connectionId).getLogged())
                sendError(opcode);
            else {
                while (message.contains("|")) {
                    users.add(message.substring(0, message.indexOf("|")));
                    message = message.substring(message.indexOf("|") + 1);
                }
                users.add(message);
                // Run on the message users list and every user stat information to a string to create a logstat ack message
                for (String currUser : users) {
                    if (connections.getConnectionIdToUsers().get(connectionId).getBlockList().contains(currUser))
                        containBlockUser = true;
                    userInformation += "10 " + opcode + " " + calculateAge(connections.getDataBase().getFromNameToUser().get(currUser).getBirthdate()) + " " + connections.getDataBase().getFromNameToUser().get(currUser).getNumPost() + " " + connections.getDataBase().getFromNameToUser().get(currUser).getFollowers().size() + " " + connections.getDataBase().getFromNameToUser().get(currUser).getFollowAfter().size() + "\n";
                }
                userInformation = userInformation.substring(6, userInformation.length() - 1);
                if (containBlockUser)
                    sendError(opcode);
                else
                    sendAck(opcode, userInformation);
            }

        // Block
        }else if(opcode.equals("12")){
            name=message.substring(0,message.indexOf('\0'));
            if(connections.getConnectionIdToUsers().get(connectionId)==null || !connections.getConnectionIdToUsers().get(connectionId).getLogged() || !connections.getDataBase().getFromNameToUser().containsKey(name)){
                sendError(opcode);

            // Remove every the sender and the blocked user from each other follow list and update their blocklist
            }else{
                connections.getConnectionIdToUsers().get(connectionId).updateBlockList(name);
                connections.getDataBase().getFromNameToUser().get(name).updateBlockList(connections.getConnectionIdToUsers().get(connectionId).getUserName());
                connections.getConnectionIdToUsers().get(connectionId).getFollowAfter().remove(name);
                connections.getConnectionIdToUsers().get(connectionId).getFollowers().remove(name);
                connections.getDataBase().getFromNameToUser().get(name).getFollowAfter().remove(connections.getConnectionIdToUsers().get(connectionId).getUserName());
                connections.getDataBase().getFromNameToUser().get(name).getFollowers().remove(connections.getConnectionIdToUsers().get(connectionId).getUserName());
                sendAck(opcode,"");
            }
        }
    }

    // Gets birthdate and calculate the number of years
    private int calculateAge(String birthdate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        //convert String to LocalDate
        LocalDate localDate = LocalDate.parse(birthdate, formatter);
        LocalDate currentDate= LocalDate.now();
        Period period=Period.between(localDate,currentDate);
       return (period.getYears());
    }

    // Create notification message and sending it to the destinationUser
    private void sendNotifications(String notificationType, String sourceUser, String destinationUser, String content){
        content = filter(content);
        String notificationMessage="09 " + notificationType + " " + sourceUser + " " + content;
        int destinationConnection=connections.getDataBase().getFromNameToUser().get(destinationUser).getConnectionId();
        if (!connections.getDataBase().getFromNameToUser().get(destinationUser).getLogged())
            connections.getDataBase().getFromNameToUser().get(destinationUser).updateMessages(notificationMessage);
        else
            connections.send(destinationConnection,notificationMessage);
    }

    // Gets message and filter out a given hardcoded words
    private String filter(String message){
        String[] RestrictedWords = connections.getDataBase().getRestrictedWordList();
        String output = "";
        boolean added = false;
        String tempWord = "";
        while (message.contains(" ")) {
            tempWord = message.substring(0, message.indexOf(" "));
            for (String word : RestrictedWords) {
                if (word.equals(tempWord)) {
                    output += "<filtered> ";
                    added = true;
                    break;
                }
            }
            if (!added)
                output += tempWord + " ";
            message = message.substring(message.indexOf(" ") + 1);
            tempWord = "";
            added = false;
        }
        for (String word : RestrictedWords) {
            if (word.equals(message)) {
                output += "<filtered>";
                added = true;
            }
        }
        if (!added)
            output += message;
        return output;
    }


    // Sends error message
    private void sendError(String opcode) {
        opcode = opcode.substring(opcode.indexOf("0") + 1); // Remove the zero from the opcode
        String errorMessage = "11 " + opcode;
//        connections.getConnectionIdToConnectionHandler().get(connectionId).send(errorMessage);
        connections.send(connectionId,errorMessage);
    }

    // Sends ack message to the sender according to its message
    private void sendAck(String opcode, String s) {
        //opcode = opcode.substring(opcode.indexOf("0") + 1); // Remove the zero from the opcode
        String ackMessage = "10 " + opcode + " " + s;
        connections.send(connectionId,ackMessage);
//        connections.getConnectionIdToConnectionHandler().get(connectionId).send(ackMessage);
        //we did an adjustment to the logstat so, we won't have a separate case here
    }

    // Updated the hashmap 'getConnectionIdToConnectionHandler' and the counter connectionHandlerId
    public void update(int id){
        connections.getConnectionIdToConnectionHandler().remove(id);
        ConnectionHandler validConnectionHandler= connections.getConnectionIdToConnectionHandler().get(connectionId);
        connections.getConnectionIdToConnectionHandler().remove(connectionId);
        connections.getConnectionIdToConnectionHandler().put(id,validConnectionHandler);
        connections.decreasingConnectionHandlerId();
        connectionId=id;
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
