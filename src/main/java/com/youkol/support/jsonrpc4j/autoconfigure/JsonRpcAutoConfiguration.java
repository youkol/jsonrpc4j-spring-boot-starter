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
package com.youkol.support.jsonrpc4j.autoconfigure;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.ConvertedParameterTransformer;
import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.HttpStatusCodeProvider;
import com.googlecode.jsonrpc4j.InvocationListener;
import com.googlecode.jsonrpc4j.JsonRpcInterceptor;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.RequestInterceptor;
import com.youkol.support.jsonrpc4j.server.DelegatingRequestInterceptor;
import com.youkol.support.jsonrpc4j.server.JsonRpcMultiServer;
import com.youkol.support.jsonrpc4j.server.JsonRpcMultiServiceName;
import com.youkol.support.jsonrpc4j.service.JsonRpcBaseService;

/**
 * Auto-configuration for the {@link JsonRpcServer}
 *
 * @author jackiea
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ JsonRpcServer.class, JsonRpcMultiServer.class, ObjectMapper.class })
@EnableConfigurationProperties(JsonRpcProperties.class)
@ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "enabled", matchIfMissing = true)
@Import({ WelcomeConfiguration.class, JsonRpcServerServletConfiguration.class, JsonRpcAnnotationConfiguration.class })
public class JsonRpcAutoConfiguration {

    @Bean
    @ConditionalOnBean(RequestInterceptor.class)
    public DelegatingRequestInterceptor delegatingRequestInterceptor(
            ObjectProvider<RequestInterceptor> requestInterceptor) {
        List<RequestInterceptor> interceptors = requestInterceptor.orderedStream().collect(Collectors.toList());
        return new DelegatingRequestInterceptor(interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "server.enabled", matchIfMissing = true)
    public JsonRpcServer jsonRpcServer(JsonRpcProperties jsonRpcProperties,
            ObjectProvider<ObjectMapper> objectMapper,
            List<? extends JsonRpcBaseService> jsonRpcBaseService,
            ObjectProvider<DelegatingRequestInterceptor> requestInterceptor,
            ObjectProvider<ErrorResolver> errorResolver,
            ObjectProvider<JsonRpcInterceptor> jsonRpcInterceptor,
            ObjectProvider<InvocationListener> invocationListener,
            ObjectProvider<ConvertedParameterTransformer> convertedParameterTransformer,
            ObjectProvider<HttpStatusCodeProvider> httpStatusCodeProvider,
            ObjectProvider<ExecutorService> batchExecutorService,
            List<JsonRpcServerCustomizer> jsonRpcServerCustomizers) {
        JsonRpcMultiServer jsonRpcServer = new JsonRpcMultiServer(objectMapper.getIfAvailable(ObjectMapper::new));

        jsonRpcServer.setBackwardsCompatible(jsonRpcProperties.getServer().getBackwardsCompatible());
        jsonRpcServer.setAllowLessParams(jsonRpcProperties.getServer().getAllowLessParams());
        jsonRpcServer.setAllowExtraParams(jsonRpcProperties.getServer().getAllowExtraParams());
        jsonRpcServer.setRethrowExceptions(jsonRpcProperties.getServer().getRethrowExceptions());
        jsonRpcServer.setShouldLogInvocationErrors(jsonRpcProperties.getServer().getShouldLogInvocationErrors());

        requestInterceptor.ifAvailable(jsonRpcServer::setRequestInterceptor);
        errorResolver.ifAvailable(jsonRpcServer::setErrorResolver);

        List<JsonRpcInterceptor> jsonRpcInterceptors = jsonRpcInterceptor.orderedStream().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(jsonRpcInterceptors)) {
            jsonRpcServer.setInterceptorList(jsonRpcInterceptors);
        }

        invocationListener.ifAvailable(jsonRpcServer::setInvocationListener);
        convertedParameterTransformer.ifAvailable(jsonRpcServer::setConvertedParameterTransformer);
        httpStatusCodeProvider.ifAvailable(jsonRpcServer::setHttpStatusCodeProvider);

        batchExecutorService.ifAvailable(jsonRpcServer::setBatchExecutorService);
        jsonRpcServer.setParallelBatchProcessingTimeout(
                jsonRpcProperties.getServer().getParallelBatchProcessingTimeout().toMillis());

        if (StringUtils.hasText(jsonRpcProperties.getServer().getContentType())) {
            jsonRpcServer.setContentType(jsonRpcProperties.getServer().getContentType());
        }

        this.addService(jsonRpcServer, jsonRpcBaseService);

        this.customize(jsonRpcServer, jsonRpcServerCustomizers);

        return jsonRpcServer;
    }

    private void addService(JsonRpcMultiServer jsonRpcMultiServer,
            List<? extends JsonRpcBaseService> jsonRpcBaseService) {
        jsonRpcBaseService.stream().forEach(service -> {
            JsonRpcMultiServiceName serviceNameAnnotation = AnnotationUtils.findAnnotation(service.getClass(),
                    JsonRpcMultiServiceName.class);
            if (serviceNameAnnotation == null) {
                return;
            }

            if (!StringUtils.hasText(serviceNameAnnotation.value())) {
                throw new IllegalArgumentException("The value of JsonRpcMultiServiceNamed annotation must not be null");
            }

            Class<?> serviceInterface = Stream.of(service.getClass().getInterfaces())
                    .filter(Objects::nonNull)
                    .filter(t -> !Objects.equals(t.getCanonicalName(), JsonRpcBaseService.class.getCanonicalName()))
                    .findFirst()
                    .orElse(null);

            jsonRpcMultiServer.addService(serviceNameAnnotation.value(), service, serviceInterface);
        });
    }

    private void customize(JsonRpcServer jsonRpcServer, List<JsonRpcServerCustomizer> customizers) {
        for (JsonRpcServerCustomizer customizer : customizers) {
            customizer.customize(jsonRpcServer);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @AutoConfigureAfter(TaskExecutionAutoConfiguration.class)
    @ConditionalOnBean(ThreadPoolTaskExecutor.class)
    @ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "server.parallel-enabled", matchIfMissing = false)
    public static class JsonRpcServerParallelBatchConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ExecutorService executorService(ThreadPoolTaskExecutor taskExecutor) {
            return taskExecutor.getThreadPoolExecutor();
        }
    }

}
