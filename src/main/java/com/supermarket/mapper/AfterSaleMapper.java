package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.AfterSale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AfterSaleMapper extends BaseMapper<AfterSale> {
    @Select("SELECT SEQ_AFTER_SALES.NEXTVAL FROM DUAL")
    Integer getNextId();
}
