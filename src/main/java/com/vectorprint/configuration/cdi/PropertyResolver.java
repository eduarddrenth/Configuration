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
package com.vectorprint.configuration.cdi;

import com.vectorprint.StringConverter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.Settings;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.ReloadableProperties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Application scoped CDI bean responsible for providing {@link CDIProperties} with properties read from a Url. CDIProperties is
 * in its turn responsible for injecting properties based on @Inject and @Property. This Resolver needs an Application scoped bean that
 * produces urls, a boolean fromJar, a boolean autoReload and an int interval (for autoreload), it is up to users of this library to provide that bean. The bean can for example use System properties that are set in the environment to produce the values.
 *
 * @author eduard
 */
@ApplicationScoped
public class PropertyResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyResolver.class.getName());

    /**
     * Load properties from jar or url, needs a CDI producer for its arguments
     *
     * @param configFileUrls create an application scoped bean to produce this
     *                       argument, pointing to a configfile
     * @param fromJar        create an application scoped bean to produce this
     *                       argument, when true read from jar in classpath
     * @param autoReload     create an application scoped bean to produce this
     *                       argument, when true autoreload properties
     * @param interval       poll interval in millisecond when autoreload is true
     * @throws Exception
     * @see StringConverter.URLParser
     */
    @Produces
    @PropertyProducer
    public EnhancedMap initSettings(@FromJar Boolean fromJar, @AutoReload boolean autoReload, @POLL_INTERVAL int interval, @ConfigFileUrls String... configFileUrls) throws Exception {
        return readAsSettingsOrProperties(fromJar, autoReload, interval, configFileUrls);
    }

    /**
     * Try to read a property file, see {@link ParsingProperties#ParsingProperties(EnhancedMap, String...)}.
     * Properties will be {@link CachingProperties cached}.
     *
     * @param fromJar    when true read from jar in classpath
     * @param autoReload when true and fromJar is changes in the property file will be propagated to injected properties
     * @param interval seconds to check for changes in the property file
     * @param urls
     * @return
     * @throws VectorPrintException
     * @throws IOException
     */
    private EnhancedMap readAsSettingsOrProperties(boolean fromJar, boolean autoReload, int interval, String... urls) throws Exception {
        return fromJar || !autoReload ?
                new CachingProperties(new ParsingProperties(new Settings(), urls)) :
                new CachingProperties(
                        new ReloadableProperties(new ObservableProperties(new Settings()), interval, urls));
    }
}
