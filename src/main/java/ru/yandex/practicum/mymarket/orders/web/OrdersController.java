package ru.yandex.practicum.mymarket.orders.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.orders.dto.OrderDto;
import ru.yandex.practicum.mymarket.orders.service.OrderService;

@Controller
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        model.addAttribute("orders", orderService.getOrders());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(
            @PathVariable("id") long id,
            @RequestParam(name = "newOrder", required = false, defaultValue = "false") boolean newOrder,
            Model model
    ) {
        OrderDto order = orderService.getOrder(id);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }
}
