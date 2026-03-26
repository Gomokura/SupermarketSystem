package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    /** 采购单列表 GET /purchase/list */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return purchaseService.list(status, supplierId, pageNum, pageSize);
    }

    /** 采购单详情 GET /purchase/{poId} */
    @GetMapping("/{poId}")
    public Result<?> detail(@PathVariable Integer poId) {
        return purchaseService.detail(poId);
    }

    /** 创建采购申请 POST /purchase/create */
    @PostMapping("/create")
    public Result<?> create(
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        Integer supplierId = (Integer) body.get("supplierId");
        String expectedDate = (String) body.get("expectedDate");
        String remark = (String) body.get("remark");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        return purchaseService.create(adminId, supplierId, expectedDate, remark, items);
    }

    /** 审批通过 PUT /purchase/{poId}/approve */
    @PutMapping("/{poId}/approve")
    public Result<?> approve(@PathVariable Integer poId) {
        return purchaseService.approve(poId);
    }

    /** 确认到货入库 PUT /purchase/{poId}/receive */
    @PutMapping("/{poId}/receive")
    public Result<?> receive(
            @PathVariable Integer poId,
            @RequestAttribute Integer adminId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        return purchaseService.receive(poId, adminId, items);
    }

    /** 取消采购单 PUT /purchase/{poId}/cancel */
    @PutMapping("/{poId}/cancel")
    public Result<?> cancel(@PathVariable Integer poId) {
        return purchaseService.cancel(poId);
    }
}
