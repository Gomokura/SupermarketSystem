package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
    @Select("SELECT SEQ_PURCHASE_ORDERS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
