package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.StocktakeTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StocktakeTaskMapper extends BaseMapper<StocktakeTask> {
    @Select("SELECT SEQ_STOCKTAKE_TASKS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
