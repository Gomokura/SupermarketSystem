package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.DeliveryTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeliveryTaskMapper extends BaseMapper<DeliveryTask> {
    @Select("SELECT SEQ_DELIVERY_TASKS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
