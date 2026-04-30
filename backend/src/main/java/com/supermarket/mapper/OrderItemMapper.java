package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    @Select("SELECT SEQ_ORDER_ITEMS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
