package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int numOfThread = Integer.parseInt(args[1]);
        System.out.println("server port: " + port + " Num of thread: " + numOfThread);
        try (Server<String> server = Server.reactor(numOfThread, port, () -> new BidiMessagingProtocolImpl(), MessageEncoderDecoderImpl::new);) {
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
