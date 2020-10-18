package c2.elastic.bucket.GenBucket.mqueue.rabbitmq;

import lombok.Data;

@Data
public class RmqProducerConfig {
    private int channel_pool_max_size;
    private int channel_pool_max_wait;
}
