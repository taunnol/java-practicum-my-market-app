package ru.yandex.practicum.mymarket.items.service;

import ru.yandex.practicum.mymarket.items.model.ItemEntity;

import java.lang.reflect.Field;

final class TestEntityIds {

    private TestEntityIds() {
    }

    static void setId(ItemEntity entity, long id) {
        try {
            Field f = ItemEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
