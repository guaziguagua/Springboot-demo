package com.shardingsphere.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类
 *
 * @author Jiahai
 */
@TableName("t_order")
@Data
public class Order implements Serializable {
//    @TableId(type = IdType.ASSIGN_ID) // IdType.AUTO 依赖数据库的主键自增策略，无法做到水平分片    ASSIGN_ID是mybatisplus的一个策略，和sharding sphere没有关系
    //UUID是无序的，不适合作为主键，SNOWFLAKE适合。
    //分布式可以设置mysql的初始值和步长
    //@TableId(type = IdType.ASSIGN_ID) 如果配置了这个，就默认mybatisp的分布式id，如果不设置，就是默认auto。所以想要使用sharding sphere就要设置@TableId(type = IdType.AUTO)
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
}