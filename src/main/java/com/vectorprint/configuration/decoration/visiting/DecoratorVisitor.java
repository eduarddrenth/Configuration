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

package com.vectorprint.configuration.decoration.visiting;

import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.ObservableProperties;

/**
 * You can define a visitor that allows you to access a certain type of {@link EnhancedMap} in the stack and
 * call its methods. This way you can for example add properties from a url or add observables
 * after the stack of properties is defined.
 * @param <E>
 * @see AbstractPropertiesDecorator#accept(com.vectorprint.configuration.decoration.visiting.DecoratorVisitor) 
 * @see SettingsField
 * @see SettingsAnnotationProcessor
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface DecoratorVisitor<E extends EnhancedMap> {
   
   Class<E> getClazzToVisit();
   
   /**
    * Access settings, see {@link AbstractPropertiesDecorator#accept(com.vectorprint.configuration.decoration.visiting.DecoratorVisitor) }.
    * @param e
    * @see SettingsField
    * @see ObservableProperties#addObserver(com.vectorprint.configuration.decoration.Observer) 
    */
   void visit(E e);

}
