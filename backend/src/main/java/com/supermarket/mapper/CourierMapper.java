package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Courier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CourierMapper extends BaseMapper<Courier> {

    /** 配送成功数 +1 */
    @Update("UPDATE delivery_persons SET total_delivery_count = total_delivery_count + 1 WHERE courier_id = #{courierId}")
    void incrementDelivered(Long courierId);
}
