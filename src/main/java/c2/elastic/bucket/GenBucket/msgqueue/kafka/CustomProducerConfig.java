package c2.elastic.bucket.GenBucket.msgqueue.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomProducerConfig {
    private int numWorkers;
    private String bootstrapServers;
    private String topic;
    private String keySerializer;
    private String valueSerializer;
    private long producerPoolMaxWait;
}
