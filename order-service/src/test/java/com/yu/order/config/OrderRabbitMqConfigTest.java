package com.yu.order.config;

import com.yu.order.domain.dto.OrderDetailDTO;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRabbitMqConfigTest {

    @Test
    void messageConverter_shouldUseJacksonJsonConverter() {
        OrderRabbitMqConfig config = new OrderRabbitMqConfig();

        MessageConverter converter = config.messageConverter();

        assertInstanceOf(Jackson2JsonMessageConverter.class, converter);
    }

    @Test
    void messageConverter_shouldSerializeOrderDetailListAsJson() {
        OrderRabbitMqConfig config = new OrderRabbitMqConfig();
        MessageConverter converter = config.messageConverter();
        OrderDetailDTO detailDTO = new OrderDetailDTO()
                .setItemId(2L)
                .setNum(1)
                .setPrice(499900L)
                .setImage("https://example.com/test.png")
                .setSpecs(Map.of("颜色", "默认"));

        Message message = converter.toMessage(List.of(detailDTO), new MessageProperties());
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        assertTrue(message.getMessageProperties().getContentType().contains("json"));
        assertTrue(body.contains("\"itemId\":2"));
    }
}
