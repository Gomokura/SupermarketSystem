package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.DamageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DamageRecordMapper extends BaseMapper<DamageRecord> {
    @Select("SELECT SEQ_DAMAGE_RECORDS.NEXTVAL FROM DUAL")
    Integer getNextId();
}
