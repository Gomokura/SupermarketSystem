package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
    @Select("SELECT SEQ_ORDER_STATUS_LOGS.NEXTVAL FROM DUAL")
    Integer getNextId();
}

