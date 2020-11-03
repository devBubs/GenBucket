package c2.elastic.bucket.GenBucket.msgqueue.kafka;

import c2.elastic.bucket.GenBucket.msgqueue.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public abstract class KConsumer<K, V> implements Consumer<V> {

    private final CustomConsumerConfig configuration;
    private final Properties properties;
    private ExecutorService workers;
    private final Deserializer<K> keyDeserializer;
    private final Deserializer<V> valueDeserializer;

    public KConsumer(CustomConsumerConfig configuration, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
        this.properties = new Properties();
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
        this.configuration = configuration;
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, configuration.getGroupId());
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, configuration.getFetchMinBytes());
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, configuration.getFetchMaxWait());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configuration.isEnableAutoCommit());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, configuration.getAutoOffsetReset());
    }

    @Override
    public void bind() {
        workers = Executors.newFixedThreadPool(configuration.getNumWorkers());
        for (int i = 0; i < configuration.getNumWorkers(); ++i) {
            workers.submit(new StartConsumerCallable());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    protected abstract void onFailedCommit(Exception exception);

    @Override
    public void shutdown() {
        workers.shutdownNow();
    }

    private class StartConsumerCallable implements Callable<Void> {
        //Supports only commitSync as of now.
        //TODO: add support of autoCommit and CommitAsync.
        @Override
        public Void call() {
            try (KafkaConsumer<K, V> kafkaConsumer = new KafkaConsumer<>(properties, keyDeserializer, valueDeserializer)) {
                kafkaConsumer.subscribe(configuration.getTopics());
                log.info("{}: Starting a new kafka consumer", configuration.getGroupId());
                while (true) {
                    Duration duration = Duration.ofMillis(configuration.getPollTimeOutDuration());
                    ConsumerRecords<K, V> records = kafkaConsumer.poll(duration);
                    final List<V> messages = StreamSupport.stream(records.spliterator(), false)
                            .map(ConsumerRecord::value)
                            .collect(Collectors.toList());
                    try {
                        consume(messages);
                    } catch (Exception e) {
                        // TODO: support sidelining
                        log.error("{}: Consume failed -> ", configuration.getGroupId(), e);
                    }
                    try {
                        kafkaConsumer.commitSync();
                    } catch (CommitFailedException e) {
                        onFailedCommit(e);
                    }
                }
            } catch (Exception e) {
                log.error("{}: Encountered error -> ", configuration.getGroupId(), e);
            }
            return null;
        }
    }
}
