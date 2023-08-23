package com.shardingsphere.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shardingsphere.entity.UserMP;
import org.apache.calcite.adapter.java.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserMP> {
}
