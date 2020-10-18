package c2.elastic.bucket.GenBucket.mqueue.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RmqConsumerConfig {
    private int numConsumers;
    private int prefetchCount;
    private int maxRetries;
    private boolean autoAck;
    private boolean rejectAllUntilDelivery;
}
