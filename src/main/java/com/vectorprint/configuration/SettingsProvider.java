package com.vectorprint.configuration;

/*
 * #%L
 * VectorPrintConfig3.0
 * %%
 * Copyright (C) 2011 - 2013 VectorPrint
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Map;

/**
 * A settings provider is responsible for providing configuration information to a
 * {@link Configurable configuarble object}.
 *
 * @see EnhancedMap
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface SettingsProvider<P extends Map> {

        /**
         *
         * @param configurable the value of configurable
         */
        void provideSettings(Configurable<P> configurable);
}
