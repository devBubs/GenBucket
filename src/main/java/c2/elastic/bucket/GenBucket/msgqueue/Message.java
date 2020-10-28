package c2.elastic.bucket.GenBucket.msgqueue;

import javafx.beans.DefaultProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message<K, V> {
    private V data;
    private K routingKey;
}
