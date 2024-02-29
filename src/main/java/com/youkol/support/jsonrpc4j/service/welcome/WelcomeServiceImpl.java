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

import org.springframework.stereotype.Service;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import com.youkol.support.jsonrpc4j.server.JsonRpcMultiServiceName;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
@Service
@AutoJsonRpcServiceImpl
@JsonRpcMultiServiceName("Welcome")
public class WelcomeServiceImpl implements WelcomeService {

    @Override
    public WelcomeResult welcome() {
        WelcomeResult result = new WelcomeResult();
        result.setCode("200");
        result.setMessage("success");
        result.setData("Welcome, Json-RPC Server is running.");

        return result;
    }

}
