package c2.elastic.bucket.GenBucket.mqueue.rabbitmq;

import c2.elastic.bucket.GenBucket.mqueue.Queue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RmqQueue implements Queue {
    private String queueName;
    private String hosts;
    private String username;
    private String password;
    private int port;
}
