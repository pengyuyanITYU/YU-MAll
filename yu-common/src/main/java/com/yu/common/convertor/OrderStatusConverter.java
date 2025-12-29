package com.yu.common.convertor;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * 订单状态转换器
 * 1、未付款 2、已付款,未发货 3、已发货,未确认 4、交易成功 5、交易取消 6、已评价
 */
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
                desc = "未付款";
                break;
            case 2:
                desc = "待发货";
                break;
            case 3:
                desc = "已发货";
                break;
            case 4:
                desc = "交易成功";
                break;
            case 5:
                desc = "已取消";
                break;
            case 6:
                desc = "已评价";
                break;
            default:
                desc = String.valueOf(value);
        }
        return new WriteCellData<>(desc);
    }
}