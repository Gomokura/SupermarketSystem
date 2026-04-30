package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Brand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BrandMapper extends BaseMapper<Brand> {
    @Select("SELECT SEQ_BRANDS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
