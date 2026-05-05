package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.PointsLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PointsLogMapper extends BaseMapper<PointsLog> {
    @Select("SELECT SEQ_POINTS_LOGS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
