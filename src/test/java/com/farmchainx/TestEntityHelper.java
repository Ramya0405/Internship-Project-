package com.farmchainx;

import org.springframework.test.util.ReflectionTestUtils;

public final class TestEntityHelper {

    private TestEntityHelper() {}

    public static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
