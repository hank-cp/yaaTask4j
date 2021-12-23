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

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public final class TaskListener {

    /**
     * Fired where {@link YaaTask#run()} is finished. Parameter result must
     * be set by {@link YaaTask#setResult(Object)} during {@link YaaTask#run()}
     *
     * @param <Result>
     */
    public interface TaskFinishedListener<Result> {
        void onTaskFinished(Result result);
    }

    /**
     * Fired where {@link YaaTask#cancel()} is finished. Parameter result will be provided
     * if it's set by {@link YaaTask#setResult(Object)} during {@link YaaTask#run()}
     *
     * @param <Result>
     */
    public interface TaskCancelledListener<Result> {
        void onTaskCancelled(Result result);
    }

    /**
     * Fired where {@link YaaTask#run()} is finished and {@link YaaTask#getErrorDetails()}
     * is not empty. Parameter result will be provided if it's set by
     * {@link YaaTask#setResult(Object)} during {@link YaaTask#run()}
     *
     * @param <Result>
     */
    public interface TaskFailedListener<Result> {
        void onTaskFailed(Result result, TaskException ex);
    }

}
