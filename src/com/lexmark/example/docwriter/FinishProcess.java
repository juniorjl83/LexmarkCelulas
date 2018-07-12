package com.lexmark.example.docwriter;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import com.lexmark.example.docwriter.singleton.Id;
import com.lexmark.example.docwriter.singleton.Trabajo;

public class FinishProcess extends Thread
{

   private ArrayList lstThreads;
   private ArrayList fileNames;
   private Boolean isFinish = Boolean.FALSE;
   private Id id;
   private MemoryManagerInstance memoryManager;

   public FinishProcess(ArrayList lstThreads, ArrayList fileNames, Id id,
      MemoryManagerInstance memoryManager)
   {
      super();
      this.lstThreads = lstThreads;
      this.fileNames = fileNames;
      this.id = id;
      this.memoryManager = memoryManager;
   }

   public synchronized void run()
   {
      int numThreadFinish;

      do
      {
         numThreadFinish = 0;
         for (int i = 0; i < lstThreads.size(); i++)
         {
            if (lstThreads.get(i) instanceof WriteMultipleFiles)
            {
               if (((WriteMultipleFiles) lstThreads.get(i)).isFinish()
                     .booleanValue())
               {
                  numThreadFinish++;
               }
            }
            else if (lstThreads.get(i) instanceof WriteOneFile)
            {
               if (((WriteOneFile) lstThreads.get(i)).isFinish().booleanValue())
               {
                  numThreadFinish++;
               }
            }
            else
            {
               if (((WriteLog) lstThreads.get(i)).isFinish().booleanValue())
               {
                  numThreadFinish++;
               }
            }
         }
      } while (numThreadFinish < lstThreads.size());

      // delete files
      for (int i = 0; i < fileNames.size(); i++)
      {
         File fileToDelete = (File) fileNames.get(i);
         Activator.getLog()
               .info("Borrando archivo..... " + fileToDelete.getName());
         fileToDelete.delete();
      }
      Map imagesOnDisk = Trabajo.getInstance();
      Activator.getLog().info("Removiendo trabajo..... " + id);
      imagesOnDisk.remove(id);

      if (memoryManager != null)
      {
         Activator.getLog().info("Liberando memoria.");
         memoryManager.releaseMemory();
      }

   }

   public Boolean isFinish()
   {
      return isFinish;
   }

}
