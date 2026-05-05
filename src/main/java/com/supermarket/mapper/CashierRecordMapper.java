package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.CashierRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CashierRecordMapper extends BaseMapper<CashierRecord> {
    @Select("SELECT SEQ_CASHIER_RECORDS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
