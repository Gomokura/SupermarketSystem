package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
    @Select("SELECT SEQ_PRODUCT_SKUS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
