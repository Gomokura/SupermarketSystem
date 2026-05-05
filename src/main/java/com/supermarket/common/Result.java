package com.supermarket.common;

/**
 * Compatibility shim: re-export com.supermarket.entity.Result
 * Some copied services still reference com.supermarket.common.Result.
 */
public class Result<T> extends com.supermarket.entity.Result<T> {
    // Inherits everything from entity.Result
}
