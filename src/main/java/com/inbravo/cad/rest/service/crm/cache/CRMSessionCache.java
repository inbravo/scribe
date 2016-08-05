/*
 * MIT License
 * 
 * Copyright (c) 2016 Amit Dixit (github.com/inbravo)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.inbravo.cad.rest.service.crm.cache;

import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CRMSessionCache {

  private static final Logger logger = Logger.getLogger(CRMSessionCache.class);

  public static final long DEFAULT_TIME_TO_LIVE = 10 * 60 * 1000;

  public static final long DEFAULT_ACCESS_TIMEOUT = 5 * 60 * 1000;

  public static final long DEFAULT_TIMER_INTERVAL = 2 * 60 * 1000;

  private long ttl = DEFAULT_TIME_TO_LIVE;

  private long ato = DEFAULT_ACCESS_TIMEOUT;

  private long tiv = DEFAULT_TIMER_INTERVAL;

  private int mcq = 100;

  private LRUMap cacheMap;

  private Timer cacheManager;

  protected final void finalize() throws Throwable {

    if (cacheManager != null) {
      cacheManager.cancel();
    }
    super.finalize();
  }

  public CRMSessionCache() {
    cacheMap = new LRUMap(mcq);
    initialize();
  }

  /* All times in milliseconds */
  public CRMSessionCache(final long timeToLive, final long accessTimeout, final long timerInterval, final int maximumCachedQuantity) {
    ttl = timeToLive;
    ato = accessTimeout;
    tiv = timerInterval;
    cacheMap = new LRUMap(maximumCachedQuantity);

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside CRMSessionCache: initialization started Time to live:" + ttl + " & Access timeout:" + ato + " & Cache timer interval: "
          + tiv + " & Maximum allowed cached quantity: " + maximumCachedQuantity);
    }
    initialize();
  }

  public final void setTimeToLive(final long milliSecs) {
    ttl = milliSecs;
    initialize();
  }

  public final void setAccessTimeout(final long milliSecs) {
    ato = milliSecs;
    initialize();
  }

  public final void setCleaningInterval(final long milliSecs) {
    tiv = milliSecs;
    initialize();
  }

  public final void initialize() {
    if (cacheManager != null) {
      cacheManager.cancel();
    }
    cacheManager = new Timer(true);
    cacheManager.schedule(new TimerTask() {
      public void run() {
        long now = System.currentTimeMillis();
        try {
          MapIterator itr = cacheMap.mapIterator();
          while (itr.hasNext()) {
            Object key = itr.next();
            final CachedObject cobj = (CachedObject) itr.getValue();
            if (cobj == null || cobj.hasExpired(now)) {

              if (logger.isDebugEnabled()) {
                logger.debug("---Inside CRMSessionCache: removing " + key + ": Idle time= " + (now - cobj.timeAccessedLast) + "; Stale time= "
                    + (now - cobj.timeCached) + "; Object count in cache= " + cacheMap.size());
              }
              itr.remove();
              Thread.yield();
            }
          }
        } catch (ConcurrentModificationException cme) {
          /*
           * This is just a timer cleaning up. It will catch up on cleaning next time it runs.
           */
          if (logger.isDebugEnabled()) {
            logger.debug("---Inside CRMSessionCache:Ignorable ConcurrentModificationException");
          }
        }
      }
    }, 0, tiv);
  }

  public final int howManyObjects() {
    return cacheMap.size();
  }

  public final void clear() {
    cacheMap.clear();
  }

  /**
   * If the given key already maps to an existing object and the new object is not equal to the
   * existing object, existing object is overwritten and the existing object is returned; otherwise
   * null is returned. You may want to check the return value for null-ness to make sure you are not
   * overwriting a previously cached object. May be you can use a different key for your object if
   * you do not intend to overwrite.
   */
  public final Object admit(final Object key, final Object dataToCache) {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside CRMSessionCache: admiting object with key= " + key + "; existing object count in cache= " + cacheMap.size());
    }

    /* Get object from cache */
    final CachedObject cobj = (CachedObject) cacheMap.get(key);

    /* If object is not found in cache */
    if (cobj == null) {
      cacheMap.put(key, new CachedObject(dataToCache));
      return null;
    } else {

      /* Get object from cache */
      final Object obj = cobj.getCachedData(key);

      /* If object is not found in cache */
      if (obj == null) {
        if (dataToCache == null) {

          cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
          return null;
        } else {
          cacheMap.put(key, new CachedObject(dataToCache));
          return null;
        }

      } else if (obj.equals(dataToCache)) {

        /* Avoids creating unnecessary new cachedObject */
        cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
        return null;
      } else {
        cacheMap.put(key, new CachedObject(dataToCache));
        return obj;
      }
    }
  }

  public final Object admit(final Object key, final Object dataToCache, final long objectTimeToLive, final long objectIdleTimeout) {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside CRMSessionCache: admiting object with key= " + key + "; existing object count in cache= " + cacheMap.size());
    }

    /* Get object from cache */
    final CachedObject cobj = (CachedObject) cacheMap.get(key);

    if (cobj == null) {

      /* Put object in cache */
      cacheMap.put(key, new CachedObject(dataToCache, objectTimeToLive, objectIdleTimeout));
      return null;
    } else {
      Object obj = cobj.getCachedData(key);
      if (obj == null) {
        if (dataToCache == null) {

          cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
          cobj.objectTTL = objectTimeToLive;
          cobj.objectIdleTimeout = objectIdleTimeout;
          cobj.userTimeouts = true;
          return null;
        } else {
          cacheMap.put(key, new CachedObject(dataToCache, objectTimeToLive, objectIdleTimeout));
          return null;
        }
      } else if (obj.equals(dataToCache)) {
        cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
        cobj.objectTTL = objectTimeToLive;
        cobj.objectIdleTimeout = objectIdleTimeout;
        cobj.userTimeouts = true;
        return null;
      } else {
        cacheMap.put(key, new CachedObject(dataToCache, objectTimeToLive, objectIdleTimeout));
        return obj;
      }
    }
  }

  public final Object recover(final Object key) {

    final CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) {
      return null;
    } else {
      return cobj.getCachedData(key);
    }
  }

  public final void discard(final Object key) {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside CRMSessionCache.discard removing: " + key + "; existing object count in cache= " + cacheMap.size());
    }
    cacheMap.remove(key);
  }

  public final long whenCached(final Object key) {
    final CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) {
      return 0;
    }
    return cobj.timeCached;
  }

  public final long whenLastAccessed(final Object key) {
    CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) {
      return 0;
    }
    return cobj.timeAccessedLast;
  }

  public final int howManyTimesAccessed(final Object key) {
    CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) {
      return 0;
    }
    return cobj.numberOfAccesses;
  }

  public final void setCacheMap(final LRUMap cacheMap) {
    this.cacheMap = cacheMap;
  }

  public final LRUMap getCacheMap() {
    return cacheMap;
  }

  /**
   * A cached object, needed to store attributes such as the last time it was accessed.
   */
  protected final class CachedObject {
    private Object cachedData;

    private long timeCached;

    private long timeAccessedLast;

    private int numberOfAccesses;

    private long objectTTL;

    private long objectIdleTimeout;

    private boolean userTimeouts;

    CachedObject(final Object cachedData) {
      long now = System.currentTimeMillis();
      this.cachedData = cachedData;
      timeCached = now;
      timeAccessedLast = now;
      ++numberOfAccesses;
    }

    CachedObject(final Object cachedData, final long timeToLive, final long idleTimeout) {
      long now = System.currentTimeMillis();
      this.cachedData = cachedData;
      objectTTL = timeToLive;
      objectIdleTimeout = idleTimeout;
      userTimeouts = true;
      timeCached = now;
      timeAccessedLast = now;
      ++numberOfAccesses;
    }

    final Object getCachedData(final Object key) {
      long now = System.currentTimeMillis();
      if (hasExpired(now)) {
        cachedData = null;
        cacheMap.remove(key);
        return null;
      }
      timeAccessedLast = now;
      ++numberOfAccesses;
      return cachedData;
    }

    final boolean hasExpired(final long now) {
      long usedTTL = userTimeouts ? objectTTL : ttl;
      long usedATO = userTimeouts ? objectIdleTimeout : ato;
      if (now > timeAccessedLast + usedATO || now > timeCached + usedTTL) {
        return true;
      } else {
        return false;
      }
    }
  }

  public final long getTtl() {
    return ttl;
  }

  public final void setTtl(final long ttl) {
    this.ttl = ttl;
  }

  public final long getAto() {
    return ato;
  }

  public final void setAto(final long ato) {
    this.ato = ato;
  }

  public final long getTiv() {
    return tiv;
  }

  public final void setTiv(final long tiv) {
    this.tiv = tiv;
  }

  public final int getMcq() {
    return mcq;
  }

  public final void setMcq(final int mcq) {
    this.mcq = mcq;
  }
}
