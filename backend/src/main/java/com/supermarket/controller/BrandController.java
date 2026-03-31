package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Brand;
import com.supermarket.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /** 品牌列表 GET /brands/list */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) String status) {
        return brandService.listAll(status);
    }

    /** 新增品牌 POST /brands/create */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Brand brand) {
        return brandService.create(brand);
    }

    /** 编辑品牌 PUT /brands/{brandId} */
    @PutMapping("/{brandId}")
    public Result<?> update(@PathVariable Integer brandId, @RequestBody Brand brand) {
        return brandService.update(brandId, brand);
    }

    /** 删除品牌 DELETE /brands/{brandId} */
    @DeleteMapping("/{brandId}")
    public Result<?> delete(@PathVariable Integer brandId) {
        return brandService.delete(brandId);
    }
}
