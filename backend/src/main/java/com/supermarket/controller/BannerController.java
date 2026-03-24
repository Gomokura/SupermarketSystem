package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Banner;
import com.supermarket.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /** C端：获取有效轮播图（无需登录） */
    @GetMapping("/list")
    public Result<?> getActiveBanners() {
        return bannerService.getActiveBanners();
    }

    /** B端：获取全部轮播图 */
    @GetMapping("/admin/list")
    public Result<?> getAllBanners() {
        return bannerService.getAllBanners();
    }

    @PostMapping("/admin")
    public Result<?> addBanner(@RequestBody Banner banner) {
        return bannerService.addBanner(banner);
    }

    @PutMapping("/admin/{bannerId}")
    public Result<?> updateBanner(@PathVariable Integer bannerId, @RequestBody Banner banner) {
        banner.setBannerId(bannerId);
        return bannerService.updateBanner(banner);
    }

    @DeleteMapping("/admin/{bannerId}")
    public Result<?> deleteBanner(@PathVariable Integer bannerId) {
        return bannerService.deleteBanner(bannerId);
    }

    @PutMapping("/admin/{bannerId}/toggle")
    public Result<?> toggleBanner(
            @PathVariable Integer bannerId,
            @RequestParam Integer isActive) {
        return bannerService.toggleBanner(bannerId, isActive);
    }
}
