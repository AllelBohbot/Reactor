package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;


public class MessageEncoderDecoderImpl implements  MessageEncoderDecoder<String>{

    private String decodedMessage="";

    @Override
    public String decodeNextByte(byte nextByte) {
        if (nextByte == ';') {
            String tempAns=decodedMessage;
            decodedMessage="";
            return tempAns;
        }
        decodedMessage=decodedMessage+((char) nextByte);
        return null;
    }

    @Override
    public byte[] encode(String message) {
       return (message+';').getBytes(StandardCharsets.UTF_8);
    }
}