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

package com.vectorprint.configuration;

import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.configuration.parameters.IntArrayParameter;
import com.vectorprint.configuration.parameters.ParameterizableImpl;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.Parameters;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
   @Parameters(
       parameters = {
          @Param(
              clazz = BooleanParameter.class,
              key = "b"
          ),
          @Param(
              clazz = ColorParameter.class,
              key = "c"
          ),
          @Param(
              clazz = IntArrayParameter.class,
              key = "a"
          ),
          @Param(
              clazz = EnumParam.class,
              key = "e"
          )
       }
   )
public class P extends ParameterizableImpl {
      
      @Setting(keys = "staticBoolean")
      private static boolean ff;

   public P() {
      addParameter(new StringParameter("s", "help").setDefault("v"),P.class);
   }

   public static boolean isFf() {
      return ff;
   }

}
