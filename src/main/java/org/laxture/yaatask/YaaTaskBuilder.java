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

import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class YaaTaskBuilder {

    public static <Result> YaaAsyncTask<Result> buildTask(Supplier<Result> action) {
        return buildTask(null, null, action);
    }

    public static <Result> YaaAsyncTask<Result> buildTask(String taskId,
                                                          String taskTag,
                                                          Supplier<Result> action) {
        YaaAsyncTask<Result> task = new YaaAsyncTask<Result>() {
            @Override
            public Result run() {
                return action.get();
            }
        };
        if (!StringUtils.isEmpty(taskId)) {
            task.setId(taskId);
        }
        if (!StringUtils.isEmpty(taskTag)) {
            task.setTag(taskTag);
        }
        return task;
    }

    public static void main(String[] args) {
        DelayTaskManager.getInstance().schedule(YaaTaskBuilder.buildTask(() -> {
            System.out.println(System.currentTimeMillis());
            return null;
        }), 1000);

        DelayTaskManager.getInstance().schedule(YaaTaskBuilder.buildTask(() -> {
            System.out.println(System.currentTimeMillis());
            return null;
        }), 1000);

        DelayTaskManager.getInstance().schedule(YaaTaskBuilder.buildTask(() -> {
            System.out.println(System.currentTimeMillis());
            return null;
        }), 1000);

        DelayTaskManager.getInstance().schedule(YaaTaskBuilder.buildTask(() -> {
            System.out.println(System.currentTimeMillis());
            return null;
        }), 1000);

        while (true) {
            // wait
        }
    }
}

