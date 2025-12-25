package ru.yandex.practicum.mymarket.items.repo;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;

@Repository
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final DatabaseClient client;

    public ItemRepositoryImpl(DatabaseClient client) {
        this.client = client;
    }

    private static String sortClause(SortMode sortMode) {
        return switch (sortMode) {
            case NO -> "";
            case ALPHA -> " ORDER BY title ASC ";
            case PRICE -> " ORDER BY price ASC ";
        };
    }

    @Override
    public Flux<ItemEntity> findPage(String search, SortMode sort, int limit, int offset) {
        String q = (search == null) ? "" : search.trim();
        SortMode sortMode = (sort == null) ? SortMode.NO : sort;

        StringBuilder sql = new StringBuilder("""
                SELECT id, title, description, img_path, price
                FROM items
                """);

        boolean hasSearch = !q.isEmpty();
        if (hasSearch) {
            sql.append(" WHERE LOWER(title) LIKE :q OR LOWER(description) LIKE :q ");
        }

        sql.append(sortClause(sortMode));
        sql.append(" LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = client.sql(sql.toString())
                .bind("limit", limit)
                .bind("offset", offset);

        if (hasSearch) {
            spec = spec.bind("q", "%" + q.toLowerCase() + "%");
        }

        return spec.map((row, meta) -> {
            ItemEntity e = new ItemEntity();
            e.setId(row.get("id", Long.class));
            e.setTitle(row.get("title", String.class));
            e.setDescription(row.get("description", String.class));
            e.setImgPath(row.get("img_path", String.class));
            e.setPrice(row.get("price", Long.class));
            return e;
        }).all();
    }

    @Override
    public Mono<Long> countBySearch(String search) {
        String q = (search == null) ? "" : search.trim();

        boolean hasSearch = !q.isEmpty();

        String sql = hasSearch
                ? "SELECT COUNT(*) AS cnt FROM items WHERE LOWER(title) LIKE :q OR LOWER(description) LIKE :q"
                : "SELECT COUNT(*) AS cnt FROM items";

        DatabaseClient.GenericExecuteSpec spec = client.sql(sql);
        if (hasSearch) {
            spec = spec.bind("q", "%" + q.toLowerCase() + "%");
        }

        return spec.map((row, meta) -> row.get("cnt", Long.class)).one();
    }
}
