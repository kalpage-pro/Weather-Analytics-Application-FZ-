package com.Fidenz.Weather.Analytics.Application.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;


@RestController
public class CacheDebugController {

    private final CacheManager cacheManager;

    public CacheDebugController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/api/debug/cache-status")
    public Map<String, Object> cacheStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String cacheName : cacheManager.getCacheNames()) {
            CaffeineCache springCache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (springCache == null) continue;

            Cache<Object, Object> nativeCache = springCache.getNativeCache();
            CacheStats stats = nativeCache.stats();

            Map<String, Object> cacheInfo = new LinkedHashMap<>();
            cacheInfo.put("hitCount", stats.hitCount());
            cacheInfo.put("missCount", stats.missCount());
            cacheInfo.put("hitRate", Math.round(stats.hitRate() * 1000.0) / 1000.0);
            cacheInfo.put("currentSize", nativeCache.estimatedSize());
            cacheInfo.put("cachedKeys", nativeCache.asMap().keySet());

            result.put(cacheName, cacheInfo);
        }
        return result;
    }
}
