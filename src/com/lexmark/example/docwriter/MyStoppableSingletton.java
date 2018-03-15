package com.lexmark.example.docwriter;

public class MyStoppableSingletton
{
   private static MyStoppable instance = null;
   
   public static MyStoppable getInstance(){
      if (instance == null){
         instance = new MyStoppable();
      }
      return instance;
   }
}
