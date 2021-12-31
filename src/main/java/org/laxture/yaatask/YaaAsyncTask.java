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

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import static org.laxture.yaatask.TaskException.ERROR_CODE_INTERNAL_SERVER_ERROR;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Slf4j
public abstract class YaaAsyncTask<Result> extends YaaTask<Result> {

    private final MyFutureTask<Result> mFuture;

    private AtomicLong mJoinTime;
    private AtomicLong mStartTime;
    private AtomicLong mEndTime;

    //*************************************************************************
    // These methods need to be override in sub class
    //*************************************************************************

    /**
     * Attempt to cancel this task.
     *
     * Override this method in sub-class if custom cancel action is needed,
     * e.g. HttpConnection.abort()
     */
    public boolean cancel() {
        super.cancel();
        // call cancelled callback immediately if task is still submitToApproval to execute.
        if (mState == State.Pending) onTaskCancelled(null);
        return mFuture.cancel(true);
    }

    //*************************************************************************
    // Public/Protected Method
    //*************************************************************************

    @Override
    public void setResult(Result result) {
        mFuture.set(result);
    }

    @Override
    public Result getResult() {
        try {
            return mFuture.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get task result. "+e.getCause(), e);
        }
    }

    @Override
    public long getWaitingTime() {
        return Optional.ofNullable(mStartTime).map(AtomicLong::longValue).orElse(System.currentTimeMillis())
                - Optional.ofNullable(mJoinTime).map(AtomicLong::longValue).orElse(0L);
    }

    @Override
    public long getUsedTime() {
        return Optional.ofNullable(mEndTime).map(AtomicLong::longValue).orElse(System.currentTimeMillis())
                - Optional.ofNullable(mStartTime).map(AtomicLong::longValue).orElse(0L);
    }

    /**
     * add to specific Executor
     */
    MyFutureTask<Result> executeOnExecutor(Executor exec) {
        if (mState != State.NotStart) {
            switch (mState) {
                case Pending:
                case Running:
                    log.error("Cannot start task:"
                            + " the task is already running or pending in queue.");

                default:
                    log.error("Cannot start task:"
                            + " the task has already been executed or cancelled"
                            + "(a task can be executed only once)");

                    return mFuture;
            }
        }
        mJoinTime = new AtomicLong(System.currentTimeMillis());
        setState(State.Pending);
        exec.execute(mFuture);

        return mFuture;
    }

    MyFutureTask<Result> getFuture() {
        return mFuture;
    }

    //*************************************************************************
    // Internal Implementation
    //*************************************************************************

    /**
     * Task must be created on Main thread.
     */
    public YaaAsyncTask() {
        Callable<Result> worker = () -> {
            mTaskInvoked.set(true);
            setState(State.Running);

            mStartTime = new AtomicLong(System.currentTimeMillis());
            Result result = run();
            mEndTime = new AtomicLong(System.currentTimeMillis());

            return postResult(result);
        };

        mFuture = new MyFutureTask<Result>(this, worker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    log.warn("Thread Interrupted error.", e);
                } catch (ExecutionException e) {
                    log.error("An error occured while executing run()", e);
                    setErrorDetails(new TaskException(ERROR_CODE_INTERNAL_SERVER_ERROR, e));
                    postResultIfNotInvoked(null);
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                    // cancel failed, wait to finished.
                }
            }
        };
    }

    //*************************************************************************
    // FutureTask
    //*************************************************************************

    public static class MyFutureTask<V> extends FutureTask<V> {

        private final YaaAsyncTask<V> mTask;
        YaaAsyncTask<V> getTask() { return mTask; }

        MyFutureTask(YaaAsyncTask<V> task, Callable<V> callable) {
            super(callable);
            mTask = task;
        }

        // expose to AbstractAsyncTask
        @Override
        protected void set(V v) {
            super.set(v);
        }
    }

}
