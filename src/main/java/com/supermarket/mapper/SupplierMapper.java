package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {
    @Select("SELECT SEQ_SUPPLIERS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
