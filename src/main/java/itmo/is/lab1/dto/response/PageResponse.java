package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.Page;

import java.util.function.Function;

import java.util.List;

public record PageResponse<T>(List<T> items, long total, int page, int size) {
    public PageResponse {
        items = List.copyOf(items);
    }

    public static <S, T> PageResponse<T> from(
            Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.items().stream().map(mapper).toList(),
                page.total(),
                page.page(),
                page.size()
        );
    }
}
