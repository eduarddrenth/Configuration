
package com.vectorprint.configuration.decoration;

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


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Changes {

   private List<String> added=Collections.emptyList(), changed = Collections.emptyList(), deleted = Collections.emptyList();

   public Changes(List<String> added, List<String> changed, List<String> deleted) {
      if (added != null) {
         this.added = Collections.unmodifiableList(added);
      }
      if (changed != null) {
         this.changed = Collections.unmodifiableList(changed);
      }
      if (deleted != null) {
         this.deleted = Collections.unmodifiableList(deleted);
      }
   }

   public List<String> getAdded() {
      return added;
   }

   public List<String> getChanged() {
      return changed;
   }

   public List<String> getDeleted() {
      return deleted;
   }

   public static List<String> fromKeys(String... keys) {
      return Arrays.asList(keys);
   }

   @Override
   public String toString() {
      return "Changes{" + "added=" + added + ", changed=" + changed + ", deleted=" + deleted + '}';
   }
   
   

}
