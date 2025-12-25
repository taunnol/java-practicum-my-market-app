package ru.yandex.practicum.mymarket.common.util;

import ru.yandex.practicum.mymarket.items.dto.ItemDto;

import java.util.ArrayList;
import java.util.List;

public final class GridUtils {

    private GridUtils() {
    }

    public static List<List<ItemDto>> toRowsOf3WithPlaceholders(List<ItemDto> items) {
        List<List<ItemDto>> rows = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return rows;
        }

        int i = 0;
        while (i < items.size()) {
            List<ItemDto> row = new ArrayList<>(3);
            for (int k = 0; k < 3; k++) {
                if (i < items.size()) {
                    row.add(items.get(i));
                    i++;
                } else {
                    row.add(placeholder());
                }
            }
            rows.add(row);
        }
        return rows;
    }

    public static ItemDto placeholder() {
        return new ItemDto(-1L, "", "", "", 0L, 0);
    }
}
