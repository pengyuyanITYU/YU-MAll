package com.yu.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    FROZEN(2, "禁止使用"),
    NORMAL(1, "已激活"),
    ;
    @EnumValue
    @JsonValue
    int value;

    String desc;

    UserStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static UserStatus of(int value) {
        if (value == 2) {
            return FROZEN;
        }
        if (value == 1) {
            return NORMAL;
        }
        throw new RuntimeException("账户状态错误");
    }
}