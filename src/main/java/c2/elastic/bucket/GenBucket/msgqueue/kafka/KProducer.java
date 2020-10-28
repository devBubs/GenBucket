package c2.elastic.bucket.GenBucket.msgqueue.kafka;

import c2.elastic.bucket.GenBucket.msgqueue.Message;
import c2.elastic.bucket.GenBucket.msgqueue.Producer;
import c2.elastic.bucket.GenBucket.msgqueue.exception.ProducerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public abstract class KProducer<K, V> extends Producer<K, V> {

    private final CustomProducerConfig configuration;
    private final Properties properties;
    private GenericObjectPool<KafkaProducer<K, V>> producerPool;

    public KProducer(CustomProducerConfig configuration) {
        this.configuration = configuration;
        this.properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, configuration.getKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, configuration.getValueSerializer());
    }

    @Override
    public void bind() {
        GenericObjectPoolConfig<KafkaProducer<K, V>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(configuration.getNumWorkers());
        poolConfig.setMaxWaitMillis(configuration.getProducerPoolMaxWait());
        this.producerPool = new GenericObjectPool<>(new PoolableKafkaProducerFactory<>(properties));
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    protected abstract Message<K, V> convert(V message);

    @Override
    protected void publish(Message<K, V> message) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<K,V>(configuration.getTopic(), message.getRoutingKey(), message.getData());
        KafkaProducer<K, V> kafkaProducer;
        try {
            kafkaProducer = producerPool.borrowObject();
        } catch (Exception e) {
            throw new ProducerException(this.getClass().getSimpleName() + ": Producer pool throttling", e);
        }
        try {
            kafkaProducer.send(producerRecord).get();
        } catch (InterruptedException e) {
            log.warn("{} is interrupted during publish", this.getClass().getSimpleName());
        } catch (ExecutionException e) {
            throw new ProducerException("Publish failed for message", e);
        } finally {
            producerPool.returnObject(kafkaProducer);
        }
    }

    @Override
    protected void publish(List<Message<K, V>> messages) {
        KafkaProducer<K, V> kafkaProducer;
        try {
            kafkaProducer = producerPool.borrowObject();
        } catch (Exception e) {
            throw new ProducerException(this.getClass().getSimpleName() + ": Producer pool throttling", e);
        }
        List<Future<RecordMetadata>> futures = messages.stream().map(message -> {
            ProducerRecord<K, V> record = new ProducerRecord<>(configuration.getTopic(), message.getRoutingKey(), message.getData());
            return kafkaProducer.send(record);
        }).collect(Collectors.toList());
        try {
            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    log.warn("{} is interrupted during publish", this.getClass().getSimpleName());
                } catch (ExecutionException e) {
                    throw new ProducerException(this.getClass().getSimpleName() + ": Publish failed for one or more messages", e);
                }
            });
        } finally {
           producerPool.returnObject(kafkaProducer);
        }
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
        producerPool.close();
    }
}
