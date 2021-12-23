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
import org.laxture.yaatask.YaaAsyncTask.MyFutureTask;
import org.laxture.yaatask.TaskListener.TaskCancelledListener;
import org.laxture.yaatask.TaskListener.TaskFailedListener;
import org.laxture.yaatask.TaskListener.TaskFinishedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public abstract class TaskManager {

    private TaskFinishedListener mDefaultFinishedListener;

    private TaskCancelledListener mDefaultCancelledListener;

    private TaskFailedListener mDefaultFailedListener;

    public void setDefaultFinishedListener(TaskFinishedListener<?> listener) {
        this.mDefaultFinishedListener = listener;
    }

    public void setDefaultCancelledListener(TaskCancelledListener<?> listener) {
        this.mDefaultCancelledListener = listener;
    }

    public void setDefaultFailedListener(TaskFailedListener<?> listener) {
        this.mDefaultFailedListener = listener;
    }

    //*************************************************************************
    // Abstract Methods
    //*************************************************************************

    protected abstract String getName();

    protected abstract ManagedThreadPoolExecutor getExecutor();

    protected int getThreadPriority() {
        return Thread.NORM_PRIORITY;
    }

    //*************************************************************************
    // Public Methods
    //*************************************************************************

    public int getRunningTaskCount() {
        return getExecutor().getActiveCount();
    }

    public int getPendingTaskCount() {
        return getExecutor().getQueue().size();
    }

    public List<YaaAsyncTask<?>> getRunningTasks() {
        return new ArrayList<>(getExecutor().mRunningPool);
    }

    public List<YaaAsyncTask<?>> getPendingTasks() {
        return getExecutor().getQueue().stream().map(runnable -> ((MyFutureTask<?>) runnable).getTask()).collect(Collectors.toList());
    }

    public List<YaaAsyncTask<?>> getAllTasks() {
        ArrayList<YaaAsyncTask<?>> tasks
                = new ArrayList<>(getExecutor().mRunningPool);
        tasks.addAll(getPendingTasks());
        return tasks;
    }

    public YaaAsyncTask<?> findTask(String taskId) {
        if (StringUtils.isEmpty(taskId)) return null;
        for (YaaAsyncTask<?> task : getAllTasks()) {
            if (taskId.equals(task.getId())) return task;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public YaaAsyncTask<?> findTaskInQueue(String taskId) {
        if (StringUtils.isEmpty(taskId)) return null;
        for (Runnable runnable : getExecutor().getQueue()) {
            MyFutureTask future = (MyFutureTask) runnable;
            if (taskId.equals(future.getTask().getId())) return future.getTask();
        }
        return null;
    }

    /**
     * Put task to queue.
     */
    public void queue(YaaAsyncTask<?> task) {
        // no existed task, add to queue.
        if (reuseTask(task) == null) {
            if (this.mDefaultFinishedListener != null) {
                task.addFinishedListener(this.mDefaultFinishedListener);
            }
            if (this.mDefaultCancelledListener != null) {
                task.addCancelledListener(this.mDefaultCancelledListener);
            }
            if (this.mDefaultFailedListener!= null) {
                task.addFailedListener(this.mDefaultFailedListener);
            }
            task.executeOnExecutor(getExecutor());
        }
    }

    public <T> T queueAndAwait(YaaAsyncTask<T> task) {
        MyFutureTask<T> future = reuseTask(task);
        if (future == null) {
            future = task.executeOnExecutor(getExecutor());
        }
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            future.getTask().setErrorDetails(
                    new TaskException(TaskException.ERROR_CODE_THREAD_MANAGEMENT_ERROR, e));
            future.getTask().onTaskFailed(null,
                    future.getTask().getErrorDetails());
        }
        return null;
    }

    /**
     * Put an head of the queue.
     */
    public void push(YaaAsyncTask<?> task) {
        MyFutureTask<?> future = reuseTask(task);

        if (future == null)
            future = task.executeOnExecutor(getExecutor());

        // move added executor to the head of the queue
        if (future != null && future.getTask().getState() != YaaTask.State.Running) {
            List<Runnable> tempQueue = new ArrayList<>();
            getExecutor().getQueue().drainTo(tempQueue);
            tempQueue.remove(future);
            getExecutor().getQueue().offer(future);
            getExecutor().getQueue().addAll(tempQueue);
        }
    }

    public <T> T pushAndAwait(YaaAsyncTask<T> task) {
        MyFutureTask<T> future = reuseTask(task);

        if (future == null)
            future = task.executeOnExecutor(getExecutor());

        // move added executor to the head of the queue
        if (future.getTask().getState() != YaaTask.State.Running) {
            List<Runnable> tempQueue = new ArrayList<>();
            getExecutor().getQueue().drainTo(tempQueue);
            tempQueue.remove(future);
            getExecutor().getQueue().offer(future);
            getExecutor().getQueue().addAll(tempQueue);
        }

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            future.getTask().onTaskFailed(null,
                    new TaskException(TaskException.ERROR_CODE_THREAD_MANAGEMENT_ERROR, e));
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public void cancelAll() {
        synchronized (getExecutor().mRunningPool) {
            for (YaaAsyncTask<?> runningTask : getExecutor().mRunningPool) runningTask.cancel();

            // cancel submitToApproval queue tasks
            for (Runnable runnable : getExecutor().getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                future.getTask().cancel();
            }
            getExecutor().purge();
        }
    }

    @SuppressWarnings("rawtypes")
    public void cancelByTag(Object tag) {
        if (tag == null) return;

        synchronized (getExecutor().mRunningPool) {
            for (YaaAsyncTask<?> runningTask : getExecutor().mRunningPool) {
                if (tag.equals(runningTask.getTag())) runningTask.cancel();
            }

            // cancel submitToApproval queue tasks
            for (Runnable runnable : getExecutor().getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                if (tag.equals(future.getTask().getTag())) future.getTask().cancel();
            }
            getExecutor().purge();
        }
    }

    @SuppressWarnings("rawtypes")
    public void cancel(YaaAsyncTask<?> task) {
        if (task == null) return;

        for (YaaAsyncTask<?> runningTask : getExecutor().mRunningPool) {
            if (task == runningTask) runningTask.cancel();
            return;
        }

        // cancel submitToApproval queue tasks
        for (Runnable runnable : getExecutor().getQueue()) {
            MyFutureTask future = (MyFutureTask) runnable;
            if (task == future.getTask()) {
                future.getTask().cancel();
                getExecutor().purge();
                return;
            }
        }
    }

    //*************************************************************************
    // Internal Implementation
    //*************************************************************************

    private ThreadFactory mThreadFactory;

    protected ThreadFactory getThreadFactory() {
        if (mThreadFactory == null) {
            mThreadFactory = new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r,
                            getName()+"#" + mCount.getAndIncrement());
                    thread.setPriority(getThreadPriority());
                    return thread;
                }
            };
        }
        return mThreadFactory;
    }

    protected static class ManagedThreadPoolExecutor extends ThreadPoolExecutor {

        private final List<YaaAsyncTask<?>> mRunningPool;

        public ManagedThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                         long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                         ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                    threadFactory);
            mRunningPool = Collections.synchronizedList(new ArrayList<>());
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected void beforeExecute(Thread thread, Runnable runnable) {
            MyFutureTask future = (MyFutureTask) runnable;

            // it's possible that the task is finished during this for loop, and
            // task will be remove from sRunningPool
            // it might cause ConcurrentModificationException
            synchronized (mRunningPool) {
                mRunningPool.add(future.getTask());
            }
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected void afterExecute(Runnable runnable, Throwable throwable) {
            MyFutureTask future = (MyFutureTask) runnable;

            // it's possible that the task is finished during this for loop, and
            // task will be remove from sRunningPool
            // it might cause ConcurrentModificationException
            synchronized (mRunningPool) {
                mRunningPool.remove(future.getTask());
            }
        }
    }

    /**
     * reuse existing executor. if it exists, reset its listener so
     * current UI shall response.
     */
    @SuppressWarnings({"rawtypes"})
    private MyFutureTask reuseTask(YaaAsyncTask task) {
        if (StringUtils.isEmpty(task.getId())) return null;

        // connect latest TaskListener to existing task
        synchronized (getExecutor().mRunningPool) {
            for (YaaAsyncTask<?> runningTask : getExecutor().mRunningPool) {
                if (!task.getId().equals(runningTask.getId())) continue;
                // found!
                runningTask.cloneTaskListeners(task);
                return runningTask.getFuture();
            }

            // find in submitToApproval queue
            for (Runnable runnable : getExecutor().getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                if (!task.getId().equals(future.getTask().getId())) continue;
                // found!
                future.getTask().cloneTaskListeners(task);
                return future;
            }
        }

        return null;
    }

}

