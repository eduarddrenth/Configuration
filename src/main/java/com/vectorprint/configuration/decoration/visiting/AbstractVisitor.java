/*
 * Copyright 2018 VectorPrint.
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
package com.vectorprint.configuration.decoration.visiting;

import com.vectorprint.ClassHelper;
import com.vectorprint.configuration.EnhancedMap;

/**
 *
 * @author eduard
 */
public abstract class AbstractVisitor<E extends EnhancedMap> implements DecoratorVisitor<E>{
    
    private Class clazz = ClassHelper.findParameterClass(0, getClass(), DecoratorVisitor.class);
    
    @Override
    public boolean shouldVisit(EnhancedMap e) {
        return clazz.isAssignableFrom(e.getClass());
    }
    
}
