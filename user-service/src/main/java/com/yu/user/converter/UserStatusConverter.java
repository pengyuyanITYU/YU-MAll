package com.yu.user.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.yu.user.enums.UserStatus;

/**
 * 用户状态转换器
 * 将 UserStatus 枚举对象转换为 String (例如："已激活")
 */
// 1. 泛型修改为 UserStatus (之前是 Integer)
public class UserStatusConverter implements Converter<UserStatus> {

    @Override
    public Class<?> supportJavaTypeKey() {
        // 2. 这里必须返回枚举的 Class，告诉 EasyExcel 这个转换器是给 UserStatus 用的
        return UserStatus.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(UserStatus value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        // 3. 直接处理枚举对象
        if (value == null) {
            return new WriteCellData<>("");
        }

        // 4. 不需要再调用 UserStatus.of(value)，因为 value 已经是枚举对象了
        // 直接获取枚举中的描述信息 (假设你的枚举中有 getDesc 方法)
        return new WriteCellData<>(value.getDesc());
    }
}