package com.shardingsphere;

import com.shardingsphere.entity.Order;
import com.shardingsphere.entity.User;
import com.shardingsphere.entity.UserMP;
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
     * 插入 t_order 数据验证简单的分库分表示例
     */

    /**
     * t_user 未做分库分表的表，插入数据验证示例
     */
    @Test
    void saveDefaultUserSharding() {
        User user = new User();
        user.setUName("公众号：11111");
        user.setUAge(12);
        user.setUAddress("宇宙商场");
        user.setCreateTime(new Date());
        user.setDateTime(new Date());
//        userRepository.save(user);
    }

}
