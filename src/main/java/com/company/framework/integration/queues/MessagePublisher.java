package com.company.framework.integration.queues;

public interface MessagePublisher extends AutoCloseable {
    void publish(String topic, String payload);

    @Override
    default void close() {}
}
