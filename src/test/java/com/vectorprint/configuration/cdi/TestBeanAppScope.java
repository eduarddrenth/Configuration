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

import com.vectorprint.configuration.EnhancedMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;

/**
 *
 * @author eduard
 */
@ApplicationScoped
public class TestBeanAppScope {

    private String testProp;
    @Inject
    @Property(keys = "fprop", required = true)
    private File fprop;
    @Inject
    @Property(required = true)
    private Boolean bprop;
    @Inject
    @Property(keys = "zprop", required = false)
    private String zprop;

    @Inject
    public void setTestProp(@Property(keys = "prop", required = true) String testProp) {
        this.testProp = testProp;
    }

    @Inject
    @Property(keys = "i", defaultValue = "1")
    private int i;

    @Inject
    @Properties
    private EnhancedMap properties;

    public EnhancedMap getProperties() {
        return properties;
    }

    private boolean bp;

    private String s;

    @Inject
    public void setS(@Property(required = false) String s) {
        this.s = s;
    }

    public String getS() {
        return s;
    }

    @Inject
    public void setSome(@Property Boolean bprop) {

        bp = bprop;
    }

    public String getTestProp() {
        return testProp;
    }

    public File getFprop() {
        return fprop;
    }

    public boolean isBprop() {
        return bprop;
    }

    public String getZprop() {
        return zprop;
    }

    public int getI() {
        return i;
    }

    public boolean isBp() {
        return bp;
    }
}
