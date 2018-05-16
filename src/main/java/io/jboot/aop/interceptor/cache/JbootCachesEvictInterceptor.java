/**
 * Copyright (c) 2015-2018, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.aop.interceptor.cache;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.log.Log;
import io.jboot.core.cache.annotation.CacheEvict;
import io.jboot.core.cache.annotation.CachesEvict;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 清除缓存操作的拦截器
 */
public class JbootCachesEvictInterceptor implements Interceptor {
    private static final Log LOG = Log.getLog(JbootCachesEvictInterceptor.class);

    private void doCachesEvict(Object[] arguments, Class targetClass, Method method, List<CacheEvict> cacheEvicts) {
        for (CacheEvict evict : cacheEvicts) {
            try {
                Kits.doCacheEvict(arguments, targetClass, method, evict);
            } catch (Exception ex) {
                LOG.error(ex.toString(), ex);
            }
        }
    }

    @Override
    public void intercept(Invocation inv) {

        Method method = inv.getMethod();

        CachesEvict cachesEvict = method.getAnnotation(CachesEvict.class);
        if (cachesEvict == null) {
            inv.invoke();
            return;
        }

        CacheEvict[] evicts = cachesEvict.value();

        List<CacheEvict> beforeInvocations = new ArrayList<>();
        List<CacheEvict> afterInvocations = new ArrayList<>();

        for (CacheEvict evict : evicts) {
            if (evict.beforeInvocation()) {
                beforeInvocations.add(evict);
            } else {
                afterInvocations.add(evict);
            }
        }


        Class targetClass = inv.getTarget().getClass();

        doCachesEvict(inv.getArgs(), targetClass, method, beforeInvocations);
        inv.invoke();
        doCachesEvict(inv.getArgs(), targetClass, method, afterInvocations);
    }
}
