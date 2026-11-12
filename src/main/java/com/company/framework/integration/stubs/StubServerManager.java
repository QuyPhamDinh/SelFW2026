package com.company.framework.integration.stubs;

public interface StubServerManager extends AutoCloseable {
    void start();
    void stop();

    @Override
    default void close() {
        stop();
    }
}
