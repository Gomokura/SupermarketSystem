package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.SeckillActivityProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeckillActivityProductModelMapper extends BaseMapper<SeckillActivityProductModel> {

    @Select("SELECT seq_activity_products.NEXTVAL FROM dual")
    Integer nextProductRowId();
}

