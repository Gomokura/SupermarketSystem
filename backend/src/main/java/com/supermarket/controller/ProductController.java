package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Product;
import com.supermarket.entity.Category;
import com.supermarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<?> getProductList(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return productService.getProductList(categoryId, keyword, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Result<?> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public Result<?> addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    @PutMapping
    public Result<?> updateProduct(@RequestBody Product product) {
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Integer id) {
        return productService.deleteProduct(id);
    }

    @GetMapping("/categories")
    public Result<?> getCategories() {
        return productService.getCategories();
    }

    @PostMapping("/categories")
    public Result<?> addCategory(@RequestBody Category category) {
        return productService.addCategory(category);
    }

    @PutMapping("/categories")
    public Result<?> updateCategory(@RequestBody Category category) {
        return productService.updateCategory(category);
    }

    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Integer id) {
        return productService.deleteCategory(id);
    }
}
