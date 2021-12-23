/*
 * Copyright (C) 2021-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.laxture.yaatask;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * This TaskManager runs task immediately.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class ImmediateTaskManager extends TaskManager {

    private static ImmediateTaskManager instance;
    private ManagedThreadPoolExecutor mExecutor;

    public static ImmediateTaskManager getInstance() {
        if (instance == null) {
            synchronized (ImmediateTaskManager.class) {
                if (instance == null) {
                    instance = new ImmediateTaskManager();
                    instance.mExecutor = new ManagedThreadPoolExecutor(0, Integer.MAX_VALUE,
                            5L, TimeUnit.SECONDS,
                            new SynchronousQueue<>(), instance.getThreadFactory());
                }
            }
        }
        return instance;
    }

    @Override
    protected String getName() {
        return "ImmediateTask";
    }

    @Override
    protected int getThreadPriority() {
        return Thread.MAX_PRIORITY;
    }

    @Override
    protected ManagedThreadPoolExecutor getExecutor() {
        return mExecutor;
    }

}

