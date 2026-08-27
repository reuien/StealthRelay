package kafka;

import index.Digest;
import org.apache.kafka.clients.producer.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class KafkaProducerTest {
    private final static String TOPIC_NAME = "testTopic1000001";

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        String usr = "testUsr";
        long streamId = 200000000L;
        String TOPIC_NAME = usr + streamId;
        KafkaTopicManager.createTopic(usr, streamId);


/*        Producer<Long, byte[]> producer = KafkaUploader.getProducerChunk();
        for (int i = 0; i < 100; i++) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(i);
            byte[] nodes_content = bos.toByteArray();
            for (int j = 0; j < 8; j++) {
                KafkaUploader.uploadChunk(producer, TOPIC_NAME, j, i, nodes_content);
            }
        }*/


        Producer<Long, String> kafkaProducerDigest = KafkaUploader.getProducerDigest();
        for (long i = 0; i < 10; i++) {
            Digest digest = new Digest(i, i ,i,false);
            KafkaUploader.uploadDigest(kafkaProducerDigest, TOPIC_NAME, 0, i, digest);
        }






//        int pos = 1101;
//        for (int i = pos; i <= pos+19; i++) {
//            long chunkID = i;
//            byte[] encChunk;
//            encChunk = java.lang.Long.toString(chunkID).getBytes();
//            int part = 1;
//            if(i <= pos+9)
//                part = 1;
//            else part = 3;
//            ProducerRecord<Long, byte[]> producerRecord = new ProducerRecord<Long, byte[]>(TOPIC_NAME, part, chunkID, encChunk);
//
//            //等待消息发送成功的同步阻塞方法
//            RecordMetadata metadata = producer.send(producerRecord).get();
//            System.out.println("metadata: "+metadata);
//            System.out.println("同步方式发送消息结果：" + " topic - " +metadata.topic() + " | partition - "+ metadata.partition() + " | offset - " +metadata.offset());
//        }



/*        int pos1 = 7001;
        for (int i = pos1; i <= pos1+19; i++) {
            long chunkID = i;
            byte[] encChunk;
            encChunk = java.lang.Long.toString(chunkID).getBytes();
            int part = 1;
            if(i <= pos1+9)
                part = 0;
            else part = 1;
            ProducerRecord<Long, byte[]> producerRecord = new ProducerRecord<Long, byte[]>(TOPIC_NAME, part, chunkID, encChunk);

            //异步回调方式发送消息
            producer.send(producerRecord, new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        System.err.println("发送消息失败：" +
                                exception.getStackTrace());
                    }
                    if (metadata != null) {
                        System.out.println("异步方式发送消息结果：" + " topic - " +metadata.topic() + " | partition - "+ metadata.partition() + " | offset - " + metadata.offset());
                    }
                }
            });
            Thread.sleep(3);
        }*/







    }
}
