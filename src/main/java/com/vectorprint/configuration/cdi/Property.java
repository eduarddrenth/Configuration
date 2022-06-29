/*
 * Copyright 2015 VectorPrint.
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

/*
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 VectorPrint
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

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Use together with @Inject, {@link CDIProperties} will provide values;
 * @author Eduard Drenth at VectorPrint.nl
 */
@Qualifier
@Retention(RUNTIME)
@Target({FIELD,ElementType.METHOD,ElementType.PARAMETER})
public @interface Property {
   
    /**
     * one or more keys to lookup a setting, the first setting found will be used.
     * @return 
     */
   @Nonbinding String[] keys() default {};

    /**
     * NOTE when a default value is set this setting has no effect
     * @return
     */
   @Nonbinding boolean required() default true;

    /**
     * You can set one or more default values as Strings which will be converted to the correct type when that type is supported.
     * @return
     */
   @Nonbinding String[] defaultValue() default {};

   /**
    * When true (default) the property will be updated when the property source changes
    * @return 
    */
   @Nonbinding boolean updatable() default true;

}
