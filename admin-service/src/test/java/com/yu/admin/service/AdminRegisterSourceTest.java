package com.yu.admin.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminRegisterSourceTest {

    @Test
    void registerShouldPersistPhoneFromRegisterForm() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/yu/admin/service/impl/AdminServiceImpl.java"));

        assertTrue(source.contains(".setPhone(phone)"),
                "register should persist phone to Administrator entity");
    }
}
