package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Address;
import com.supermarket.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService extends ServiceImpl<AddressMapper, Address> {

    @Autowired
    private AddressMapper addressMapper;

    public Result<?> getUserAddresses(Integer userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.orderByDesc(Address::getIsDefault);
        List<Address> list = this.list(wrapper);
        return Result.success(list);
    }

    public Result<?> addAddress(Address address) {
        long cnt = this.count(new LambdaQueryWrapper<Address>().eq(Address::getUserId, address.getUserId()));
        if (cnt >= 10) {
            return Result.error("收货地址最多10条");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, address.getUserId());
            List<Address> list = this.list(wrapper);
            for (Address a : list) {
                a.setIsDefault(0);
                this.updateById(a);
            }
        }
        address.setAddressId(addressMapper.getNextId());
        this.save(address);
        return Result.success();
    }

    public Result<?> updateAddress(Address address) {
        if (address == null || address.getAddressId() == null) {
            return Result.error("addressId不能为空");
        }
        Address existing = this.getById(address.getAddressId());
        if (existing == null) return Result.error("地址不存在");
        if (existing.getUserId() == null || !existing.getUserId().equals(address.getUserId())) {
            return Result.error("无权修改该地址");
        }

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, address.getUserId());
            List<Address> list = this.list(wrapper);
            for (Address a : list) {
                if (!a.getAddressId().equals(address.getAddressId())) {
                    a.setIsDefault(0);
                    this.updateById(a);
                }
            }
        }
        this.updateById(address);
        return Result.success();
    }

    public Result<?> deleteAddress(Integer addressId, Integer userId) {
        Address existing = this.getById(addressId);
        if (existing == null) return Result.error("地址不存在");
        if (existing.getUserId() == null || !existing.getUserId().equals(userId)) {
            return Result.error("无权删除该地址");
        }
        if (existing.getIsDefault() != null && existing.getIsDefault() == 1) {
            return Result.error("默认地址不能删除，请先设置其他地址为默认");
        }
        this.removeById(addressId);
        return Result.success();
    }
}
