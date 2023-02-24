1.
1.1
mvn clean
mvn compile
TPC: mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="7777"
Reactor: mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="7777 5"
 
1.2 
REGISTER allel pass 11-11-1111
LOGIN allel pass
LOGOUT 
FOLLOW 0 user1 
FOLLOW 1 user1 
PM user1 hi 
POST hi with @user1 how are you
BLOCK user1
LOGSTAT 
STAT user1 user2 user3 

2. 
The filtered set of words is in class DataBase under the name "restrictedWordsList"