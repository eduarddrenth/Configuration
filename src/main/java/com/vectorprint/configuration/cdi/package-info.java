/*
 * Copyright 2018 Fryske Akademy.
 *
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
 */
/**
 * This package contains a setup for applications to retrieve application properties
 * from an external Url or from a jar. Users need to provide urls, a boolean "fromJar" and a boolean "autoReload" via
 * an application scoped cdi bean that produces these, see {@link com.vectorprint.configuration.cdi.ConfigFileUrls},
 * {@link com.vectorprint.configuration.cdi.FromJar} and {@link com.vectorprint.configuration.cdi.AutoReload}. Properties can than
 * be injected using Inject and {@link com.vectorprint.configuration.cdi.Property} and {@link com.vectorprint.configuration.cdi.Properties} annotations. The underlying configuration library
 * offers many features, such as support for data types, caching, reloadability, observablility, readonliness, threadsafety, helpsupport,
 * reading from input / writing to output, sorting.
 */
package com.vectorprint.configuration.cdi;
