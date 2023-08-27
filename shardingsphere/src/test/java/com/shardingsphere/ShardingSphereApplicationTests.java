package com.shardingsphere;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shardingsphere.entity.*;
import com.shardingsphere.mapper.OrderItemMapper;
import com.shardingsphere.mapper.OrderMapper;
import com.shardingsphere.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class ShardingSphereApplicationTests {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Test
    public void testInsert(){
        UserMP userMP = new UserMP();
        userMP.setUname("guagauzi");
        userMapper.insert(userMP);
    }
    //开启事务
    @Transactional
    @Test
    public void testTrans(){

        UserMP user = new UserMP();
        user.setUname("铁锤");
        userMapper.insert(user);

        List<UserMP> users = userMapper.selectList(null);
        System.out.println(users);
    }

    /**
     * 读数据测试,查询两次才会实现slave1 和 slave2 的轮询
     */
    @Test
    public void testSelectAll(){
        List<UserMP> users = userMapper.selectList(null);
        List<UserMP> user1 = userMapper.selectList(null);
        List<UserMP> user2 = userMapper.selectList(null);
        users.forEach(System.out::println);
    }

    /**
     * 测试水平分片
     *  Insert statement does not support sharding table routing to multiple data nodes.  这是个报错，就是说你水平分片的插入语句不支持路由到多个数据节点
     *  因为不知道是往哪个数据库的哪个表提交，@TableName("t_order")这只是逻辑表名，实际表找不到
     */
    @Test
    public void testInsertOrder(){

        Order order = new Order();
        order.setOrderNo("ATGUIGU001");
        order.setUserId(1L);
        order.setAmount(new BigDecimal(100));
        orderMapper.insert(order);
    }


    /**
     * 水平分片：分库插入数据测试,
     */
    @Test
    public void testInsertOrderDatabaseStrategy(){

        for (long i = 0; i < 4; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU001");
            order.setUserId(i + 1);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }

    }


    /**
     * 水平分片：分库+分表插入数据测试 userid分库  orderid分表
     */
    @Test
    public void testInsertOrderTableStrategy(){

        for (long i = 1; i < 5; i++) {

            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(1L);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }

        for (long i = 5; i < 9; i++) {

            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(2L);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }
    }



    /**
     * 垂直分片：插入数据测试
     */
    @Transactional
    @Test
    public void testInsertOrderAndUser(){

        UserMP user = new UserMP();
        user.setUname("强哥2");
        userMapper.insert(user);
        Order order = new Order();
        order.setOrderNo("ATGUIGU0012222");
        order.setUserId(user.getId());
        order.setAmount(new BigDecimal(100));
        orderMapper.insert(order);

    }


    /**
     * sharding 水平分片查询所有记录，这种会查询全部的库和全部表
     * Actual SQL: server-order1 ::: SELECT  id,order_no,user_id,amount  FROM t_order0 UNION ALL SELECT  id,order_no,user_id,amount  FROM t_order1
     * Actual SQL: server-order0 ::: SELECT  id,order_no,user_id,amount  FROM t_order0 UNION ALL SELECT  id,order_no,user_id,amount  FROM t_order1
     */
    @Test
    public void testShardingSelectAll(){

        List<Order> orders = orderMapper.selectList(null);
        orders.forEach(System.out::println);
    }
    /**
     * 水平分片：根据user_id查询记录
     * 查询了一个数据源，每个数据源中使用UNION ALL连接两个表，只查询了server_order 1
     */
    @Test
    public void testShardingSelectByUserId(){

        QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("user_id", 1L);
        List<Order> orders = orderMapper.selectList(orderQueryWrapper);
        orders.forEach(System.out::println);
    }


    /**
     * 联合表的插入
     *
     */
    @Test
    public void testInsertOrderAndOrderItem(){

        for (long i = 1; i < 3; i++) {

            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(1L);
            orderMapper.insert(order);

            for (long j = 1; j < 3; j++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo("ATGUIGU" + i);
                orderItem.setUserId(1L);
                orderItem.setPrice(new BigDecimal(10));
                orderItem.setCount(2);
                orderItemMapper.insert(orderItem);
            }
        }

        for (long i = 5; i < 7; i++) {

            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(2L);
            orderMapper.insert(order);

            for (long j = 1; j < 3; j++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo("ATGUIGU" + i);
                orderItem.setUserId(2L);
                orderItem.setPrice(new BigDecimal(1));
                orderItem.setCount(3);
                orderItemMapper.insert(orderItem);
            }
        }

    }


    /**
     * 测试关联表查询
     * 普通的关联表查询是笛卡尔积的查询，假如水平分片到库1的表1表二，库二的表一表二。。例如下面这个求和是从t_order1的order号去t_order_item1查询item，但是t_order1的order，不会出现在t_order_item0
     * 但是默认回去查询
     * server-order0 ::: SELECT o.order_no, SUM(i.price * i.count) AS amount FROM t_order0 o JOIN t_order_item1 i ON o.order_no = i.order_no GROUP BY o.order_no ORDER BY o.order_no ASC
     * server-order0 ::: SELECT o.order_no, SUM(i.price * i.count) AS amount FROM t_order1 o JOIN t_order_item1 i ON o.order_no = i.order_no GROUP BY o.order_no ORDER BY o.order_no ASC
     * 这里需要绑定表规则，去掉多余查询
     */
    @Test
    public void testGetOrderAmount(){

        List<OrderVo> orderAmountList = orderMapper.getOrderAmount();
        orderAmountList.forEach(System.out::println);
    }


}
