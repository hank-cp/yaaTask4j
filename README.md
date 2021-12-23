# YaaTask4j
Yet another async task manager for java to simplify multi-thread task execution on Java.

### Setup
* Maven
    ```
    <dependency>
        <groupId>org.laxture</groupId>
        <artifactId>yaaTask4j</artifactId>
        <version>0.1.0</version>
    </dependency>
    ```
* Gradle
    ```
    dependencies {
        implementation 'org.laxture:yaaTask4j:0.1.0'
    }
    ```
  
### Usage
```java
  ImmediateTaskManager.getInstance().queue(TaskBuilder.build(() -> {
    // hello world
  }));
```

### Features
* `YaaTasks` with same id will be executed only once in a `TaskManager`
* `TaskManager` interface
  * Execution
    * `queue(task)` put the task to the end of execution queue.
    * `queueAndAwait(task)` put the task to the end of execution queue and wait for it finished.
    * `push(task)` put the task to the head of execution queue.
    * `pushAndAwait(task)` put the task to the head of execution queue and wait for it finished.
  * Management
    * `getRunningTaskCount()` get currently running tasks count.
    * `getPendingTaskCount()` get currently pending tasks count.
    * `getRunningTasks()` get currently running tasks
    * `getPendingTasks()` get currently pending tasks
    * `getAllTasks()` get all tasks
    * `findTask(taskId)` find task by task id
    * `findTaskInQueue(taskId)` find pending task by task id
    * `cancelAll()` cancel all tasks execution, include running tasks
    * `cancelByTag(taskTag)` cancel tasks by provided tag execution, include running tasks
    * `cancel(task)` cancel specific task execution
* Three `TaskManager` is provided for different purposes
  * `ImmediateTaskManager` runs task immediately.
  * `SerialTaskManager` runs task one by one in serial.
  * `QueueTaskManager` runs task in a concurrent queue, maximum concurrent count is default
    to host machine's CPU core count.

### License 

```
/*
 * Copyright (C) 2019-present the original author or authors.
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
```