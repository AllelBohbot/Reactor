package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;


public class main {
    public static void main(String[] args) {

//        //try(Server<String> server=Server.threadPerClient(7777,()->new BidiMessagingProtocolImpl(), MessageEncoderDecoderImpl::new);) {
//        //    server.serve();
//        try (Server<String> server = Server.reactor(3, 7777, () -> new BidiMessagingProtocolImpl(), MessageEncoderDecoderImpl::new);) {
//            server.serve();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    }
}
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        String date = "16/08/2016";
//
//        //convert String to LocalDate
//        LocalDate localDate = LocalDate.parse(date, formatter);
//        LocalDate currentDate= LocalDate.now();
//        Period period=Period.between(localDate,currentDate);
//        System.out.println(period.getYears());
//        String age=""+period;
//        System.out.println(age);
