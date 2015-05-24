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
package com.vectorprint.configuration.binding;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface CachingFactory {

   public static class CacheKey {

      final Class a, b;

      public CacheKey(Class a, Class b) {
         this.a = a;
         this.b = b;
      }

      @Override
      public int hashCode() {
         int hash = 5;
         return hash;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }
         if (getClass() != obj.getClass()) {
            return false;
         }
         final CacheKey other = (CacheKey) obj;
         if (this.a != other.a && (this.a == null || !this.a.equals(other.a))) {
            return false;
         }
         if (this.b != other.b && (this.b == null || !this.b.equals(other.b))) {
            return false;
         }
         return true;
      }

   }

}
