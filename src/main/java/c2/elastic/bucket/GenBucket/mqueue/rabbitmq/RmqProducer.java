package c2.elastic.bucket.GenBucket.mqueue.rabbitmq;

import c2.elastic.bucket.GenBucket.mqueue.Producer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public abstract class RmqProducer<T> extends Producer<RmqMessage> {

    private final Exchange exchange;
    private final GenericObjectPool<Channel> channelPool;
    private final Connection connection;
    private final RmqProducerConfig producerConfig;

    public RmqProducer(Exchange exchange, Connection connection, RmqProducerConfig producerConfig) throws IOException, TimeoutException {
        this.exchange = exchange;
        this.connection = connection;
        this.producerConfig = producerConfig;
        channelPool = getChannelPool();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    protected abstract RmqMessage convert(T message);

    public void produce(T message) throws Exception {
        publish(convert(message));
    }

    @Override
    protected void publish(RmqMessage rmqMessage) throws Exception {
        Channel channel = channelPool.borrowObject();
        channel.basicPublish(exchange.getExchangeName(), rmqMessage.getRoutingKey(), null, rmqMessage.getBody());
        channelPool.returnObject(channel);
    }

    @Override
    public void shutdown() {
        log.info("Shutting down producer");
        if(connection!=null && connection.isOpen()) {
            try {
                channelPool.close();
                connection.close();
            } catch (IOException e) {
                log.warn("Unable to close producer connection");
            }
        }
    }

    private GenericObjectPool<Channel> getChannelPool(){
        GenericObjectPoolConfig<Channel> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(producerConfig.getChannel_pool_max_size());
        config.setMaxWaitMillis(producerConfig.getChannel_pool_max_wait());
        return new GenericObjectPool<>(new PoolableChannelFactory(connection), config);
    }
}
