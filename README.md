[![GitHub release](https://img.shields.io/github/release/hank-cp/yaaTask4j.svg)](https://github.com/hank-cp/sbp/releases)
![Maven Central](https://img.shields.io/maven-central/v/org.laxture/yaaTask4j)
![Test](https://img.shields.io/github/workflow/status/hank-cp/yaaTask4j/CI%20Test)
![GitHub](https://img.shields.io/github/license/hank-cp/yaaTask4j.svg)
![GitHub last commit](https://img.shields.io/github/last-commit/hank-cp/yaaTask4j.svg)

# YaaTask4j
**Y**et **A**nother **A**sync task manager for java to simplify multi-thread task execution for Java.
It has been used for production for 10+ years.

[中文](README.zh-cn.md)

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
    ImmediateTaskManager.getInstance().queue(TaskBuilder.build(
        taskId, // optional, could be used to find task from TaskManager later
        taskTag, // optional, could be used to find task from TaskManager later
        () -> {
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
  * Cancellation
    * `cancelAll()` cancel all tasks execution, include running tasks
    * `cancelByTag(taskTag)` cancel tasks by provided tag execution, include running tasks
    * `cancel(task)` cancel specific task execution
  * Management
    * `getRunningTaskCount()` get currently running tasks count.
    * `getPendingTaskCount()` get currently pending tasks count.
    * `getRunningTasks()` get currently running task instances.
    * `getPendingTasks()` get currently pending task instances.
    * `getAllTasks()` get all tasks.
    * `findTask(taskId)` find task by task id.
    * `findTaskInQueue(taskId)` find pending task by task id.
* Three `TaskManager` are provided for different purposes:
  * `ImmediateTaskManager` executes task immediately.
  * `SerialTaskManager` executes only one task at the same time. Tasks in queue will be executed one by one in serial.
  * `QueueTaskManager` executes task in a concurrent queue, maximum concurrent count is default
    to host machine's CPU core count.
  * You could implement `TaskManager` with your own thread pool policy.
* Java 8/Android supported.

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