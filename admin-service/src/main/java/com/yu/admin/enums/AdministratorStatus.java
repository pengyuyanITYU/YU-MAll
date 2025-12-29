package com.yu.admin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdministratorStatus {
    FROZEN(1, "禁用"),
    NORMAL(0, "正常"),
    ;
    @EnumValue
    @JsonValue
    int value;

    String desc;

    AdministratorStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static AdministratorStatus of(int value) {
        if (value == 1) {
            return FROZEN;
        }
        if (value == 0) {
            return NORMAL;
        }
        throw new RuntimeException("账户状态错误");
    }
}