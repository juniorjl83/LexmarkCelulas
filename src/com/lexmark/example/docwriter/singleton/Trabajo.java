package com.lexmark.example.docwriter.singleton;

import java.util.HashMap;
import java.util.Map;

public class Trabajo
{
   private static Map instance = null;

   public static Map getInstance() {
      if (instance == null){
         instance = new HashMap();
      }
      return instance;
   }
}
