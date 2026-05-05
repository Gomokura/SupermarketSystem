package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.TokenBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TokenBlacklistMapper extends BaseMapper<TokenBlacklist> {
    @Select("SELECT COUNT(1) FROM TOKEN_BLACKLIST WHERE TOKEN = #{token}")
    int isBlacklistedCount(String token);
}