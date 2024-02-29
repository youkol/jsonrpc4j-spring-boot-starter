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
package com.youkol.support.jsonrpc4j.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;

import com.googlecode.jsonrpc4j.JsonRpcServer;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
public class JsonRpcServlet extends HttpServlet {

    private final JsonRpcServer jsonRpcServer;

    public JsonRpcServlet(JsonRpcServer jsonRpcServer) {
        Assert.notNull(jsonRpcServer, "JsonRpcServer must not be null.");
        this.jsonRpcServer = jsonRpcServer;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.jsonRpcServer.handle(req, resp);
    }

}
