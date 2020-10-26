package c2.elastic.bucket.GenBucket.msgqueue.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;

@ToString
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomConsumerConfig {
    private String groupId;
    private Collection<String> topics;
    private int numWorkers;
    private long pollTimeOutDuration;
    private String bootstrapServers;
    private String keyDeserializer;
    private String valueDeserializer;
    private long fetchMinBytes;
    private long fetchMaxWait;
    private boolean enableAutoCommit;
    private String autoOffsetReset;
}
