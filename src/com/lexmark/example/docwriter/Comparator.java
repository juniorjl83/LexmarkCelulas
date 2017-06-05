package com.lexmark.example.docwriter;

public class Comparator implements java.util.Comparator
{

   public int compare(Object o1, Object o2)
   {
      return ((ComboCelula) o1).name.compareTo(((ComboCelula) o2).name);
   }

}
