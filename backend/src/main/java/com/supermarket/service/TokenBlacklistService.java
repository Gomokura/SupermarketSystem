package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.TokenBlacklist;
import com.supermarket.mapper.TokenBlacklistMapper;
import com.supermarket.config.JwtConfig;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenBlacklistService extends ServiceImpl<TokenBlacklistMapper, TokenBlacklist> {

    @Autowired
    private JwtConfig jwtConfig;

    public boolean isBlacklisted(String token) {
        try {
            LambdaQueryWrapper<TokenBlacklist> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TokenBlacklist::getToken, token);
            return baseMapper.selectCount(wrapper) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void blacklistToken(String token, Integer userId, String userType) {
        try {
            Claims claims = jwtConfig.parseToken(token);
            Date expiresAt = claims.getExpiration();

            TokenBlacklist blacklist = new TokenBlacklist();
            blacklist.setToken(token);
            blacklist.setUserId(userId);
            blacklist.setUserType(userType);
            blacklist.setBlacklistedAt(new Date());
            blacklist.setExpiresAt(expiresAt);

            baseMapper.insert(blacklist);
        } catch (Exception e) {
        }
    }

    public void blacklistUserTokens(Integer userId, String userType) {
        LambdaQueryWrapper<TokenBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TokenBlacklist::getUserId, userId)
               .eq(TokenBlacklist::getUserType, userType);
        baseMapper.delete(wrapper);
    }

    public void removeFromBlacklist(String token) {
        baseMapper.deleteById(token);
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        LambdaQueryWrapper<TokenBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(TokenBlacklist::getExpiresAt, new Date());
        baseMapper.delete(wrapper);
    }
}