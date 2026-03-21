package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Address;
import com.supermarket.mapper.AddressMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService extends ServiceImpl<AddressMapper, Address> {

    public Result<?> getUserAddresses(Integer userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.orderByDesc(Address::getIsDefault);
        List<Address> list = this.list(wrapper);
        return Result.success(list);
    }

    public Result<?> addAddress(Address address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, address.getUserId());
            List<Address> list = this.list(wrapper);
            for (Address a : list) {
                a.setIsDefault(0);
                this.updateById(a);
            }
        }
        this.save(address);
        return Result.success();
    }

    public Result<?> updateAddress(Address address) {
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

    public Result<?> deleteAddress(Integer addressId) {
        this.removeById(addressId);
        return Result.success();
    }
}
