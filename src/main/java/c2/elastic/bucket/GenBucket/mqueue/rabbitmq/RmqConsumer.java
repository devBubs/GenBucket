package c2.elastic.bucket.GenBucket.mqueue.rabbitmq;

import c2.elastic.bucket.GenBucket.mqueue.Consumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.min;

@Slf4j
public abstract class RmqConsumer implements Consumer<RmqQueue, RmqMessage> {

    private final RmqQueue queue;
    private ExecutorService workers;
    private final Connection connection;
    private final RmqConsumerConfig consumerConfig;
    private final int WAIT = 10000;

    protected RmqConsumer(RmqQueue queue, Connection connection, RmqConsumerConfig consumerConfig) {
        this.queue = queue;
        this.connection = connection;
        this.consumerConfig = consumerConfig;
    }

    //protected abstract void preAck(RmqMessage message);

    protected abstract void onFailedConsume(RmqMessage message);

    @Override
    public void bind() {
        workers = Executors.newFixedThreadPool(consumerConfig.getNumConsumers());
        for (int i = 0; i < consumerConfig.getNumConsumers(); ++i) {
            workers.submit(new StartConsumerCallable());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void shutdown() {
        log.info("Shutting down consumer");
        workers.shutdownNow();
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                log.warn("Connection for consumers could not be closed");
            }
        }
    }

    private class StartConsumerCallable implements Callable<Void> {

        @Override
        public Void call() throws IOException {
            Channel channel = connection.createChannel();
            channel.basicQos(consumerConfig.getPrefetchCount());
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                RmqMessage message = RmqMessage.builder().body(delivery.getBody()).build();
                int waitBeforeRetry;
                int numTries = 0;
                while (numTries <= consumerConfig.getMaxRetries()) {
                    try {
                        consume(message);
//                        if (!consumerConfig.isAutoAck()) {
//                            preAck(message);
//                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), consumerConfig.isRejectAllUntilDelivery());
//                        }
                        break;
                    } catch (Exception e) {
                        numTries++;
                        waitBeforeRetry = min(WAIT, 1 << numTries);
                        log.warn("Could not consume message {}, retrying after " + waitBeforeRetry + " ms", e.getMessage());
                        try {
                            Thread.sleep(waitBeforeRetry);
                        } catch (InterruptedException interruptedException) {
                            log.warn("Wait before retry interrupted");
                        }
                    }
                }
                if (numTries > consumerConfig.getMaxRetries()) {
                    log.error("MaxRetries reached: Could not consume message");
                    onFailedConsume(message);
                }

            };
            log.info("Starting consumer");
            channel.basicConsume(queue.getQueueName(), consumerConfig.isAutoAck(), deliverCallback, consumerTag -> {});
            return null;
        }
    }
}
