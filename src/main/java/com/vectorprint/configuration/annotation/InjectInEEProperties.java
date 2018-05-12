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
package com.vectorprint.configuration.annotation;

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

import com.vectorprint.configuration.decoration.EEProperties;
import java.lang.annotation.ElementType;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Use together with @Produce and {@link InjectionPoint} to provide {@link EEProperties#EEProperties(com.vectorprint.configuration.EnhancedMap) } with properties.
 * @author Eduard Drenth at VectorPrint.nl
 */
@Qualifier
@Retention(RUNTIME)
@Target({FIELD,ElementType.METHOD,ElementType.CONSTRUCTOR})
public @interface InjectInEEProperties {
   
    /**
     * a key to use in your EnhancedMap producing method
     * @return 
     */
   @Nonbinding String key() default "";
   
}
