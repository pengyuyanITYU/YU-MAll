package com.yu.common.convertor;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * 支付类型转换器
 * 1 -> 支付宝
 * 2 -> 微信
 * 3 -> 余额
 */
public class PaymentTypeConverter implements Converter<Integer> {

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
                desc = "支付宝";
                break;
            case 2:
                desc = "微信";
                break;
            case 3:
                desc = "扣减余额";
                break;
            default:
                desc = "未知(" + value + ")";
        }
        return new WriteCellData<>(desc);
    }
}