package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Courier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CourierMapper extends BaseMapper<Courier> {

    /** 配送成功数 +1 */
    @Update("UPDATE DELIVERY_PERSONS SET TOTAL_DELIVERY_COUNT = TOTAL_DELIVERY_COUNT + 1 WHERE COURIER_ID = #{courierId}")
    void incrementDelivered(Long courierId);

    @Select("SELECT SEQ_DELIVERY_PERSONS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
