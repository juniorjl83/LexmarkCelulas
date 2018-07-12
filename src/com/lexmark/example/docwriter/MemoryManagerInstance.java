package com.lexmark.example.docwriter;

import com.lexmark.prtapp.memoryManager.Memory;
import com.lexmark.prtapp.memoryManager.MemoryManager;
import com.lexmark.prtapp.memoryManager.NativeMemory;

public class MemoryManagerInstance
{
   private NativeMemory nativeMem = null;
   private MemoryManager memoryManager = null;
   Memory javaMem = null;

   public MemoryManagerInstance(NativeMemory nativeMem, Memory javaMem,
         MemoryManager memoryManager)
   {
      this.nativeMem = nativeMem;
      this.javaMem = javaMem;
      this.memoryManager = memoryManager;
   }

   public void releaseMemory()
   {
      try
      {
         if (nativeMem != null && memoryManager != null)
         {
            Activator.getLog().info("liberando memoria nativa!");
            memoryManager.freeMemory(nativeMem);
         }
         if (javaMem != null && memoryManager != null) 
         {
            Activator.getLog().info("liberando memoria java!");
            memoryManager.freeMemory(javaMem);
         }
      }
      catch (Exception e)
      {
         Activator.getLog().info("Error liberando memoria! " + e.getMessage());
      }

   }
}
