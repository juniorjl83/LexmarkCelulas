package com.lexmark.example.docwriter;

import com.lexmark.prtapp.memoryManager.MemoryManager;
import com.lexmark.prtapp.memoryManager.NativeMemory;

public class MemoryManagerInstance
{
   private NativeMemory nativeMem = null;
   private MemoryManager memoryManager = null;
   
   public MemoryManagerInstance(NativeMemory nativeMem, MemoryManager memoryManager){
      this.nativeMem = nativeMem;
      this.memoryManager = memoryManager;
   }

   public NativeMemory getNativeMem()
   {
      return nativeMem;
   }

   public void setNativeMem(NativeMemory nativeMem)
   {
      this.nativeMem = nativeMem;
   }

   public MemoryManager getMemoryManager()
   {
      return memoryManager;
   }

   public void setMemoryManager(MemoryManager memoryManager)
   {
      this.memoryManager = memoryManager;
   }
   
   public void releaseMemory(){
      if (nativeMem != null && memoryManager != null){
         Activator.getLog().info("liberando memoria!");
         memoryManager.freeMemory(nativeMem);
      }
         
   }
}
