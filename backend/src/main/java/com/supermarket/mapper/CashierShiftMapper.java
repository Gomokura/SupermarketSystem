package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.CashierShift;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CashierShiftMapper extends BaseMapper<CashierShift> {
    @Select("SELECT SEQ_CASHIER_SHIFTS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
