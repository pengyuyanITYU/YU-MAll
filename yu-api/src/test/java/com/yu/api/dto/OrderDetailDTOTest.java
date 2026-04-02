package com.yu.api.dto;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OrderDetailDTOTest {

    @Test
    void shouldBeSerializable() {
        OrderDetailDTO dto = new OrderDetailDTO()
                .setItemId(1L)
                .setNum(2);

        assertDoesNotThrow(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
                objectOutputStream.writeObject(dto);
            }
        });
    }
}
