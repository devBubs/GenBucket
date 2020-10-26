package c2.elastic.bucket.GenBucket.msgqueue.kafka;

import c2.elastic.bucket.GenBucket.msgqueue.Message;
import c2.elastic.bucket.GenBucket.msgqueue.Producer;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class KProducer<K, V> extends Producer<K, V> {

    private final CustomProducerConfig configuration;
    private final Properties properties;
    private GenericObjectPool<KafkaProducer<K, V>> producerPool;

    public KProducer(CustomProducerConfig configuration) {
        this.configuration = configuration;
        this.properties = new Properties();
    }

    @Override
    public void bind() {
        GenericObjectPoolConfig<KafkaProducer<K, V>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(configuration.getNumWorkers());
        poolConfig.setMaxWaitMillis(configuration.getProducerPoolMaxWait());
        this.producerPool = new GenericObjectPool<>(new PoolableKafkaProducerFactory<>(properties));
    }

    protected abstract Message<K, V> convert(V message);

    @Override
    protected void publish(Message<K, V> message) {

    }

    @Override
    protected void publish(List<Message<K, V>> messages) {

    }

    @Override
    public void produce(V data) {
        publish(convert(data));
    }

    @Override
    public void produce(List<V> data) {
        publish(data.stream().map(this::convert).collect(Collectors.toList()));
    }

    @Override
    public void shutdown() {

    }
}
