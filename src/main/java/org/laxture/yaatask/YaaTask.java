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
import org.apache.commons.lang3.StringUtils;
import org.laxture.yaatask.TaskListener.TaskCancelledListener;
import org.laxture.yaatask.TaskListener.TaskFailedListener;
import org.laxture.yaatask.TaskListener.TaskFinishedListener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Slf4j
public abstract class YaaTask<Result> {

    // state
    public enum State {
        NotStart, Pending, Running, Finished, Cancelled, Failed
    }
    volatile State mState = State.NotStart;
    public State getState() { return mState; }
    public void setState(State state) { mState = state; }

    private Result mResult;

    // thread control
    final AtomicBoolean mCancelled = new AtomicBoolean();
    final AtomicBoolean mTaskInvoked = new AtomicBoolean();

    // listeners
    private final Set<TaskFinishedListener<Result>> mFinishedListeners = new HashSet<>();
    private final Set<TaskCancelledListener<Result>> mCancelledListeners = new HashSet<>();
    private final Set<TaskFailedListener<Result>> mFailedListeners = new HashSet<>();

    // error code
    private TaskException mException;
    public TaskException getErrorDetails() { return mException; }
    public void setErrorDetails(TaskException exception) { mException = exception; }

    private String mId = UUID.randomUUID().toString();
    public String getId() { return mId; }
    public void setId(String id) { mId = id; }

    private Object mTag;
    public Object getTag() { return mTag; }
    public void setTag(Object tag) { mTag = tag; }

    //*************************************************************************
    // These method need to be override in sub class
    //*************************************************************************

    public abstract Result run();

    public boolean cancel() {
        mCancelled.set(true);
        return true;
    }

    public void setResult(Result result) {
        mResult = result;
    }

    public Result getResult() {
        return mResult;
    }

    public long getWaitingTime() {
        return 0L;
    }

    public long getUsedTime() {
        return 0L;
    }

    //*************************************************************************
    // Public/Protected Method
    //*************************************************************************

    public YaaTask<Result> addFinishedListener(TaskFinishedListener<Result> listener) {
        mFinishedListeners.add(listener);
        return this;
    }

    public YaaTask<Result> addCancelledListener(TaskCancelledListener<Result> listener) {
        mCancelledListeners.add(listener);
        return this;
    }

    public YaaTask<Result> addFailedListener(TaskFailedListener<Result> listener) {
        mFailedListeners.add(listener);
        return this;
    }

    public void cloneTaskListeners(YaaTask<Result> task) {
        mFinishedListeners.addAll(task.mFinishedListeners);
        mCancelledListeners.addAll(task.mCancelledListeners);
        mFailedListeners.addAll(task.mFailedListeners);
    }

    public void removeAllTaskListeners() {
        mFinishedListeners.clear();
        mCancelledListeners.clear();
        mFailedListeners.clear();
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    //*************************************************************************
    // Internal Implementation
    //*************************************************************************

    /**
     * If cancel, try to call cancel callback
     */
    void postResultIfNotInvoked(Result result) {
        if (!mTaskInvoked.get()) {
            postResult(result);
        }
    }

    /**
     * This method must be called somewhere to trigger TaskListener callback.
     */
    protected Result postResult(Result result) {
        finish(result, getErrorDetails());
        return result;
    }

    private void finish(Result result, TaskException exception) {
        if (isCancelled()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s%s is cancelled in %d ms.", getClass().getSimpleName(),
                        StringUtils.isEmpty(mId) ? "" : " " + mId, getUsedTime()));
            }
            onTaskCancelled(result);
            setState(State.Cancelled);

        } else if (mException != null) {
            log.warn(String.format("%s%s quit by error %s in %d ms.", getClass().getSimpleName(),
                StringUtils.isEmpty(mId) ? "" : " " + mId, mException.getErrorCode(), getUsedTime()));
            onTaskFailed(result, mException);
            setState(State.Failed);

        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s%s is completed in %dms.", getClass().getSimpleName(),
                    StringUtils.isEmpty(mId) ? "" : " " + mId, getUsedTime()));
            }
            onTaskFinished(result);
            setState(State.Finished);
        }
    }

    //*************************************************************************
    // TaskCallback
    //
    // These methods will be delegated to UI thread, make sure they are only used
    // to update UI.
    //*************************************************************************

    public void onTaskFinished(final Result returnObj) {
        if (mFinishedListeners.size() == 0) return;
        for (final TaskFinishedListener<Result> callback : mFinishedListeners) {
            callback.onTaskFinished(returnObj);
        }
    }

    public void onTaskCancelled(final Result result) {
        if (mCancelledListeners.size() == 0) return;
        for (final TaskCancelledListener<Result> callback : mCancelledListeners) {
            callback.onTaskCancelled(result);
        }
    }

    public void onTaskFailed(final Result result, final TaskException ex) {
        if (mFailedListeners.size() == 0) return;
        for (final TaskFailedListener<Result> callback : mFailedListeners) {
            callback.onTaskFailed(result, ex);
        }
    }
}
