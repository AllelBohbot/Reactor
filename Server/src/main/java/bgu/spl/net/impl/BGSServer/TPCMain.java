package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        System.out.println("server port: " + port);
        try(Server<String> server=Server.threadPerClient(port,()->new BidiMessagingProtocolImpl(), MessageEncoderDecoderImpl::new);) {
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
