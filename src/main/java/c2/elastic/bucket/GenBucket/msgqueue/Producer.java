package c2.elastic.bucket.GenBucket.msgqueue;

import java.util.List;

public abstract class Producer<K,V> {
    protected abstract void bind();
    protected abstract void publish(Message<K,V> message);
    protected abstract void publish(List<Message<K,V>> messages);
    protected abstract void shutdown();

    public abstract void produce(V data);
    public abstract void produce(List<V> data);
}
