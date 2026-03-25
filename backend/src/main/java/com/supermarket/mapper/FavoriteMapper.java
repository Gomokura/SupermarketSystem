package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT seq_favorites.NEXTVAL FROM dual")
    Integer nextId();
}

