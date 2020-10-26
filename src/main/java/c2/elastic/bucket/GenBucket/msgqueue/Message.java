package c2.elastic.bucket.GenBucket.msgqueue;

import lombok.Data;

import java.util.Map;

@Data
public class Message<K, V> {
    private V data;
    private K routingKey;
}
