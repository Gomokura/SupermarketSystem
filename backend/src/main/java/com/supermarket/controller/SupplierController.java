package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Supplier;
import com.supermarket.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /** 供应商列表 GET /suppliers/list */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) String status) {
        return supplierService.listAll(status);
    }

    /** 供应商详情 GET /suppliers/{supplierId} */
    @GetMapping("/{supplierId}")
    public Result<?> detail(@PathVariable Integer supplierId) {
        return supplierService.detail(supplierId);
    }

    /** 新增供应商 POST /suppliers/create */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Supplier supplier) {
        return supplierService.create(supplier);
    }

    /** 编辑供应商 PUT /suppliers/{supplierId} */
    @PutMapping("/{supplierId}")
    public Result<?> update(@PathVariable Integer supplierId, @RequestBody Supplier supplier) {
        return supplierService.update(supplierId, supplier);
    }

    /** 删除供应商 DELETE /suppliers/{supplierId} */
    @DeleteMapping("/{supplierId}")
    public Result<?> delete(@PathVariable Integer supplierId) {
        return supplierService.delete(supplierId);
    }
}
