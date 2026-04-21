package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 查客户手机号（供配送员查看用） */
    @Select("SELECT phone FROM users WHERE user_id = #{userId}")
    String selectPhoneByUserId(Long userId);
}
