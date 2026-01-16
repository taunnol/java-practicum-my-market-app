package ru.yandex.practicum.mymarket.items.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.CartAction;
import ru.yandex.practicum.mymarket.common.dto.ItemAction;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.common.util.GridUtils;
import ru.yandex.practicum.mymarket.items.service.ItemCatalogService;

@Controller
@Validated
public class ItemsController {

    private final ItemCatalogService itemCatalogService;
    private final CartService cartService;

    public ItemsController(ItemCatalogService itemCatalogService, CartService cartService) {
        this.itemCatalogService = itemCatalogService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "NO") SortMode sort,
            @RequestParam(required = false, defaultValue = "1") @Positive int pageNumber,
            @RequestParam(required = false, defaultValue = "5") @Positive int pageSize,
            Model model
    ) {
        return itemCatalogService.getCatalogPage(search, sort, pageNumber, pageSize)
                .map(page -> {
                    model.addAttribute("items", GridUtils.toRowsOf3WithPlaceholders(page.items()));
                    model.addAttribute("search", search == null ? "" : search);
                    model.addAttribute("sort", sort.name());
                    model.addAttribute("paging", page.paging());
                    return "items";
                });
    }

    @PostMapping("/items")
    public Mono<String> changeCountFromItems(@Valid @ModelAttribute ItemsActionForm form) {
        CartAction cartAction = (form.getAction() == ItemAction.PLUS) ? CartAction.PLUS : CartAction.MINUS;

        String redirect = UriComponentsBuilder.fromPath("/items")
                .queryParam("search", form.getSearch())
                .queryParam("sort", form.getSort().name())
                .queryParam("pageNumber", form.getPageNumber())
                .queryParam("pageSize", form.getPageSize())
                .build()
                .toUriString();

        return cartService.changeCount(form.getId(), cartAction)
                .thenReturn("redirect:" + redirect);
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable("id") long id, Model model) {
        return renderItem(id, model);
    }

    @PostMapping("/items/{id}")
    public Mono<String> changeCountFromItem(
            @PathVariable("id") long id,
            @Valid @ModelAttribute ItemDetailsActionForm form,
            Model model
    ) {
        CartAction cartAction = (form.getAction() == ItemAction.PLUS) ? CartAction.PLUS : CartAction.MINUS;
        return cartService.changeCount(id, cartAction)
                .then(renderItem(id, model));
    }

    private Mono<String> renderItem(long id, Model model) {
        return itemCatalogService.getItem(id)
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item";
                });
    }
}
