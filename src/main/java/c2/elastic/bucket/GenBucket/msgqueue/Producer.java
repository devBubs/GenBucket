package c2.elastic.bucket.GenBucket.msgqueue;

public interface Producer<M> {
    void bind();
    void produce(M message);
    void shutdown();
}
