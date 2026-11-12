package com.company.framework.integration.db;

public interface DbClient extends AutoCloseable {
    Object query(String sql, Object... parameters);

    @Override
    default void close() {}
}
