package c2.elastic.bucket.GenBucket.mqueue;

public interface Consumer<Q extends Queue, M> {
    void consume(M message);
    void bind(Q queue);
    void shutdown();
}
