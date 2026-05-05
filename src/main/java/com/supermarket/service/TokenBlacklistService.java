package com.supermarket.service;

/**
 * TokenBlacklistService - JWT token blacklist management (HTTP Session 模式下不直接使用)
 * 仅保留用于潜在的向后兼容性
 */
public class TokenBlacklistService {
    public boolean isBlacklisted(String token) { return false; }
    public void blacklistToken(String token, Integer userId, String userType) {}
    public void blacklistUserTokens(Integer userId, String userType) {}
    public void removeFromBlacklist(String token) {}
}
