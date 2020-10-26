package c2.elastic.bucket.GenBucket.msgqueue;

import java.util.List;

public interface Consumer<M> {
    void consume(List<M> messages);
    void bind();
    void shutdown();
}
