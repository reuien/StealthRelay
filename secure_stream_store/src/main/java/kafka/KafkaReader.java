package kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import crypto.LongMacNodeNodeContent;
import exceptions.StorageException;
import index.Chunk;
import index.Digest;
import index.blockindex.node.NodeContent;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

public class KafkaReader {
    private final static String KAFKA_SERVER = "localhost:9092";
    private final static String CONSUMER_GROUP_NAME = "testJavaGroup";

    public static KafkaConsumer<Long, byte[]> getConsumerChunk(int pollNumber){
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        // 消费分组名
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_NAME);
        //把发送的key从字符串序列化为字节数组
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        //把发送消息value从字符串序列化为字节数组
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, pollNumber);

        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 104857600);

        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 104857600);
        /*System.out.println(props);
        System.out.println(pollNumber);*/

        //创建一个消费者的客户端
        KafkaConsumer<Long, byte[]> consumer = new KafkaConsumer<>(props);
        return consumer;
    }
    public static KafkaConsumer<Long, String> getConsumerDigest(int pollNumber){
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        // 消费分组名
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_NAME);
        //把发送的key从字符串序列化为字节数组
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        //把发送消息value从字符串序列化为字节数组
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, pollNumber);

        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 104857600);

        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 104857600);
        /*System.out.println(props);
        System.out.println(pollNumber);*/

        //创建一个消费者的客户端
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }

//    public static ConsumerRecord<Long, byte[]> consumerChunk(String TOPIC_NAME, int part, long dataId){
//        // 消费者订阅主题列表
//        //consumer.subscribe(Arrays.asList(TOPIC_NAME));
//        //指定分区消费
//        //consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, part)));
//        /*//消息回溯消费
//        consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, 0 )));
//        consumer.seekToBeginning(Arrays.asList(new TopicPartition(TOPIC_NAME,0 )));*/
//
//        KafkaConsumer<Long, byte[]> consumer = getConsumerChunk(1);
//        consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, part)));
//        //指定offset
//        consumer.seek(new TopicPartition(TOPIC_NAME, part), dataId);
//
//        ConsumerRecords<Long, byte[]> records = consumer.poll(3000);
//        ConsumerRecord<Long, byte[]> result = null;
//        if (records.count() == 1){
//            for (ConsumerRecord<Long, byte[]> record : records){
//                result = record;
//            }
//        }
////        for (ConsumerRecord<Long, byte[]> record : records){
////            System.out.printf("收到消息：partition = %d, offset = %d, key = %s, value = %s%n", record.partition(),record.offset(), record.key(), Arrays.toString(record.value()));
////        }
//
//        consumer.close();
//
//        return result;
//    }
    public static ConsumerRecords<Long, byte[]> consumerChunks(String TOPIC_NAME, int part, long dataIdFrom, long dataIdTo){
        // 消费者订阅主题列表
        //consumer.subscribe(Arrays.asList(TOPIC_NAME));
        //指定分区消费
        //consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, part)));
        /*//消息回溯消费
        consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, 0 )));
        consumer.seekToBeginning(Arrays.asList(new TopicPartition(TOPIC_NAME,0 )));*/

        long dataNum = dataIdTo - dataIdFrom + 1;
        KafkaConsumer<Long, byte[]> consumer = getConsumerChunk((int) dataNum);
        consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, part)));
        //指定offset
        consumer.seek(new TopicPartition(TOPIC_NAME, part), dataIdFrom);
        ConsumerRecords<Long, byte[]> records = consumer.poll(Duration.ofSeconds(10));
//        for (ConsumerRecord<Long, byte[]> record : records){
//            System.out.printf("收到消息：partition = %d, offset = %d, key = %s, value = %s%n", record.partition(),record.offset(), record.key(), Arrays.toString(record.value()));
//        }
        consumer.close();
        return records;
    }

    public static ConsumerRecords<Long, String> consumerDigests(String TOPIC_NAME, int part, long dataIdFrom, long dataIdTo){
        // 消费者订阅主题列表
        //consumer.subscribe(Arrays.asList(TOPIC_NAME));
        //指定分区消费
        //consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, part)));
        /*//消息回溯消费
        consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, 0 )));
        consumer.seekToBeginning(Arrays.asList(new TopicPartition(TOPIC_NAME,0 )));*/

        long dataNum = dataIdTo - dataIdFrom + 1;
        KafkaConsumer<Long, String> consumer = getConsumerDigest((int) dataNum);
        consumer.assign(Arrays.asList(new TopicPartition(TOPIC_NAME, part)));
        //指定offset
        consumer.seek(new TopicPartition(TOPIC_NAME, part), dataIdFrom);
        ConsumerRecords<Long, String> records = consumer.poll(Duration.ofSeconds(30));
        /*for (ConsumerRecord<Long, String> record : records){
            System.out.printf("收到消息：partition = %d, offset = %d, key = %s, value = %s%n", record.partition(),record.offset(), record.key(), JSON.parse(record.value()));
        }*/
        consumer.close();
        return records;
    }

//    public Chunk getChunk(String usrName, long steamId, int key) {
//        String TOPIC_NAME = usrName + steamId;
//        ConsumerRecord<Long, byte[]> record = KafkaReader.consumerChunk(TOPIC_NAME, 0, key);
//        Chunk curChunk = new Chunk(record.key(), record.value());
//        return curChunk;
//    }

    public List<Chunk> getChunks(String usrName, long steamId, long dataIdFrom, long dataIdTo) {
        List<Chunk> chunks = new ArrayList<>();
        String TOPIC_NAME = usrName + steamId;
        ConsumerRecords<Long, byte[]> records = KafkaReader.consumerChunks(TOPIC_NAME, 0, dataIdFrom, dataIdTo);
        //System.out.println(records);
        for (ConsumerRecord<Long, byte[]> record : records){
            Chunk curChunk = new Chunk(record.key(), record.value());
            chunks.add(curChunk);
        }
        //System.out.println(chunks.size());
        if (chunks == null)
            throw new StorageException("No Chunk found", 1);
        return chunks;
    }
    public static Digest jsonToDigest(String jsonStr){
        JSONObject json = JSON.parseObject(jsonStr);
        Digest digest = JSONObject.parseObject(json.toJSONString(), Digest.class);
        return digest;
    }
    public List<Digest> getDigests(String usrName, long steamId, long dataIdFrom, long dataIdTo) {
        List<Digest> digestList = new ArrayList<>();
        String TOPIC_NAME = usrName + steamId;
        ConsumerRecords<Long, String> records = KafkaReader.consumerDigests(TOPIC_NAME, 1, dataIdFrom, dataIdTo);
        //System.out.println(records);
        for (ConsumerRecord<Long, String> record : records){
            Digest curDigest = jsonToDigest(record.value());
            //curDigest.setChunkIdFrom(record.key());
            //curDigest.setChunkIdTo(record.key());
            digestList.add(curDigest);
        }
        //System.out.println(chunks.size());
        if (digestList == null)
            throw new StorageException("No Digest found", 1);
        return digestList;
    }

    public Digest getAggregatedDigest(String usrName, long steamId, long dataIdFrom, long dataIdTo) {
        String TOPIC_NAME = usrName + steamId;
        ConsumerRecords<Long, String> records = KafkaReader.consumerDigests(TOPIC_NAME, 1, dataIdFrom, dataIdTo);
        List<Digest> digestList = new ArrayList<>();
        for (ConsumerRecord<Long, String> record : records){
            Digest curDigest = jsonToDigest(record.value());
            digestList.add(curDigest);
        }
        Digest digest;
        if (digestList.get(0).isThisHasMAC()){
            digest = new Digest(0, 0, 0, true, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        }else{
            digest = new Digest(0, 0, 0, false);
        }
        for (Digest curDigest : digestList){
            digest.mergeOther(curDigest);
        }
        //digest.setChunkIdFrom(dataIdFrom);
        //digest.setChunkIdTo(dataIdTo);
        return digest;
    }

    public Digest getAggregatedDigestNew(String usrName, long steamId, long dataIdFrom, long dataIdTo) {
        String TOPIC_NAME = usrName + steamId;
        ConsumerRecords<Long, String> records = KafkaReader.consumerDigests(TOPIC_NAME, 1, dataIdFrom, dataIdTo);
        List<Digest> digestList = new ArrayList<>();
        for (ConsumerRecord<Long, String> record : records){
            Digest curDigest = jsonToDigest(record.value());
            digestList.add(curDigest);
        }
        Digest digest;
        if (digestList.get(0).isThisHasMAC()){
            digest = new Digest(0, 0, 0, 0, 0, 0, 0, 0, 0,
                    true, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        }else{
            digest = new Digest(0, 0, 0, 0, 0, 0, 0, 0, 0, false);
        }

        for (Digest curDigest : digestList){
            digest.mergeOtherNewDigest(curDigest);
        }
        //digest.setChunkIdFrom(dataIdFrom);
        //digest.setChunkIdTo(dataIdTo);
        return digest;
    }

    //最初始版本
    public NodeContent[] getAggregation(String usrName, long steamId, long fromL, long toL, int[] ids) throws Exception {
        if (toL < fromL) {
            throw new IllegalArgumentException("From (" + fromL + ") is greater than to (" + toL + ")");
        }
        String TOPIC_NAME = usrName + steamId;
        NodeContent[] result = new NodeContent[ids.length];
        for (int i = 0; i < ids.length; i++) {
            int part = ids[i]+1;
            ConsumerRecords<Long, byte[]> records = KafkaReader.consumerChunks(TOPIC_NAME, part, fromL, toL);
            long sum = 0;
            LongMacNodeNodeContent node0 = new LongMacNodeNodeContent(0, BigInteger.ZERO);
            for (ConsumerRecord<Long, byte[]> record : records){
                LongMacNodeNodeContent nodeCur = LongMacNodeNodeContent.decode(record.value());
                node0.mergeOther(nodeCur);
//              System.out.printf("收到消息：partition = %d, offset = %d, key = %s, value = %s%n", record.partition(),record.offset(), record.key(), Arrays.toString(record.value()));
//              System.out.println("Node_"+i);
//              System.out.println("long: "+nodei.getLong());
//              System.out.println("mac:  "+nodei.getMac());
//              System.out.println("String:"+nodei.getStringRepresentation());
//              System.out.println("toString:"+nodei);
            }
            result[i] = node0;
            //System.out.println(node0.getStringRepresentation());
        }
        //System.out.println(result.length);
        return result;
    }


}
