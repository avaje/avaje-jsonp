/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.eclipse.parsson;

import org.eclipse.parsson.api.BufferPool;
import org.eclipse.parsson.api.JsonConfig;

import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;
import jakarta.json.spi.JsonProvider;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Jitendra Kotamraju
 * @author Kin-man Chung
 * @author Alex Soto
 */
public class JsonProviderImpl extends JsonProvider {
    private final BufferPool bufferPool = BufferPoolThreadLocal.provide();

    public JsonGenerator.Key createGeneratorKey(String key) {
        return JsonGeneratorImpl.key(key);
    }

    @Override
    public JsonGenerator createGenerator(Writer writer) {
        return new JsonGeneratorImpl(writer, bufferPool);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) {
        return new JsonGeneratorImpl(out, bufferPool);
    }

    @Override
    public JsonParser createParser(Reader reader) {
        return new JsonParserImpl(reader, bufferPool);
    }

    @Override
    public JsonParser createParser(InputStream in) {
        return new JsonParserImpl(in, bufferPool);
    }

    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        BufferPool pool = null;
        if (config != null && config.containsKey(BufferPool.class.getName())) {
            pool = (BufferPool)config.get(BufferPool.class.getName());
        }
        if (pool == null) {
            pool = bufferPool;
        }
        return new JsonParserFactoryImpl(pool);
    }

    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        Map<String, Object> providerConfig;
        boolean prettyPrinting;
        BufferPool pool;
        if (config == null) {
            providerConfig = Collections.emptyMap();
            prettyPrinting = false;
            pool = bufferPool;
        } else {
            providerConfig = new HashMap<>();
            if (prettyPrinting=JsonProviderImpl.isPrettyPrintingEnabled(config)) {
                providerConfig.put(JsonGenerator.PRETTY_PRINTING, true);
            }
            pool = (BufferPool)config.get(BufferPool.class.getName());
            if (pool != null) {
                providerConfig.put(BufferPool.class.getName(), pool);
            } else {
                pool = bufferPool;
            }
            providerConfig = Collections.unmodifiableMap(providerConfig);
        }

        return new JsonGeneratorFactoryImpl(providerConfig, prettyPrinting, pool);
    }

    private void addKnowProperty(Map<String, Object> providerConfig, Map<String, ?> config, String property) {
        if (config.containsKey(property)) {
            providerConfig.put(property, config.get(property));
        }
    }

    static boolean isPrettyPrintingEnabled(Map<String, ?> config) {
        return config.containsKey(JsonGenerator.PRETTY_PRINTING);
    }

    static boolean isRejectDuplicateKeysEnabled(Map<String, ?> config) {
        return config.containsKey(JsonConfig.REJECT_DUPLICATE_KEYS);
    }
}
