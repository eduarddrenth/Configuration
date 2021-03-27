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
import com.vectorprint.configuration.generated.jaxb.Settingstype;
import com.vectorprint.configuration.generated.parser.PropertiesParser;
import com.vectorprint.configuration.jaxb.SettingsFromJAXB;
import com.vectorprint.configuration.jaxb.SettingsXMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Application scoped CDI bean responsible for providing {@link CDIProperties} with properties read from a Url. CDIProperties is
 * in its turn responsible for injecting properties based on @Inject and @Property. This Resolver needs an Application scoped bean that
 * produces urls, a boolean fromJar, a boolean autoReload and an int interval (for autoreload), it is up to users of this library to provide that bean. The bean can for example use System properties that are set in the environment to produce the values.
 * @author eduard
 */
@ApplicationScoped
public class PropertyResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyResolver.class.getName());

    /**
     * Load properties from jar or url, needs a CDI producer for its arguments
     *
     * @param configFileUrls create an application scoped bean to produce this
     * argument, pointing to a configfile
     * @param fromJar create an application scoped bean to produce this
     * argument, when true read from jar in classpath
     * @param autoReload create an application scoped bean to produce this
     * argument, when true autoreload properties
     * @param interval poll interval in millisecond when autoreload is true
     * @see StringConverter.URLParser
     * @see SettingsXMLHelper#XSD
     * @throws Exception
     */
    @Produces
    @PropertyProducer
    public EnhancedMap initSettings(@FromJar Boolean fromJar, @AutoReload boolean autoReload, @POLL_INTERVAL int interval, @ConfigFileUrls String... configFileUrls) throws Exception {
        return readAsSettingsOrProperties(fromJar, autoReload, interval, configFileUrls);
    }

    private Reader getReader(String configFileUrl, boolean fromJar) throws IOException {
        InputStream is = fromJar
                        ? Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileUrl)
                        : StringConverter.URL_PARSER.convert(configFileUrl).openStream();
        if (is == null) {
            throw new IllegalArgumentException(String.format("%s not readable, reading from jar: %s", configFileUrl, fromJar));
        }
        return new InputStreamReader(is);
    }

    /**
     *
     * Try to read as either a {@link Settingstype xml configuration file} or
     * {@link PropertiesParser a property file} holding settings. Either {@link SettingsFromJAXB#fromJaxb(Reader)
     * } or {@link ParsingProperties#ParsingProperties(EnhancedMap, String...)
     * }
     * will be called. By default properties will be {@link CachingProperties cached} and {@link com.vectorprint.configuration.decoration.ReadonlyProperties read only}.
     *
     * @param fromJar when true read from jar in classpath
     * @param autoReload
     * @param interval
     * @param urls
     * @return
     * @throws JAXBException
     * @throws VectorPrintException
     * @throws IOException
     */
    private EnhancedMap readAsSettingsOrProperties(boolean fromJar, boolean autoReload, int interval, String... urls) throws Exception {
        if (urls.length==1) {
            String url = urls[0];
            try {
                try (Reader in = getReader(url, fromJar)) {
                    SettingsXMLHelper.validateXml(in);
                }
                try (Reader in = getReader(url, fromJar)) {
                    return new SettingsFromJAXB().fromJaxb(getReader(url, fromJar));
                }
            } catch (SAXException sAXException) {
                LOGGER.warn(String.format("%s does not contain settings xml, trying to parse settings directly", url));
            }
        }
        return fromJar || !autoReload ?
                new CachingProperties(new ParsingProperties(new Settings(), urls)) :
                new CachingProperties(
                    new ReloadableProperties(new ObservableProperties(new Settings()),interval, urls));
    }
}
