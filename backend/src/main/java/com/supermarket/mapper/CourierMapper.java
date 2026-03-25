package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Courier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CourierMapper extends BaseMapper<Courier> {

    /** 配送成功数 +1 */
    @Update("UPDATE couriers SET total_delivered = total_delivered + 1 WHERE courier_id = #{courierId}")
    void incrementDelivered(Long courierId);

    /** 配送失败数 +1 */
    @Update("UPDATE couriers SET total_failed = total_failed + 1 WHERE courier_id = #{courierId}")
    void incrementFailed(Long courierId);
}
