package com.yu.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.admin.domain.po.Administrator;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdministratorMapper extends BaseMapper<Administrator> {
}
