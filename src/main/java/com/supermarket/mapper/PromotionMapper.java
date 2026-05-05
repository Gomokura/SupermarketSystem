package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Promotion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PromotionMapper extends BaseMapper<Promotion> {
    @Select("SELECT SEQ_ACTIVITIES.NEXTVAL FROM DUAL")
    Integer getNextId();
}
