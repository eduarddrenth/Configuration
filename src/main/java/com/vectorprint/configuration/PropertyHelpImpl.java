
package com.vectorprint.configuration;

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


import java.io.Serial;

public class PropertyHelpImpl implements PropertyHelp {

   @Serial
   private static final long serialVersionUID = 1;
   private String type = "unknown type", explanation = "unknown";

   public PropertyHelpImpl(String type, String explanation) {
      this.type = type;
      this.explanation = explanation;
   }

   public PropertyHelpImpl(String explanation) {
      this.explanation = explanation;
   }

   public PropertyHelpImpl() {
   }

   @Override
   public String getExplanation() {
      return explanation;
   }

   @Override
   public String getType() {
      return type;
   }

}
