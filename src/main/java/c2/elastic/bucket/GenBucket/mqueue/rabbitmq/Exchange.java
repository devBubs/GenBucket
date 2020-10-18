package c2.elastic.bucket.GenBucket.mqueue.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Exchange {
    private String exchangeName;
    private String exchangeType;
}
