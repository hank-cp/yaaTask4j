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

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SimulatedLatchTask extends SimulatorTask {

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    protected String simulateRun() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            new RuntimeException("Failed to sleep", e);
        }
        return "SimulatorTask Finished";
    }

    public void greenLight() {
        latch.countDown();
    }

    @Override
    public boolean cancel() {
        greenLight();
        return super.cancel();
    }

}
