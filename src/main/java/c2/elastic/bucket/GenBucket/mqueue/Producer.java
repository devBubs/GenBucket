package c2.elastic.bucket.GenBucket.mqueue;

public abstract class Producer<M> {
    protected abstract void publish(M m) throws Exception;

    public abstract void shutdown();
}
