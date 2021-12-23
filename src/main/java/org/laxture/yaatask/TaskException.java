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
public class TaskException extends Exception {

    private static final long serialVersionUID = -1053643297931040840L;

    public static final int ERROR_CODE_SUCCESSFUL = 0;

    public static final int ERROR_CODE_INTERNAL_SERVER_ERROR = 6000000;

    public static final int ERROR_CODE_THREAD_MANAGEMENT_ERROR = 6000001;

    private final int mErrorCode;

    public TaskException(int errorCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        mErrorCode = errorCode;
    }

    public TaskException(int errorCode, String detailMessage) {
        super(detailMessage);
        mErrorCode = errorCode;
    }

    public TaskException(int errorCode, Throwable throwable) {
        super(throwable);
        mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

}
