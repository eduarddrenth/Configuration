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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.io.File;

/**
 *
 * @author eduard
 */
@ApplicationScoped
public class PropertyLocationProvider {
    
    @Produces   
    @ConfigFileUrls
    public String[] getUrl() {
        return new String[]{"src/test/resources/test.properties"};
    }

    @Produces
    @FromJar
    public boolean fromJar() {
        return false;
    }

    @Produces
    @AutoReload
    public boolean autoReload() {
        return true;
    }

    @Produces
    @POLL_INTERVAL
    public int pollInterval() {
        return 1000;
    }


}
