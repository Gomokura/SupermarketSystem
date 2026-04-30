package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {
    @Select("SELECT SEQ_INVENTORY_LOGS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
