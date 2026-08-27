package kafka;

import com.alibaba.fastjson.JSON;
import index.Digest;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;


import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaUploader {
    private final static String KAFKA_SERVER = "localhost:9092";

    public static void uploadChunk(Producer<Long, byte[]> producer, String TOPIC_NAME, int part, long chunkID,
                                   byte[] encChunk) throws ExecutionException, InterruptedException {
        ProducerRecord<Long, byte[]> producerRecord = new ProducerRecord<>(TOPIC_NAME, part, chunkID, encChunk);
        RecordMetadata metadata = producer.send(producerRecord).get();
/*        System.out.println("metadata: "+metadata);
        System.out.println("同步方式发送消息结果：" + " topic - " +metadata.topic() + " | partition - " +
                metadata.partition() + " | offset - " +metadata.offset());*/
    }

    public static void uploadDigest(Producer<Long, String> producer, String TOPIC_NAME, int part, long chunkID,
                                    Digest digest) throws ExecutionException, InterruptedException {

        ProducerRecord<Long, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, part, chunkID, JSON.toJSONString(digest));
        RecordMetadata metadata = producer.send(producerRecord).get();
/*        System.out.println("metadata: "+metadata);
        System.out.println("同步方式发送消息结果：" + " topic - " +metadata.topic() + " | partition - " +
                metadata.partition() + " | offset - " +metadata.offset());*/
    }

    public static Producer<Long, byte[]> getProducerChunk(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        Producer<Long, byte[]> producer = new KafkaProducer<>(props);
        return producer;
    }

    public static Producer<Long, String> getProducerDigest(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        Producer<Long, String> producer = new KafkaProducer<>(props);
        return producer;
    }


}