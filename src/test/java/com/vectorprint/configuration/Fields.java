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

import com.vectorprint.configuration.annotation.Feature;
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.configuration.decoration.Changes;
import com.vectorprint.configuration.decoration.Observable;
import com.vectorprint.configuration.decoration.Observer;
import com.vectorprint.configuration.decoration.ReadonlyProperties;
import java.net.URL;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Fields implements Observer {
   
   @Setting(keys = "b")
   private boolean b;
   @Setting(keys = "B")
   private final Boolean B = true;
   @Setting(keys = "nodefault")
   private Boolean NODEF;
   @Setting(keys = "u")
   private URL u;
   @Setting(keys = "f")
   private Float F;
   @Setting(keys = "ff")
   private float[] ff;
   @SettingsField(
       observable = true,
       urls = {"src/test/resources/config/chart.properties"},
       features = {
          @Feature(clazz = ReadonlyProperties.class)
       }
   )
   @Setting(keys = "settings")
   private EnhancedMap settings;


   public Boolean getB() {
      return B;
   }

   public URL getU() {
      return u;
   }

   public Float getF() {
      return F;
   }

   public boolean isB() {
      return b;
   }

   public float[] getFf() {
      return ff;
   }

   public EnhancedMap getSettings() {
      return settings;
   }

   public void setF(Float F) {
      this.F = 50f;
   }

   @Override
   public void update(Observable object, Changes changes) {
      System.out.println(changes);
   }
   
}
