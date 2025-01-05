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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 *
 * @author eduard
 */
@Singleton
public class TestBean {
    @Inject @Property
    private String fieldprop;
    @Inject @Property(updatable = false)
    private String fieldpropro;
    private String parampropkey;
    private String paramprop;

    public String getFieldprop() {
        return fieldprop;
    }

    public void setFieldprop(String fieldprop) {
        this.fieldprop = fieldprop;
    }

    public String getFieldpropro() {
        return fieldpropro;
    }

    public void setFieldpropro(String fieldpropro) {
        this.fieldpropro = fieldpropro;
    }

    public String getParampropkey() {
        return parampropkey;
    }

    @Inject @Property(keys = "key")
    public void setParampropkey(String parampropkey) {
        this.parampropkey = parampropkey;
    }

    public String getParamprop() {
        return paramprop;
    }

    @Inject @Property
    public void setParamprop(String paramprop) {
        this.paramprop = paramprop;
    }
}
