# YaaTask4j
是的, 这又是一个Java的多线程轮子. 接口简单清晰, 十年生产环境磨砺, 品质有保证.

### 安装
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

### 用法
```java
  ImmediateTaskManager.getInstance().queue(TaskBuilder.build(
      taskId, // 可选, 可以用来从`TaskManager`找到这个task
      taskTag, // 可选, 可以用来从`TaskManager`找到这个task
      () -> {
          // hello world
      }));
```

### 特性
* `YaaTasks`在一个`TaskManager`中只会被运行一次
* `TaskManager`接品
  * 运行
    * `queue(task)` 将task丢到队列中排队执行
    * `queueAndAwait(task)` 将task丢到队列中排队执行, 并等待其线束返回
    * `push(task)` 将task插到队列最前面
    * `pushAndAwait(task)` 将task插到队列最前面, 并等待其线束返回
  * 取消
    * `cancelAll()` 取消队列中所有task, 包括正在执行的task
    * `cancelByTag(taskTag)` 按taskTag取消队列中的task, 包括正在执行的task
    * `cancel(task)` 取消指定的task
  * 管理
    * `getRunningTaskCount()` 获取当前正在执行中的task数量
    * `getPendingTaskCount()` 获取当前正在排队的task数量
    * `getRunningTasks()` 获取当前正在运行的task实例
    * `getPendingTasks()` 获取当前正在排队的task实例
    * `getAllTasks()` 获取所有task实例
    * `findTask(taskId)` 按taskId找到task
    * `findTaskInQueue(taskId)`  按taskId找到正在排队的task (还没执行)
* 提供三个`TaskManager`实例, 适用于不同的场景:
  * `ImmediateTaskManager`立即执行task.
  * `SerialTaskManager`同时间只执行一个task, 队列中的task会一个接一个执行.
  * `QueueTaskManager`并发执行task, 并发数为当前主机的CPU核数.
  * 想要自定义不同的线程池策略可以可以自己实现`TaskManager`.
* 支持Java8, 支持Android