package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.SeckillActivityModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeckillActivityModelMapper extends BaseMapper<SeckillActivityModel> {

    @Select("SELECT seq_activities.NEXTVAL FROM dual")
    Integer nextActivityId();
}

