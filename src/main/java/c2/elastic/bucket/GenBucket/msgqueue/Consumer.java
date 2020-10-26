package c2.elastic.bucket.GenBucket.msgqueue;

public interface Consumer<M> {
    void consume(M message);
    void bind();
    void shutdown();
}
