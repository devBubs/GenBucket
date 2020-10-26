package c2.elastic.bucket.GenBucket.msgqueue;

import java.util.Map;

public class Message<T> {
    private T data;
    private Map<String, String> properties;
    private String routingKey;
}
