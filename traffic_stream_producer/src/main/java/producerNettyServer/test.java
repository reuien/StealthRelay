package producerNettyServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class test {
    static Map<String, List<Long>> usrStreams = new HashMap<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        /*String usr1 = "usr1";
        String usr2 = "usr2";
        String usr3 = "usr3";
        System.out.println(usrStreams);
        addStream(usr1, 1001L);
        System.out.println(usrStreams);
        addStream(usr1, 1002L);
        System.out.println(usrStreams);
        addStream(usr1, 1003L);
        System.out.println(usrStreams);
        addStream(usr2, 2001L);
        System.out.println(usrStreams);
        addStream(usr2, 2002L);
        System.out.println(usrStreams);
        addStream(usr2, 2003L);
        System.out.println(usrStreams);
        addStream(usr3, 3001L);
        System.out.println(usrStreams);
        addStream(usr3, 3002L);
        System.out.println(usrStreams);
        addStream(usr3, 3003L);
        System.out.println(usrStreams);

        System.out.println("====");
        deleteStream(usr2, 2002L);
        System.out.println(usrStreams);
        System.out.println(getStreams(usr1));
        System.out.println(getStreams(usr2));
        System.out.println(getStreams(usr3));*/

/*        String owner = "testUsr1";
        long sid = 100001L;
        KafkaTopicCreater.createTopic(owner, sid);*/


    }

    public static void addStream(String usrName, long streamID) {
        if (usrStreams.containsKey(usrName)){
            List<Long> newStreams = usrStreams.get(usrName);
            newStreams.add(streamID);
            usrStreams.replace(usrName, newStreams);
        }
        else {
            List<Long> newStreams2 = new ArrayList<>();
            newStreams2.add(streamID);
            usrStreams.put(usrName, newStreams2);
        }
    }
    public static void deleteStream(String usrName, long streamID) {
        List<Long> newStreams = usrStreams.get(usrName);
        newStreams.remove(streamID);
        usrStreams.replace(usrName, newStreams);
    }
    public static List<Long> getStreams(String usrName) {
        return usrStreams.get(usrName);
    }

}
