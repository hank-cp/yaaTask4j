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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public abstract class SimulatorTask extends YaaAsyncTask<String> {

    private final static Logger log = LoggerFactory.getLogger(SimulatorTask.class);

    // test thread control
    public CountDownLatch testThreadLatch;

    // status
    public boolean finished;
    public boolean cancelled;
    public boolean failed;

    // switches
    public boolean testCancel;
    public boolean testFail;

    public SimulatorTask() {
        addFinishedListener(new DefaultSimulatorCallback());
        addCancelledListener(new DefaultSimulatorCallback());
        addFailedListener(new DefaultSimulatorCallback());
    }

    @Override
    public String run() {
        if (testCancel) {
            setResult("SimulatorTask Cancelled");
            cancel();
        }

        String result = simulateRun();

        if (testFail) {
            setErrorDetails(new TaskException(999, "SimulatorTask Failed"));
            return "SimulatorTask Failed";
        } else {
            return result;
        }
    }

    protected abstract String simulateRun();

    @Override
    public boolean cancel() {
        setResult("SimulatorTask Cancelled");
        return super.cancel();
    }

    public class DefaultSimulatorCallback implements
            TaskListener.TaskFinishedListener<String>,
            TaskListener.TaskCancelledListener<String>,
            TaskListener.TaskFailedListener<String> {

        @Override
        public void onTaskFinished(String result) {
            finished = true;
            log.debug("Simulator Task Finished");
            if (testThreadLatch != null) testThreadLatch.countDown();
        }

        @Override
        public void onTaskCancelled(String result) {
            cancelled = true;
            log.debug("Simulator Task Cancelled");
            if (testThreadLatch != null) testThreadLatch.countDown();
        }

        @Override
        public void onTaskFailed(String result, TaskException ex) {
            failed = true;
            log.debug("Simulator Task Failed");
            if (testThreadLatch != null) testThreadLatch.countDown();
        }
    }
}
