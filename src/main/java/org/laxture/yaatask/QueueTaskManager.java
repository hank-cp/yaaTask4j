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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This TaskManager runs task in a concurrent queue, maximum concurrent count is default
 * to host machine's CPU core count.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class QueueTaskManager extends TaskManager {

    private static final int KEEP_ALIVE = 0;
    private static final int QUEUE_CONSUMING_LIMIT = Runtime.getRuntime().availableProcessors();

    private static QueueTaskManager instance;
    private ManagedThreadPoolExecutor mExecutor;

    public static QueueTaskManager getInstance() {
        if (instance == null) {
            synchronized (QueueTaskManager.class) {
                if (instance == null) {
                    instance = new QueueTaskManager();
                    instance.mExecutor = new ManagedThreadPoolExecutor(
                            QUEUE_CONSUMING_LIMIT, QUEUE_CONSUMING_LIMIT,
                            KEEP_ALIVE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                            instance.getThreadFactory());
                }
            }
        }
        return instance;
    }

    @Override
    protected String getName() {
        return "QueueTask";
    }

    @Override
    protected ManagedThreadPoolExecutor getExecutor() {
        return mExecutor;
    }

}

