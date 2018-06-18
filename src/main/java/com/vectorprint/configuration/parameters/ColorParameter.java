/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.parameters;

/*
 * #%L
 * VectorPrintReport
 * %%
 * Copyright (C) 2012 - 2013 VectorPrint
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
import java.awt.Color;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ColorParameter extends ParameterImpl<Color> {

   public ColorParameter(String key, String help) {
      super(key, help,Color.class);
   }
   
   @Override
   protected String valueToString(Color value) {
      return value != null ? '#' + Integer.toHexString(value.getRGB()).substring(2) : "";
   }

    @Override
    public Parameter<Color> clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
