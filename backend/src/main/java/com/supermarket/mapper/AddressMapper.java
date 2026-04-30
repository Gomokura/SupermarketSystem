package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
    @Select("SELECT SEQ_ADDRESSES.NEXTVAL FROM DUAL")
    Integer getNextId();
}
