package ru.yandex.practicum.mymarket.common.util;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GridUtilsTest {

    @Test
    void toRowsOf3WithPlaceholders_addsPlaceholdersToLastRow() {
        List<ItemDto> items = List.of(
                new ItemDto(1, "t1", "d1", "/i1", 10, 0),
                new ItemDto(2, "t2", "d2", "/i2", 20, 0),
                new ItemDto(3, "t3", "d3", "/i3", 30, 0),
                new ItemDto(4, "t4", "d4", "/i4", 40, 0)
        );

        List<List<ItemDto>> rows = GridUtils.toRowsOf3WithPlaceholders(items);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).extracting(ItemDto::id).containsExactly(1L, 2L, 3L);
        assertThat(rows.get(1)).extracting(ItemDto::id).containsExactly(4L, -1L, -1L);
    }

    @Test
    void toRowsOf3WithPlaceholders_emptyList_returnsEmptyRows() {
        assertThat(GridUtils.toRowsOf3WithPlaceholders(List.of())).isEmpty();
    }
}
