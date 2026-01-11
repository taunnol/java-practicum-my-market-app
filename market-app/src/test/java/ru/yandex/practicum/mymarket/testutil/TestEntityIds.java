package ru.yandex.practicum.mymarket.testutil;

import java.lang.reflect.Field;

public final class TestEntityIds {

    private TestEntityIds() {
    }

    public static void setId(Object entity, long id) {
        Class<?> c = entity.getClass();
        try {
            Field f = c.getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
