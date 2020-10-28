package c2.elastic.bucket.GenBucket.msgqueue.exception;

public class ProducerException extends RuntimeException {
    public ProducerException(String message){
        super(message);
    }

    public ProducerException(String message, Throwable cause){
        super(message, cause);
    }
}
