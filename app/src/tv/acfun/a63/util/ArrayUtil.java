/*
 * Copyright (C) 2013 YROM.NET
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
package tv.acfun.a63.util;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;


public class ArrayUtil {
    public static long[] toLongArray(List<Long> list){
        if(list==null || list.isEmpty()) return null;
        long[] arr = new long[list.size()];
        for(int i=0;i<list.size();i++){
            arr[i] = list.get(i).longValue();
        }
        return arr;
    }
    public static <E> ArrayList<E> newArrayList(){
        return new ArrayList<E>();
    }
    
    public static <E> boolean validate(List<E> list){
        return list != null && !list.isEmpty();
    }
    public static <E> SparseArray<E> putAll(SparseArray<E> source, SparseArray<E> dest){
        if(dest == null)
            dest = source;
        else if(source != null)
            for(int i=0 ; i<source.size();i++){
                int key = source.keyAt(i);
                E value = source.valueAt(i);
                dest.put(key, value);
            }
        return dest;
    }
    
    public static ArrayList<Integer> asList(int... arr){
        ArrayList<Integer> list = new ArrayList<Integer>(arr.length);
        for(int i=0;i<arr.length;i++){
            list.add(Integer.valueOf(arr[i]));
        }
        return list;
        
    }
}
