package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BannerMapper extends BaseMapper<Banner> {
    @Select("SELECT SEQ_BANNERS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
