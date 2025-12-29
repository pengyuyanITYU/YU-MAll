package com.yu.common.utils;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class ExcelUtils {

    /**
     * 读取 Excel (EasyExcel)
     *
     * @param is    输入流
     * @param clazz 实体类 Class
     * @param <T>   泛型
     * @return 数据集合
     */
    public static <T> List<T> importEasyExcel(InputStream is, Class<T> clazz) {
        try {
            return EasyExcel.read(is).head(clazz).sheet().doReadSync();
        } catch (Exception e) {
            log.error("导入Excel异常", e);
            throw new RuntimeException("导入Excel失败");
        }
    }

    /**
     * 导出 Excel (EasyExcel)
     *
     * @param response  响应
     * @param list      数据列表
     * @param clazz     实体类 Class (用于获取表头注解)
     * @param sheetName sheet名称
     * @param <T>       泛型
     */
    public static <T> void exportEasyExcel(HttpServletResponse response, List<T> list, Class<T> clazz, String sheetName) {
        try {
            // 1. 设置响应头，告诉浏览器下载的是 Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 3. 写入数据
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .doWrite(list);
        } catch (IOException e) {
            log.error("导出Excel异常", e);
            throw new RuntimeException("导出Excel失败");
        }
    }
}