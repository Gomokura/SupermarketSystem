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
    public Result<?> addAddress(@RequestBody Address address) {
        return addressService.addAddress(address);
    }

    @PutMapping
    public Result<?> updateAddress(@RequestBody Address address) {
        return addressService.updateAddress(address);
    }

    @DeleteMapping("/{addressId}")
    public Result<?> deleteAddress(@PathVariable Integer addressId) {
        return addressService.deleteAddress(addressId);
    }
}
