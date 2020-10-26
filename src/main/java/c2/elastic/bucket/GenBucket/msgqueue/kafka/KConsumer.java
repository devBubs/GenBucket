package c2.elastic.bucket.GenBucket.msgqueue.kafka;

import c2.elastic.bucket.GenBucket.msgqueue.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class KConsumer<T,M> implements Consumer<M> {

    private final String consumerGroup;
    private final CustomConsumerConfig configuration;
    private final Properties properties;
    private final Collection<String> topics;
    private ExecutorService workers;

    public KConsumer(CustomConsumerConfig configuration, Collection<String> topics) {
        this.properties = new Properties();
        this.consumerGroup = configuration.getGroupId();
        this.configuration = configuration;
        this.topics = topics;
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,configuration.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, configuration.getKeyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, configuration.getValueDeserializer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, configuration.getFetchMinBytes());
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, configuration.getFetchMaxWait());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configuration.isEnableAutoCommit());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, configuration.getAutoOffsetReset());
    }

    @Override
    public void bind() {
        workers = Executors.newFixedThreadPool(configuration.getNumWorkers());
        for(int i=0; i<configuration.getNumWorkers(); ++i){
            workers.submit(new StartConsumerCallable());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    protected abstract void onFailedCommit(Exception exception);

    @Override
    public void shutdown() {
        workers.shutdownNow();
    }

    private class StartConsumerCallable implements Callable<Void>{
        @Override
        public Void call() {
            try(KafkaConsumer<T,M> kafkaConsumer = new KafkaConsumer<>(properties)){
                kafkaConsumer.subscribe(topics);
                log.info("{}: Starting a new kafka consumer", consumerGroup);
                while(true){
                    Duration duration = Duration.ofMillis(configuration.getPollTimeOutDuration());
                    ConsumerRecords<T, M> records = kafkaConsumer.poll(duration);
                    for(ConsumerRecord<T, M> record: records){
                        consume(record.value());
                    }
                    try{
                        kafkaConsumer.commitSync();
                    } catch (CommitFailedException e){
                        onFailedCommit(e);
                    }
                }
            }catch (Exception e){
                log.error("{}: Encountered error -> {}", consumerGroup, e);
            }
            return null;
        }
    }
}
