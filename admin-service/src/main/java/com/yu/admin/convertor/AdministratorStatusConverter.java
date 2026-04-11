package com.yu.admin.convertor;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.yu.admin.enums.AdministratorStatus;

/**
 * 管理员状态转换器
 * 将 AdministratorStatus 枚举转换为 String ("正常"/"禁用")
 */
// 1. 修改泛型为 AdministratorStatus
public class AdministratorStatusConverter implements Converter<AdministratorStatus> {

    @Override
    public Class<?> supportJavaTypeKey() {
        // 2. 这里必须返回枚举的 Class，而不是 Integer.class
        return AdministratorStatus.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(AdministratorStatus value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        // 3. 直接处理枚举对象
        if (value == null) {
            return new WriteCellData<>("");
        }

        // 因为传入的已经是枚举对象了，不需要再调用 of() 方法去查找
        // 直接获取描述即可
        return new WriteCellData<>(value.getDesc());
    }
}