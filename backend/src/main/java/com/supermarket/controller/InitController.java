package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Timestamp;

@RestController
@RequestMapping("/init")
public class InitController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private CourierMapper courierMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private CouponMapper couponMapper;

    @PostMapping("/test-data")
    public Result<?> initTestData() {
        try {
            // 1. 插入测试用户
            insertUsers();

            // 2. 插入管理员
            insertAdmins();

            // 3. 插入配送员
            insertCouriers();

            // 4. 插入分类
            insertCategories();

            // 5. 插入品牌
            insertBrands();

            // 6. 插入商品
            insertProducts();

            // 7. 插入Banner
            insertBanners();

            // 8. 插入优惠券
            insertCoupons();

            return Result.success("测试数据初始化完成！");
        } catch (Exception e) {
            return Result.error("初始化失败: " + e.getMessage());
        }
    }

    @PostMapping("/admins")
    public Result<?> initAdmins() {
        try {
            insertAdmins();
            return Result.success("管理员数据初始化完成！");
        } catch (Exception e) {
            return Result.error("初始化失败: " + e.getMessage());
        }
    }

    private void insertUsers() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        User user1 = new User();
        user1.setUserId(1001);
        user1.setUsername("13800138001");
        user1.setPassword("6ad14ba9986e3615423dfca256d04e3f"); // user123
        user1.setNickname("银卡会员");
        user1.setRealName("张三");
        user1.setPhone("13800138001");
        user1.setMemberLevel("SILVER");
        user1.setPoints(520);
        user1.setStatus("active");
        user1.setCreateTime(now);
        userMapper.insert(user1);

        User user2 = new User();
        user2.setUserId(1002);
        user2.setUsername("13800138002");
        user2.setPassword("6ad14ba9986e3615423dfca256d04e3f");
        user2.setNickname("金卡会员");
        user2.setRealName("李四");
        user2.setPhone("13800138002");
        user2.setMemberLevel("GOLD");
        user2.setPoints(1200);
        user2.setStatus("active");
        user2.setCreateTime(now);
        userMapper.insert(user2);

        User user3 = new User();
        user3.setUserId(1003);
        user3.setUsername("13800138003");
        user3.setPassword("6ad14ba9986e3615423dfca256d04e3f");
        user3.setNickname("普通会员");
        user3.setRealName("王五");
        user3.setPhone("13800138003");
        user3.setMemberLevel("NORMAL");
        user3.setPoints(0);
        user3.setStatus("active");
        user3.setCreateTime(now);
        userMapper.insert(user3);
    }

    private void insertAdmins() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Admin admin1 = new Admin();
        admin1.setAdminId(1);
        admin1.setUsername("admin");
        admin1.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        admin1.setRealName("超级管理员");
        admin1.setPhone("13800000001");
        admin1.setRole("SUPER_ADMIN");
        admin1.setStatus("active");
        adminMapper.insert(admin1);

        Admin admin2 = new Admin();
        admin2.setAdminId(2);
        admin2.setUsername("manager");
        admin2.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        admin2.setRealName("店长");
        admin2.setPhone("13800000002");
        admin2.setRole("MANAGER");
        admin2.setStatus("active");
        adminMapper.insert(admin2);

        Admin admin3 = new Admin();
        admin3.setAdminId(3);
        admin3.setUsername("cashier01");
        admin3.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        admin3.setRealName("收银员小张");
        admin3.setPhone("13800000003");
        admin3.setRole("CASHIER");
        admin3.setStatus("active");
        adminMapper.insert(admin3);

        Admin admin4 = new Admin();
        admin4.setAdminId(4);
        admin4.setUsername("warehouse01");
        admin4.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        admin4.setRealName("仓管老王");
        admin4.setPhone("13800000004");
        admin4.setRole("WAREHOUSE");
        admin4.setStatus("active");
        adminMapper.insert(admin4);

        Admin admin5 = new Admin();
        admin5.setAdminId(5);
        admin5.setUsername("product01");
        admin5.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        admin5.setRealName("商品专员小李");
        admin5.setPhone("13800000005");
        admin5.setRole("PRODUCT");
        admin5.setStatus("active");
        adminMapper.insert(admin5);

        Admin admin6 = new Admin();
        admin6.setAdminId(6);
        admin6.setUsername("service01");
        admin6.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        admin6.setRealName("客服小美");
        admin6.setPhone("13800000006");
        admin6.setRole("SERVICE");
        admin6.setStatus("active");
        adminMapper.insert(admin6);
    }

    private void insertCouriers() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Courier c1 = new Courier();
        c1.setCourierId(1);
        c1.setPhone("13900000001");
        c1.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        c1.setCourierName("张配送");
        c1.setStatus("active");
        c1.setCreateTime(now);
        courierMapper.insert(c1);

        Courier c2 = new Courier();
        c2.setCourierId(2);
        c2.setPhone("13900000002");
        c2.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        c2.setCourierName("李配送");
        c2.setStatus("active");
        c2.setCreateTime(now);
        courierMapper.insert(c2);

        Courier c3 = new Courier();
        c3.setCourierId(3);
        c3.setPhone("13900000003");
        c3.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq"); // BCrypt: 123456
        c3.setCourierName("王配送");
        c3.setStatus("active");
        c3.setCreateTime(now);
        courierMapper.insert(c3);
    }

    private void insertCategories() {
        Category c1 = new Category();
        c1.setCategoryId(1);
        c1.setParentId(0);
        c1.setCategoryName("生鲜食品");
        c1.setSortOrder(1);
        categoryMapper.insert(c1);

        Category c2 = new Category();
        c2.setCategoryId(2);
        c2.setParentId(0);
        c2.setCategoryName("日用百货");
        c2.setSortOrder(2);
        categoryMapper.insert(c2);

        Category c3 = new Category();
        c3.setCategoryId(3);
        c3.setParentId(0);
        c3.setCategoryName("饮料零食");
        c3.setSortOrder(3);
        categoryMapper.insert(c3);

        Category c4 = new Category();
        c4.setCategoryId(4);
        c4.setParentId(0);
        c4.setCategoryName("粮油调味");
        c4.setSortOrder(4);
        categoryMapper.insert(c4);

        Category c5 = new Category();
        c5.setCategoryId(5);
        c5.setParentId(1);
        c5.setCategoryName("新鲜水果");
        c5.setSortOrder(1);
        categoryMapper.insert(c5);

        Category c6 = new Category();
        c6.setCategoryId(6);
        c6.setParentId(1);
        c6.setCategoryName("新鲜蔬菜");
        c6.setSortOrder(2);
        categoryMapper.insert(c6);
    }

    private void insertBrands() {
        Brand b1 = new Brand();
        b1.setBrandId(1);
        b1.setBrandName("可口可乐");
        b1.setStatus("active");
        brandMapper.insert(b1);

        Brand b2 = new Brand();
        b2.setBrandId(2);
        b2.setBrandName("农夫山泉");
        b2.setStatus("active");
        brandMapper.insert(b2);

        Brand b3 = new Brand();
        b3.setBrandId(3);
        b3.setBrandName("蒙牛");
        b3.setStatus("active");
        brandMapper.insert(b3);

        Brand b4 = new Brand();
        b4.setBrandId(4);
        b4.setBrandName("伊利");
        b4.setStatus("active");
        brandMapper.insert(b4);

        Brand b5 = new Brand();
        b5.setBrandId(5);
        b5.setBrandName("乐事");
        b5.setStatus("active");
        brandMapper.insert(b5);
    }

    private void insertProducts() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Product p1 = new Product();
        p1.setProductId(1);
        p1.setCategoryId(5);
        p1.setBrandId(3);
        p1.setProductName("蒙牛纯牛奶");
        p1.setBarcode("6907878100013");
        p1.setUnit("箱");
        p1.setPrice(59.90);
        p1.setOriginalPrice(69.90);
        p1.setCostPrice(45.00);
        p1.setStock(100);
        p1.setStockWarning(10);
        p1.setStatus("active");
        p1.setIsRecommend(1);
        p1.setCreateTime(now);
        productMapper.insert(p1);

        Product p2 = new Product();
        p2.setProductId(2);
        p2.setCategoryId(5);
        p2.setBrandId(4);
        p2.setProductName("伊利纯牛奶");
        p2.setBarcode("6907878100020");
        p2.setUnit("箱");
        p2.setPrice(58.90);
        p2.setOriginalPrice(68.90);
        p2.setCostPrice(44.00);
        p2.setStock(80);
        p2.setStockWarning(10);
        p2.setStatus("active");
        p2.setIsRecommend(1);
        p2.setCreateTime(now);
        productMapper.insert(p2);

        Product p3 = new Product();
        p3.setProductId(3);
        p3.setCategoryId(3);
        p3.setBrandId(1);
        p3.setProductName("可口可乐500ml");
        p3.setBarcode("6902083888001");
        p3.setUnit("瓶");
        p3.setPrice(3.50);
        p3.setOriginalPrice(4.00);
        p3.setCostPrice(2.50);
        p3.setStock(500);
        p3.setStockWarning(50);
        p3.setStatus("active");
        p3.setIsRecommend(1);
        p3.setCreateTime(now);
        productMapper.insert(p3);

        Product p4 = new Product();
        p4.setProductId(4);
        p4.setCategoryId(3);
        p4.setBrandId(2);
        p4.setProductName("农夫山泉550ml");
        p4.setBarcode("6921168500013");
        p4.setUnit("瓶");
        p4.setPrice(2.00);
        p4.setOriginalPrice(2.50);
        p4.setCostPrice(1.20);
        p4.setStock(800);
        p4.setStockWarning(100);
        p4.setStatus("active");
        p4.setIsRecommend(1);
        p4.setCreateTime(now);
        productMapper.insert(p4);

        Product p5 = new Product();
        p5.setProductId(5);
        p5.setCategoryId(3);
        p5.setBrandId(5);
        p5.setProductName("乐事薯片原味");
        p5.setBarcode("6920152400012");
        p5.setUnit("袋");
        p5.setPrice(8.90);
        p5.setOriginalPrice(10.90);
        p5.setCostPrice(6.50);
        p5.setStock(200);
        p5.setStockWarning(20);
        p5.setStatus("active");
        p5.setIsRecommend(1);
        p5.setCreateTime(now);
        productMapper.insert(p5);

        Product p6 = new Product();
        p6.setProductId(6);
        p6.setCategoryId(6);
        p6.setBrandId(0);
        p6.setProductName("新鲜西红柿");
        p6.setBarcode("XS001");
        p6.setUnit("斤");
        p6.setPrice(3.99);
        p6.setOriginalPrice(4.99);
        p6.setCostPrice(2.50);
        p6.setStock(200);
        p6.setStockWarning(30);
        p6.setStatus("active");
        p6.setIsRecommend(0);
        p6.setCreateTime(now);
        productMapper.insert(p6);

        Product p7 = new Product();
        p7.setProductId(7);
        p7.setCategoryId(6);
        p7.setBrandId(0);
        p7.setProductName("新鲜黄瓜");
        p7.setBarcode("XS002");
        p7.setUnit("斤");
        p7.setPrice(2.99);
        p7.setOriginalPrice(3.99);
        p7.setCostPrice(1.80);
        p7.setStock(150);
        p7.setStockWarning(20);
        p7.setStatus("active");
        p7.setIsRecommend(0);
        p7.setCreateTime(now);
        productMapper.insert(p7);

        Product p8 = new Product();
        p8.setProductId(8);
        p8.setCategoryId(5);
        p8.setBrandId(0);
        p8.setProductName("红富士苹果");
        p8.setBarcode("XS003");
        p8.setUnit("斤");
        p8.setPrice(5.99);
        p8.setOriginalPrice(6.99);
        p8.setCostPrice(3.50);
        p8.setStock(300);
        p8.setStockWarning(50);
        p8.setStatus("active");
        p8.setIsRecommend(1);
        p8.setCreateTime(now);
        productMapper.insert(p8);
    }

    private void insertBanners() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Banner b1 = new Banner();
        b1.setBannerId(1);
        b1.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=supermarket%20promotion%20banner%20fresh%20groceries%20sale&image_size=landscape_16_9");
        b1.setLinkType("activity");
        b1.setLinkTarget("");
        b1.setSortOrder(1);
        b1.setStatus("active");
        bannerMapper.insert(b1);

        Banner b2 = new Banner();
        b2.setBannerId(2);
        b2.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=summer%20sale%20supermarket%20fresh%20fruits&image_size=landscape_16_9");
        b2.setLinkType("category");
        b2.setLinkTarget("3");
        b2.setSortOrder(2);
        b2.setStatus("active");
        bannerMapper.insert(b2);
    }

    private void insertCoupons() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Coupon c1 = new Coupon();
        c1.setCouponId(1);
        c1.setCouponType("new_user");
        c1.setCouponName("新人专享券");
        c1.setDescription("新用户注册即送");
        c1.setMinAmount(0.0);
        c1.setFaceValue(10.00);
        c1.setTotalCount(1000);
        c1.setIssuedCount(0);
        c1.setPerLimit(1);
        c1.setStatus("active");
        c1.setStartTime(now);
        c1.setEndTime(new Timestamp(now.getTime() + 90L * 24 * 60 * 60 * 1000));
        c1.setCreateTime(now);
        couponMapper.insert(c1);

        Coupon c2 = new Coupon();
        c2.setCouponId(2);
        c2.setCouponType("full_reduce");
        c2.setCouponName("满100减15");
        c2.setDescription("全场通用");
        c2.setMinAmount(100.00);
        c2.setFaceValue(15.00);
        c2.setTotalCount(500);
        c2.setIssuedCount(0);
        c2.setPerLimit(3);
        c2.setStatus("active");
        c2.setStartTime(now);
        c2.setEndTime(new Timestamp(now.getTime() + 30L * 24 * 60 * 60 * 1000));
        c2.setCreateTime(now);
        couponMapper.insert(c2);

        Coupon c3 = new Coupon();
        c3.setCouponId(3);
        c3.setCouponType("discount");
        c3.setCouponName("8折优惠券");
        c3.setDescription("生鲜食品专用");
        c3.setMinAmount(50.00);
        c3.setFaceValue(0.8);
        c3.setTotalCount(300);
        c3.setIssuedCount(0);
        c3.setPerLimit(2);
        c3.setStatus("active");
        c3.setStartTime(now);
        c3.setEndTime(new Timestamp(now.getTime() + 15L * 24 * 60 * 60 * 1000));
        c3.setCreateTime(now);
        couponMapper.insert(c3);
    }
}