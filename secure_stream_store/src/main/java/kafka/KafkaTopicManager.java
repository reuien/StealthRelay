package kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KafkaTopicManager {
    private final static String KAFKA_SERVER = "localhost:9092";
    private static final long TOPIC_ADMIN_TIMEOUT_SECONDS = 5L;

    public static void createTopic(String userName, long sid) throws ExecutionException, InterruptedException {

        String NEW_TOPIC_NAME = userName + sid;
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", KAFKA_SERVER);
        props.setProperty("request.timeout.ms", String.valueOf(TOPIC_ADMIN_TIMEOUT_SECONDS * 1000));
        props.setProperty("default.api.timeout.ms", String.valueOf(TOPIC_ADMIN_TIMEOUT_SECONDS * 1000));
        try (AdminClient adminClient = AdminClient.create(props)) {
            NewTopic newTopic = new NewTopic(NEW_TOPIC_NAME, 8, (short) 1);
            try {
                adminClient.createTopics(Collections.singleton(newTopic)).all()
                        .get(TOPIC_ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new ExecutionException("Kafka topic create timed out after "
                        + TOPIC_ADMIN_TIMEOUT_SECONDS + " seconds", e);
            }
        }
        System.out.println("Topic created successfully");
    }

    public static void deleteTopic(String userName, long sid) throws ExecutionException, InterruptedException {

        String DELETE_TOPIC_NAME = userName + sid;
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", KAFKA_SERVER);
        props.setProperty("request.timeout.ms", String.valueOf(TOPIC_ADMIN_TIMEOUT_SECONDS * 1000));
        props.setProperty("default.api.timeout.ms", String.valueOf(TOPIC_ADMIN_TIMEOUT_SECONDS * 1000));
        try (AdminClient adminClient = AdminClient.create(props)) {
            DeleteTopicsResult result = adminClient.deleteTopics(Collections.singleton(DELETE_TOPIC_NAME));
            result.values().get(DELETE_TOPIC_NAME).get(TOPIC_ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("Topic deleted successfully");
        } catch (TimeoutException e) {
            System.out.println("Error deleting topic: " + e.getMessage());
            throw new ExecutionException("Kafka topic delete timed out after "
                    + TOPIC_ADMIN_TIMEOUT_SECONDS + " seconds", e);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error deleting topic: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw e;
        }
    }

}
