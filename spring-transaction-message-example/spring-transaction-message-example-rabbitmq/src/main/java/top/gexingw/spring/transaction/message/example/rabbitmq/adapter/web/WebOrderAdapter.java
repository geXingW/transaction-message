package top.gexingw.spring.transaction.message.example.rabbitmq.adapter.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.gexingw.spring.transaction.message.example.rabbitmq.application.service.order.OrderCommandService;

/**
 * @author GeXingW
 */
@RestController
@RequestMapping("/web/order")
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class WebOrderAdapter {

    private final OrderCommandService orderCommandService;

    @GetMapping
    public Object index(Long id) {
        orderCommandService.create(id);
        return "Ok...";
    }

}
