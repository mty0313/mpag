package top.diff.mpag.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class GuavaCache {
  private static final Cache<String, Object> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(24, TimeUnit.HOURS)
      .maximumSize(100)
      .build();

  public static void put(String key, Object value) {
    cache.put(key, value);
  }

  public static Object get(String key) {
    return cache.getIfPresent(key);
  }

  public static String getString(String key) {
    Object cached = cache.getIfPresent(key);
    if (null != cached) {
      return cached.toString();
    }
    return null;
  }

  public static Long getLong(String key) {
    Object cached = cache.getIfPresent(key);
    if (null != cached) {
      return Long.parseLong(cached.toString());
    }
    return null;
  }

  public static void remove(String key) {
    cache.invalidate(key);
  }

  public static void clear() {
    cache.invalidateAll();
  }
}

