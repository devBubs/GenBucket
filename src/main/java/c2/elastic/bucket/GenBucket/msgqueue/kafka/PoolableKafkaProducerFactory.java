package c2.elastic.bucket.GenBucket.msgqueue.kafka;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serializer

import java.util.Properties;

public class PoolableKafkaProducerFactory<T, M>  extends BasePooledObjectFactory<KafkaProducer<T, M>> {
    private final Properties producerProperties;
    private final Serializer<T> keySerializer;
    private final Serializer<M> valueSerializer;

    public PoolableKafkaProducerFactory(Properties producerProperties, Serializer<T> keySerializer, Serializer<M> valueSerializer) {
        this.producerProperties = producerProperties;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public KafkaProducer<T, M> create() throws Exception {
        return new KafkaProducer<T, M>(producerProperties, keySerializer, valueSerializer);
    }

    @Override
    public PooledObject<KafkaProducer<T, M>> wrap(KafkaProducer<T, M> kafkaProducer) {
        return new DefaultPooledObject<>(kafkaProducer);
    }
}
