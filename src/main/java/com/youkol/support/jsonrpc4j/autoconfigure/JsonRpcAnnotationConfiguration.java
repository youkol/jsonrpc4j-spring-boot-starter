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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.ConvertedParameterTransformer;
import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.HttpStatusCodeProvider;
import com.googlecode.jsonrpc4j.InvocationListener;
import com.googlecode.jsonrpc4j.JsonRpcInterceptor;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcClientProxyCreator;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(WelcomeConfiguration.class)
@ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "enabled", matchIfMissing = true)
public class JsonRpcAnnotationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "server.enabled", matchIfMissing = true)
    public AutoJsonRpcServiceImplExporter autoJsonRpcServiceImplExporter(
            JsonRpcProperties jsonRpcProperties,
            ObjectProvider<ObjectMapper> objectMapper,
            ObjectProvider<ErrorResolver> errorResolver,
            ObjectProvider<JsonRpcInterceptor> jsonRpcInterceptor,
            ObjectProvider<InvocationListener> invocationListener,
            ObjectProvider<ConvertedParameterTransformer> convertedParameterTransformer,
            ObjectProvider<HttpStatusCodeProvider> httpStatusCodeProvider,
            ObjectProvider<ExecutorService> batchExecutorService,
            List<AutoJsonRpcServiceImplExporterCustomizer> customizers) {
        AutoJsonRpcServiceImplExporter serviceExporter = new AutoJsonRpcServiceImplExporter();

        serviceExporter.setObjectMapper(objectMapper.getIfAvailable(ObjectMapper::new));
        serviceExporter.setBackwardsCompatible(jsonRpcProperties.getServer().getBackwardsCompatible());
        serviceExporter.setAllowLessParams(jsonRpcProperties.getServer().getAllowLessParams());
        serviceExporter.setAllowExtraParams(jsonRpcProperties.getServer().getAllowExtraParams());
        serviceExporter.setRethrowExceptions(jsonRpcProperties.getServer().getRethrowExceptions());
        serviceExporter.setShouldLogInvocationErrors(jsonRpcProperties.getServer().getShouldLogInvocationErrors());

        errorResolver.ifAvailable(serviceExporter::setErrorResolver);

        List<JsonRpcInterceptor> jsonRpcInterceptors = jsonRpcInterceptor.orderedStream().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(jsonRpcInterceptors)) {
            serviceExporter.setInterceptorList(jsonRpcInterceptors);
        }

        invocationListener.ifAvailable(serviceExporter::setInvocationListener);
        convertedParameterTransformer.ifAvailable(serviceExporter::setConvertedParameterTransformer);
        httpStatusCodeProvider.ifAvailable(serviceExporter::setHttpStatusCodeProvider);

        batchExecutorService.ifAvailable(serviceExporter::setBatchExecutorService);
        serviceExporter.setParallelBatchProcessingTimeout(
                jsonRpcProperties.getServer().getParallelBatchProcessingTimeout().toMillis());

        if (StringUtils.hasText(jsonRpcProperties.getServer().getContentType())) {
            serviceExporter.setContentType(jsonRpcProperties.getServer().getContentType());
        }

        this.customize(serviceExporter, customizers);

        return serviceExporter;
    }

    private void customize(AutoJsonRpcServiceImplExporter exporter,
            List<AutoJsonRpcServiceImplExporterCustomizer> customizers) {
        for (AutoJsonRpcServiceImplExporterCustomizer customizer : customizers) {
            customizer.customize(exporter);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "client.enabled", matchIfMissing = false)
    public AutoJsonRpcClientProxyCreator autoJsonRpcClientProxyCreator(JsonRpcProperties jsonRpcProperties,
            ObjectProvider<ObjectMapper> objectMapper) {
        AutoJsonRpcClientProxyCreator autoJsonRpcClientProxyCreator = new AutoJsonRpcClientProxyCreator();

        autoJsonRpcClientProxyCreator.setObjectMapper(objectMapper.getIfAvailable(ObjectMapper::new));
        autoJsonRpcClientProxyCreator.setScanPackage(jsonRpcProperties.getClient().getScanPackage());
        autoJsonRpcClientProxyCreator.setBaseUrl(this.resolveBaseUrl(jsonRpcProperties.getClient().getBaseUrl()));

        return autoJsonRpcClientProxyCreator;
    }

    private URL resolveBaseUrl(String baseUrl) {
        try {
            return new URL(baseUrl);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("wkrj.jsonrpc.client.baseUrl is illegal.", ex);
        }
    }
}
