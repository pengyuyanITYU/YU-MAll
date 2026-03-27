package com.yu.common.convertor;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

public class OrderStatusConverter implements Converter<Integer> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }

        String desc;
        switch (value) {
            case 1:
                desc = "UNPAID";
                break;
            case 2:
                desc = "PAID_WAIT_SHIP";
                break;
            case 3:
                desc = "SHIPPED_WAIT_CONFIRM";
                break;
            case 4:
                desc = "DONE";
                break;
            case 5:
                desc = "CANCELLED";
                break;
            case 6:
                desc = "REVIEWED";
                break;
            default:
                desc = String.valueOf(value);
        }
        return new WriteCellData<>(desc);
    }
}