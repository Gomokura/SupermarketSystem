package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Address;
import com.supermarket.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/list")
    public Result<?> getUserAddresses(@RequestAttribute Integer userId) {
        return addressService.getUserAddresses(userId);
    }

    @PostMapping
    public Result<?> addAddress(
            @RequestAttribute Integer userId,
            @RequestBody Address address) {
        address.setUserId(userId);
        return addressService.addAddress(address);
    }

    @PutMapping
    public Result<?> updateAddress(
            @RequestAttribute Integer userId,
            @RequestBody Address address) {
        address.setUserId(userId);
        return addressService.updateAddress(address);
    }

    @DeleteMapping("/{addressId}")
    public Result<?> deleteAddress(
            @RequestAttribute Integer userId,
            @PathVariable Integer addressId) {
        return addressService.deleteAddress(addressId, userId);
    }
}
