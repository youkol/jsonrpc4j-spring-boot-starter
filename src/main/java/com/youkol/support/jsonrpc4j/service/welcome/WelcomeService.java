/*
 * Copyright (C) 2024 the original author or authors.
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
package com.youkol.support.jsonrpc4j.service.welcome;

import java.time.LocalDateTime;

import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.youkol.support.jsonrpc4j.service.JsonRpcBaseService;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
@JsonRpcService("/jsonrpc/welcome")
public interface WelcomeService extends JsonRpcBaseService {

    @JsonRpcMethod(value = "Welcome.welcome")
    WelcomeResult welcome();

    public static class WelcomeResult {

        private String code;

        private String message;

        private String data;

        private LocalDateTime timestamp = LocalDateTime.now();

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return this.code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getData() {
            return this.data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public LocalDateTime getTimestamp() {
            return this.timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
