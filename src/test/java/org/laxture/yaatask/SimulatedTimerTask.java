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
public class SimulatedTimerTask extends SimulatorTask {

    private long mTime;

    public SimulatedTimerTask(long time) {
        mTime = time;
    }

    @Override
    protected String simulateRun() {
        try {
            Thread.sleep(mTime);
        } catch (InterruptedException e) {
            new RuntimeException("Failed to sleep", e);
        }
        return "SimulatorTask Finished";
    }

}
