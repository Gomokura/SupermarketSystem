package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.CashierRecordItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CashierRecordItemMapper extends BaseMapper<CashierRecordItem> {
    @Select("SELECT SEQ_CASHIER_RECORD_ITEMS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
