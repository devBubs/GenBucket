package c2.elastic.bucket.GenBucket.mqueue.rabbitmq;

import com.rabbitmq.client.AMQP;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RmqMessage {
    private byte[] body;
    private AMQP.BasicProperties properties;
    private String routingKey;
}
