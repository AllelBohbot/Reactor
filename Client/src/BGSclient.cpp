#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <thread>
#include <string>
using namespace std;

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

std::string dateAndTime(){
    std::string dateTime = "";
    boost::posix_time::ptime timeUTC = boost::posix_time::second_clock::universal_time();
    std::string day = std::to_string(timeUTC.date().day());
    std::string month = std::to_string(timeUTC.date().month().as_number());
    std::string year = std::to_string(timeUTC.date().year());
    std::string hour = std::to_string(timeUTC.time_of_day().hours());
    std::string minute = std::to_string(timeUTC.time_of_day().minutes());
    return dateTime = day + "-" + month + "-" + year + " " + hour + ":" + minute;
}

std::string builderInput(std::string line){
    std::string ans="";
    if (line.find("REGISTER")<line.size()){
        //erasing the ,essage
        line=line.erase(0,9);
        //appending opcode
        ans.append("01");
        ans.append(line.substr(0,line.find(" ")));
        ans += '\0';//adding zero after username
        line= line.erase(0,line.find(" ")+1);
        ans.append(line.substr(0,line.find(" ")));
        ans += '\0';
        //adding zero after password
        line= line.erase(0,line.find(" ")+1);
        ans.append(line);
        ans += '\0';
    }
    else if(line.find("LOGIN")<line.size()){
        line=line.erase(0,6);
        ans.append("02");
        ans.append(line.substr(0,line.find(" ")));
        ans += '\0';
        line= line.erase(0,line.find(" ")+1);
        ans.append(line.substr(0,line.find(" ")));
        ans += '\0';
        line= line.erase(0,line.find(" ")+1);
        ans.append(line.substr(0,1));
    } else if(line.find("LOGOUT")<line.size()){
        ans.append("03");
    }else if(line.find("FOLLOW")<line.size()){
        line=line.erase(0,7);
        ans.append("04");
        ans.append(line.substr(0,1));
        line=line.erase(0,2);
        ans.append(line);
    } else if(line.find("POST")<line.size()){
        line=line.erase(0,5);
        ans.append("05");
        ans.append(line);
        ans += '\0';
    } else if(line.find("PM")<line.size()){
        line=line.erase(0,3);
        ans.append("06");
        //adding userName
        ans.append(line.substr(0,line.find(" ")));
        ans += '\0';
        line=line.erase(0,line.find(" ")+1);
        //adding Content
        ans.append(line);
        ans += '\0';
        ans.append(dateAndTime());
        ans += '\0';
    }else if(line.find("LOGSTAT")<line.size()){
        ans.append("07");
    }else if(line.find("STAT")<line.size()){
        ans.append("08");
        line=line.erase(0,5);
        while (line.find(" ")<line.size()){
            ans.append(line.substr(0,line.find(" ")));
            line=line.erase(0,line.find(" ") + 1);
            ans.append("|");
        }
        ans.append(line);
        ans += '\0';
    }else if(line.find("BLOCK")<line.size()) {
        ans.append("12");
        line = line.erase(0, 6);
        ans.append(line);
        ans += '\0';
    }
    return ans;
}

std::string builderOutput(std::string answer){
    std::string output;
    if(!answer.substr(0,2).compare("10")) {
        answer = answer.erase(0, 3);
        output.append("ACK ");
        int opcode = stoi(answer.substr(0, 2));
        output.append(std::to_string(opcode));
        //follow case
        if (!answer.substr(0, 2).compare("04")) {
            answer = answer.erase(0, 2);
            output.append(" " +answer.substr(0, answer.find('\0')));
            //logstat&stat cases
        } else if (!answer.substr(0, 2).compare("07") || !answer.substr(0, 2).compare("08")) {
            answer = answer.erase(0, 3);
            if(answer.find('\n') < answer.size())
            {
                output.append(" " + answer.substr(0, answer.find('\n') + 1));
                answer = answer.erase(0, answer.find('\n') + 1);
                while (answer.find('\n') < answer.size()) {
                    output.append("ACK " + std::to_string(opcode) + " ");
                    output.append(answer.substr(6, answer.find('\n')-5));
                    answer = answer.erase(0, answer.find('\n') + 1);
                }
                output.append("ACK " + std::to_string(opcode) + " ");
                output.append(answer.substr(6, answer.size()-7));
            }
            else
            {
                output.append(" " + answer.substr(0, answer.size()-1));
            }
            }
        }
        else if(!answer.substr(0,2).compare("09")){
            answer=answer.erase(0,2);
            output.append("NOTIFICATION");
            if(!answer.substr(0,2).compare("0")){
                answer=answer.erase(0,1);
                output.append("PM");
            } else if(!answer.substr(0,2).compare("1")){
                answer=answer.erase(0,1);
                output.append("POST");
            }
            output.append(answer.substr(0,answer.find('\0')));
            answer=answer.erase(0,answer.find('\0'));
            output.append(answer.substr(0,answer.find('\0')));
            output.resize(output.size()-1);
    } else if(!answer.substr(0,2).compare("11")){
            answer=answer.erase(0,2);
            output.append("ERROR ");
            answer=answer.substr(1,answer.length()-1);
            int opcode= stoi(answer);
            output.append(to_string(opcode));
        }
    return output;
}



void static ioToServer(ConnectionHandler &connectionHandler,std::string *lock) {
    while (1) {
        if (!lock->compare("1")) {
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            std::string ans = builderInput(line);
            *lock = "0";
            if (!connectionHandler.sendLine(ans)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }
            //std::cout << "Sent bytes to server" << std::endl;
            }
    }
}



void static serverToIo(ConnectionHandler &connectionHandler, std::string* lock) {
    while (1){
        std::string answer;
        if (!connectionHandler.getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        answer = builderOutput(answer);
        int len;
        len=answer.length();
        //answer.resize(len-1);
        std::cout << answer << std::endl << std::endl;
        if(!answer.substr(0,3).compare("ACK") || !answer.substr(0,5).compare("ERROR"))
            *lock="1";
        if (!answer.compare("ACK 3")) {
            std::cout << "Exiting...\n" << std::endl;
            delete lock;
            connectionHandler.close();
            break;
        }

    }
}


int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    std::cout<<"Connected\n";
    std::string *key=new string ("1");
    std::thread sendThread(ioToServer,std::ref(connectionHandler), std::ref(key));
    serverToIo(connectionHandler,key);
    return 0;
}