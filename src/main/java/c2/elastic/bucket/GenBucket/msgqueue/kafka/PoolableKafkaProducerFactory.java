package c2.elastic.bucket.GenBucket.msgqueue.kafka;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

public class PoolableKafkaProducerFactory<T, M>  extends BasePooledObjectFactory<KafkaProducer<T, M>> {
    private final Properties producerProperties;

    public PoolableKafkaProducerFactory(Properties producerProperties) {
        this.producerProperties = producerProperties;
    }

    @Override
    public KafkaProducer<T, M> create() throws Exception {
        return new KafkaProducer<>(producerProperties);
    }

    @Override
    public PooledObject<KafkaProducer<T, M>> wrap(KafkaProducer<T, M> kafkaProducer) {
        return new DefaultPooledObject<>(kafkaProducer);
    }
}
