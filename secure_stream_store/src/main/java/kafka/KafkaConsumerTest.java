package kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import index.Digest;
import index.DigestShow;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KafkaConsumerTest {
    private final static String TOPIC_NAME = "testTopicCreate001";
    private final static String CONSUMER_GROUP_NAME = "testJavaGroup";

    public static void main(String[] args) {
//        String usr = "testUsr";
//        long streamId = 100000001L;
//        String TOPIC_NAME = usr + streamId;
//        String TOPIC_NAME = "RealData_0507_Test_7363489638919324779";
//        String TOPIC_NAME = "RealData_0507_Test_2724174914223130962";
//        RealData_0508_Test_-8793904477770439337
//        String TOPIC_NAME = "qiao-3520022569269093985";
        String TOPIC_NAME = "TESTTT3912298808912637091";
        String usr = "TESTTT";
        long streamId = 538747647371556138L;
        KafkaReader kr = new KafkaReader();

        Digest d1 = new Digest(0, 0, 0, true, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        Digest d2 = new Digest(0, 0, 0, true, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        Date start = new Date();
        for (int i = 0; i < 21600; i+=10) {
            //d1.mergeOther(kr.getAggregatedDigest(usr, streamId, i, i+9));
        }
        System.out.println(d1);
        List<Digest> dl = kr.getDigests(usr, streamId, 0, 21599);
        for (Digest curD : dl){
            d2.mergeOther(curD);
        }
        System.out.println(d2);
        Date end = new Date();
        System.out.println(start);
        System.out.println(end);
        System.out.println(end.getTime() - start.getTime());

        System.out.println(dl.size());
        //System.out.println(digests);

        /*for (int i = 1; i < 4; i++) {
            long dataIdFrom = 0;
            long dataNum = 1000;
            System.out.println(i);
            ConsumerRecords<Long, byte[]> records = KafkaReader.getDatas(TOPIC_NAME, i, dataIdFrom, dataNum);
            long sum = 0;
            LongMacNodeNodeContent node0 = new LongMacNodeNodeContent(0, BigInteger.ZERO);
            for (ConsumerRecord<Long, byte[]> record : records){
                //System.out.printf("收到消息：partition = %d, offset = %d, key = %s, value = %s%n", record.partition(),record.offset(), record.key(), Arrays.toString(record.value()));
                LongMacNodeNodeContent nodei = LongMacNodeNodeContent.decode(record.value());
//                System.out.println("Node_"+i);
//                System.out.println("long: "+nodei.getLong());
//                System.out.println("mac:  "+nodei.getMac());
//                System.out.println("String:"+nodei.getStringRepresentation());
//                System.out.println("toString:"+nodei);
                node0.mergeOther(nodei);
                sum += nodei.getLong();
            }
            System.out.println(sum);
            System.out.println("merge: "+node0.getLong());
            System.out.println("merge mac: "+node0.getMac());
        }

        BigInteger M = BigInteger.ONE.shiftLeft(128);
        System.out.println(M);*/








//        Properties props = new Properties();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.72.130:9092");
//        // 消费分组名
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_NAME);
//        //把发送的key从字符串序列化为字节数组
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
//        //把发送消息value从字符串序列化为字节数组
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
//
//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 6);
//
//        //创建一个消费者的客户端
//        KafkaConsumer<Long, byte[]> consumer = new KafkaConsumer<Long, byte[]>(props);
//        // 消费者订阅主题列表
//        //consumer.subscribe(Arrays.asList(TOPIC_NAME));
//        //指定分区消费
//        consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, 1 )));
//
//        /*//消息回溯消费
//        consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, 0 )));
//        consumer.seekToBeginning(Arrays.asList(new TopicPartition(TOPIC_NAME,0 )));*/
//
//        //指定offset
//        //consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, 0 )));
//        consumer.seek(new TopicPartition(TOPIC_NAME, 1 ), 1 );
//
//        ConsumerRecords<Long, byte[]> records =consumer.poll(Duration.ofMillis( 1000 ));
//        for (ConsumerRecord<Long, byte[]> record : records){
//            System.out.printf("收到消息：partition = %d, offset = %d, key = %s, value = %s%n", record.partition(),record.offset(), record.key(), Arrays.toString(record.value()));
//        }
        // Arrays.toString(record.value())

/*        while (true) {
            *//*
             * poll() API 是拉取消息的⻓轮询
             *//*
            ConsumerRecords<Long, byte[]> records =consumer.poll(Duration.ofMillis( 1000 ));

            for (ConsumerRecord<Long, byte[]> record : records) {
                System.out.printf("收到消息：partition = %d,offset = %d, key =%s, value = %s%n", record.partition(),record.offset(), record.key(), record.value());
            }
        }*/

    }

}