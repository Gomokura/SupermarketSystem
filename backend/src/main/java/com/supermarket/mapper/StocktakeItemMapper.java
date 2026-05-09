package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.StocktakeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StocktakeItemMapper extends BaseMapper<StocktakeItem> {
    @Select("SELECT SEQ_CHECK_ITEMS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
