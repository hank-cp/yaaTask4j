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

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class TaskManagerTest {

    private final static Logger log = LoggerFactory.getLogger(TaskManagerTest.class);

    @Test
    public void testOnTaskExecution() {
        CountDownLatch latch = new CountDownLatch(1);
        final SimulatedTimerTask task = new SimulatedTimerTask(500);
        task.testThreadLatch = latch;
        try {
            new Thread(() -> ImmediateTaskManager.getInstance().queue(task)).run();
            latch.await();
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        log.debug("Start assertion");
        assertFalse("OnTaskCancelled shouldn't be called", task.cancelled);
        assertFalse("isCancelled shouldn't be true", task.isCancelled());
        assertTrue("OnTaskFinished is not called", task.finished);
        assertFalse("OnTaskFailed shouldn't be called", task.failed);
        assertEquals("SimulatorTask Finished", task.getResult());

        waitAWhile();
        assertEquals(YaaTask.State.Finished, task.getState());
    }

    @Test
    public void testOnTaskCancelled() {
        CountDownLatch latch = new CountDownLatch(1);
        final SimulatedTimerTask task = new SimulatedTimerTask(500);
        task.testCancel = true;
        task.testThreadLatch = latch;
        try {
            new Thread(() -> ImmediateTaskManager.getInstance().queue(task)).run();
            latch.await();
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        log.debug("Start assertion");
        assertTrue("OnTaskCancelled is not called", task.cancelled);
        assertTrue("isCancelled should be true", task.isCancelled());
        assertFalse("OnTaskFinished shouldn't be called", task.finished);
        assertFalse("OnTaskFailed shouldn't be called", task.failed);
        assertEquals("SimulatorTask Cancelled", task.getResult());

        waitAWhile();
        assertEquals(YaaTask.State.Cancelled, task.getState());
    }

    @Test
    public void testOnTaskFailed() {
        CountDownLatch latch = new CountDownLatch(1);
        final SimulatedTimerTask task = new SimulatedTimerTask(500);
        task.testFail = true;
        task.testThreadLatch = latch;
        try {
            new Thread(() -> ImmediateTaskManager.getInstance().queue(task)).run();
            latch.await();
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        log.debug("Start assertion");
        assertFalse("OnTaskCancelled shouldn't be called", task.cancelled);
        assertFalse("isCancelled shouldn't be true", task.isCancelled());
        assertFalse("OnTaskFinished shouldn't be called", task.finished);
        assertTrue("OnTaskFailed is not called", task.failed);
        assertEquals("SimulatorTask Failed", task.getResult());

        waitAWhile();
        assertEquals(YaaTask.State.Failed, task.getState());
    }


    public void testRunImmediately() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.testThreadLatch = latch;
                new Thread(() -> ImmediateTaskManager.getInstance().queue(task)).run();
            }
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        waitAWhile();

        log.debug("assertion for <testRunImmediately> ");
        // all 5 tasks should be start immediately.
        assertEquals(5, ImmediateTaskManager.getInstance().getRunningTaskCount());
        List<YaaAsyncTask<?>> tasks = ImmediateTaskManager.getInstance().getRunningTasks();
        assertEquals(5, tasks.size());

        log.debug("Release waiting tasks");
        for (YaaAsyncTask<?> task : tasks) {
            SimulatedLatchTask latchTask = (SimulatedLatchTask) task;
            latchTask.greenLight();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("loch latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        log.debug("assertion after release latch");
        assertEquals(0, ImmediateTaskManager.getInstance().getRunningTaskCount());
        tasks = ImmediateTaskManager.getInstance().getRunningTasks();
        assertEquals(0, tasks.size());
    }

    public void testRunInSerial() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.testThreadLatch = latch;
                new Thread(() -> SerialTaskManager.getInstance().queue(task)).run();
            }
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // wait a while so task will be put in the running queue
        waitAWhile();

        log.debug("assertion for <testRunInSerial> ");
        assertEquals(1, SerialTaskManager.getInstance().getRunningTaskCount());
        assertEquals(4, SerialTaskManager.getInstance().getPendingTaskCount());

        SimulatedLatchTask runningTask = (SimulatedLatchTask) SerialTaskManager.getInstance().getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(1, SerialTaskManager.getInstance().getRunningTaskCount());
        assertEquals(3, SerialTaskManager.getInstance().getPendingTaskCount());

        runningTask = (SimulatedLatchTask) SerialTaskManager.getInstance().getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(1, SerialTaskManager.getInstance().getRunningTaskCount());
        assertEquals(2, SerialTaskManager.getInstance().getPendingTaskCount());

        runningTask = (SimulatedLatchTask) SerialTaskManager.getInstance().getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(1, SerialTaskManager.getInstance().getRunningTaskCount());
        assertEquals(1, SerialTaskManager.getInstance().getPendingTaskCount());

        runningTask = (SimulatedLatchTask) SerialTaskManager.getInstance().getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(1, SerialTaskManager.getInstance().getRunningTaskCount());
        assertEquals(0, SerialTaskManager.getInstance().getPendingTaskCount());

        runningTask = (SimulatedLatchTask) SerialTaskManager.getInstance().getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(0, SerialTaskManager.getInstance().getRunningTaskCount());
        assertEquals(0, SerialTaskManager.getInstance().getPendingTaskCount());
    }

    public void testQueueTask() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.setId(Integer.toString(i));
                task.testThreadLatch = latch;
                new Thread(() -> QueueTaskManager.getInstance().queue(task)).run();
            }
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // wait a while so task will be put in the running queue
        waitAWhile();

        log.debug("assertion for <testQueueTask> ");
        assertEquals(3, QueueTaskManager.getInstance().getRunningTaskCount());
        assertEquals(2, QueueTaskManager.getInstance().getPendingTaskCount());

        // task_0 is running
        SimulatedLatchTask task_0 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("0");
        assertEquals(YaaTask.State.Running, task_0.getState());
        SimulatedLatchTask task_1 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("1");
        assertEquals(YaaTask.State.Running, task_1.getState());
        SimulatedLatchTask task_2 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("2");
        assertEquals(YaaTask.State.Running, task_2.getState());
        // task_3 is submitToApproval in head of the queue
        SimulatedLatchTask task_3 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("3");
        assertEquals(YaaTask.State.Pending, task_3.getState());
        SimulatedLatchTask task_4 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("4");
        assertEquals(YaaTask.State.Pending, task_4.getState());
        // let the task_0 go
        task_0.greenLight();
        waitAWhile();
        assertEquals(3, QueueTaskManager.getInstance().getRunningTaskCount());
        assertEquals(1, QueueTaskManager.getInstance().getPendingTaskCount());
        // task_3 should be started now
        assertEquals(YaaTask.State.Running, task_3.getState());

        // push a new task_6 to head of waiting queue
        try {
            final SimulatedLatchTask task = new SimulatedLatchTask();
            task.setId("5");
            task.testThreadLatch = latch;
            new Thread(() -> QueueTaskManager.getInstance().push(task)).run();
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        waitAWhile();
        assertEquals(3, QueueTaskManager.getInstance().getRunningTaskCount());
        assertEquals(2, QueueTaskManager.getInstance().getPendingTaskCount());

        // let the task_3 go
        task_3.greenLight();
        waitAWhile();
        assertEquals(YaaTask.State.Finished, task_3.getState());
        // task_5 should be started now
        SimulatedLatchTask task_5 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("5");
        assertEquals(YaaTask.State.Running, task_5.getState());
    }

    public void testResueTask() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.setId(Integer.toString(i));
                task.testThreadLatch = latch;
                new Thread(() -> QueueTaskManager.getInstance().queue(task)).run();
            }
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // wait a while so task will be put in the running queue
        waitAWhile();

        log.debug("assertion for <testQueueTask> ");
        assertEquals(3, QueueTaskManager.getInstance().getRunningTaskCount());
        assertEquals(2, QueueTaskManager.getInstance().getPendingTaskCount());

        // task_0 is running
        SimulatedLatchTask task_0 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("0");
        assertEquals(YaaTask.State.Running, task_0.getState());
        SimulatedLatchTask task_1 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("1");
        assertEquals(YaaTask.State.Running, task_1.getState());
        SimulatedLatchTask task_2 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("2");
        assertEquals(YaaTask.State.Running, task_2.getState());
        // task_3 is submitToApproval in head of the queue
        SimulatedLatchTask task_3 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("3");
        assertEquals(YaaTask.State.Pending, task_3.getState());
        SimulatedLatchTask task_4 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("4");
        assertEquals(YaaTask.State.Pending, task_4.getState());

        // queue a new task_0, the original task_0 should be reuse.
        try {
            final SimulatedLatchTask task = new SimulatedLatchTask();
            task.setId("0");
            task.testThreadLatch = latch;
            new Thread(() -> QueueTaskManager.getInstance().queue(task)).run();
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // queue should be the same.
        log.debug("assertion for");
        assertEquals(3, QueueTaskManager.getInstance().getRunningTaskCount());
        assertEquals(2, QueueTaskManager.getInstance().getPendingTaskCount());
        // task_0 shouldn't be changed.
        SimulatedLatchTask task_0_2 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("0");
        assertSame(task_0_2, task_0);

        // push a new task_4, the original task_0 should be reuse.
        try {
            final SimulatedLatchTask task = new SimulatedLatchTask();
            task.setId("4");
            task.testThreadLatch = latch;
            new Thread(() -> QueueTaskManager.getInstance().push(task)).run();
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // queue should be the same.
        log.debug("assertion for");
        assertEquals(3, QueueTaskManager.getInstance().getRunningTaskCount());
        assertEquals(2, QueueTaskManager.getInstance().getPendingTaskCount());
        // task_4 shouldn't be changed.
        SimulatedLatchTask task_4_2 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("4");
        assertSame(task_4_2, task_4);
    }

    public void testCancelByTag() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.setId(Integer.toString(i));
                if (i % 2 == 0) task.setTag("even");
                task.testThreadLatch = latch;
                new Thread(() -> QueueTaskManager.getInstance().queue(task)).run();
            }
        } catch (Throwable e) {
            log.error("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // wait a while so task will be put in the running queue
        waitAWhile();

        log.debug("assertion for <testQueueTask> ");
        assertEquals(3, QueueTaskManager.getInstance().getRunningTaskCount());
        assertEquals(2, QueueTaskManager.getInstance().getPendingTaskCount());

        SimulatedLatchTask task_0 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("0");
        assertEquals(YaaTask.State.Running, task_0.getState());
        SimulatedLatchTask task_1 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("1");
        assertEquals(YaaTask.State.Running, task_1.getState());
        SimulatedLatchTask task_2 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("2");
        assertEquals(YaaTask.State.Running, task_2.getState());
        // task_3 is submitToApproval in head of the queue
        SimulatedLatchTask task_3 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("3");
        assertEquals(YaaTask.State.Pending, task_3.getState());
        SimulatedLatchTask task_4 = (SimulatedLatchTask) QueueTaskManager.getInstance().findTask("4");
        assertEquals(YaaTask.State.Pending, task_4.getState());

        log.debug("assertion for cancelByTag ");
        QueueTaskManager.getInstance().cancelByTag("even");
        waitAWhile();
        assertEquals(2, QueueTaskManager.getInstance().getAllTasks().size());
        // even task might be finished before cancelled
        assertTrue(YaaTask.State.Cancelled == task_0.getState());
        assertTrue(YaaTask.State.Cancelled == task_2.getState());
        assertTrue(YaaTask.State.Cancelled == task_4.getState());
        // task 1 keep running
        assertEquals(YaaTask.State.Running, task_1.getState());
        // task 3 start to run
        assertTrue(YaaTask.State.Running ==task_3.getState()
                || YaaTask.State.Pending == task_3.getState());
    }


    @After
    public void tearDown() throws Exception {
        SerialTaskManager.getInstance().cancelAll();
        ImmediateTaskManager.getInstance().cancelAll();
        QueueTaskManager.getInstance().cancelAll();
        waitAWhile();
        assertEquals(0, SerialTaskManager.getInstance().getRunningTaskCount());
        assertEquals(0, SerialTaskManager.getInstance().getPendingTaskCount());
        assertEquals(0, ImmediateTaskManager.getInstance().getRunningTaskCount());
        assertEquals(0, ImmediateTaskManager.getInstance().getPendingTaskCount());
        assertEquals(0, QueueTaskManager.getInstance().getRunningTaskCount());
        assertEquals(0, QueueTaskManager.getInstance().getPendingTaskCount());
    }

    private static final long WAIT_FOR_QUEUE = 200;

    public void waitAWhile() {
        try {
            Thread.sleep(WAIT_FOR_QUEUE);
        } catch (InterruptedException e) {
            log.error("loch latch failed", e);
            assertFalse(e.getMessage(), true);
        }
    }
}
