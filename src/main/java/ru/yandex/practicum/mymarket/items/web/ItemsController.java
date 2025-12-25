package ru.yandex.practicum.mymarket.items.web;

import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.CartAction;
import ru.yandex.practicum.mymarket.common.dto.ItemAction;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.common.util.GridUtils;
import ru.yandex.practicum.mymarket.items.service.CatalogPage;
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
    public String getItems(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "NO") SortMode sort,
            @RequestParam(required = false, defaultValue = "1") @Positive int pageNumber,
            @RequestParam(required = false, defaultValue = "5") @Positive int pageSize,
            Model model
    ) {
        CatalogPage page = itemCatalogService.getCatalogPage(search, sort, pageNumber, pageSize);

        model.addAttribute("items", GridUtils.toRowsOf3WithPlaceholders(page.items()));
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("sort", sort.name());
        model.addAttribute("paging", page.paging());

        return "items";
    }

    @PostMapping("/items")
    public String changeCountFromItems(
            @RequestParam("id") @Positive long id,
            @RequestParam("action") ItemAction action,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "NO") SortMode sort,
            @RequestParam(required = false, defaultValue = "1") @Positive int pageNumber,
            @RequestParam(required = false, defaultValue = "5") @Positive int pageSize,
            RedirectAttributes redirectAttributes
    ) {
        CartAction cartAction = (action == ItemAction.PLUS) ? CartAction.PLUS : CartAction.MINUS;
        cartService.changeCount(id, cartAction);

        redirectAttributes.addAttribute("search", search == null ? "" : search);
        redirectAttributes.addAttribute("sort", sort.name());
        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);

        return "redirect:/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(
            @PathVariable("id") @Positive long id,
            Model model
    ) {
        return renderItem(id, model);
    }

    @PostMapping("/items/{id}")
    public String changeCountFromItemPage(
            @PathVariable("id") @Positive long id,
            @RequestParam("action") ItemAction action,
            Model model
    ) {
        CartAction cartAction = (action == ItemAction.PLUS) ? CartAction.PLUS : CartAction.MINUS;
        cartService.changeCount(id, cartAction);

        return renderItem(id, model);
    }

    private String renderItem(long id, Model model) {
        model.addAttribute("item", itemCatalogService.getItem(id));
        return "item";
    }
}
