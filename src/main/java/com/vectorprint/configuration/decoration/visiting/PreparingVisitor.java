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

import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.preparing.PrepareKeyValue;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class PreparingVisitor implements DecoratorVisitor<PreparingProperties>{

   private final PrepareKeyValue pkv;

   public PreparingVisitor(PrepareKeyValue pkv) {
      this.pkv = pkv;
   }

   @Override
   public Class<PreparingProperties> getClazzToVisit() {
      return PreparingProperties.class;
   }

   /**
    * Call {@link PreparingProperties#addObserver(com.vectorprint.configuration.observing.PrepareKeyValue) }.
    * @param e 
    */
   @Override
   public void visit(PreparingProperties e) {
         e.addObserver(pkv);
   }

}
