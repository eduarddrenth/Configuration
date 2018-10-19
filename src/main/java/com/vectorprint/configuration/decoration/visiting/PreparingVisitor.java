
package com.vectorprint.configuration.decoration.visiting;

/*-
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 - 2018 VectorPrint
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

import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.preparing.PrepareKeyValue;

public class PreparingVisitor extends AbstractVisitor<PreparingProperties> {

   private final PrepareKeyValue pkv;

   public PreparingVisitor(PrepareKeyValue pkv) {
      this.pkv = pkv;
   }

   /**
    * Call {@link PreparingProperties#addObserver(com.vectorprint.configuration.observing.PrepareKeyValue) }.
    *
    * @param e
    */
   @Override
   public void visit(PreparingProperties e) {
      e.addObserver(pkv);
   }

}
