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
public class KafkaTopic {
    private String brokerAddress;
    private String name;
    private int numPartitions;
}
