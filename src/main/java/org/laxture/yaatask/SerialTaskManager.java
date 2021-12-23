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

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This TaskManager runs task one by one in serial.
 */
public class SerialTaskManager extends TaskManager {

    private static final int KEEP_ALIVE = 0;
    private static final int QUEUE_CONSUMING_LIMIT = 1;

    private static final String QUEUE_DEFAULT = "DEFAULT";
    public static final String QUEUE_SMS = "SMS";
    public static final String QUEUE_BIZ_EVENT = "BIZ_EVENT";
    public static final String QUEUE_COST_COMPUTING = "COST_COMPUTING";

    private static final Map<String, SerialTaskManager> instancePool = new Hashtable<>();
    private ManagedThreadPoolExecutor mExecutor;

    public static SerialTaskManager getInstance(String name) {
        if (instancePool.get(name) == null) {
            synchronized (SerialTaskManager.class) {
                if (instancePool.get(name) == null) {
                    SerialTaskManager instance = new SerialTaskManager();
                    instance = new SerialTaskManager();
                    instance.mExecutor = new ManagedThreadPoolExecutor(
                            QUEUE_CONSUMING_LIMIT, QUEUE_CONSUMING_LIMIT,
                            KEEP_ALIVE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                            instance.getThreadFactory());
                    instancePool.put(name, instance);
                }
            }
        }
        return instancePool.get(name);
    }

    public static SerialTaskManager getInstance() {
        return getInstance(QUEUE_DEFAULT);
    }

    @Override
    protected String getName() {
        return "SerialTask";
    }

    @Override
    protected ManagedThreadPoolExecutor getExecutor() {
        return mExecutor;
    }

    public static int getTotalPendingTaskCount() {
        return instancePool.values().stream().mapToInt(TaskManager::getPendingTaskCount).sum();
    }

}

