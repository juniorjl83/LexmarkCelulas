package com.lexmark.example.docwriter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.lexmark.prtapp.image.DocumentWriter;
import com.lexmark.prtapp.image.Image;
import com.lexmark.prtapp.image.ImageException;
import com.lexmark.prtapp.image.ImageFactory;
import com.lexmark.prtapp.memoryManager.MemoryManager;
import com.lexmark.prtapp.smbclient.SmbClient;
import com.lexmark.prtapp.util.AppLogRef;
import com.lexmark.ui.Constants;

public class WriteOneFile extends Thread
{
   private DocumentWriter dw = null;
   private ImageFactory imageFactory;
   private SmbClient client = null;
   private int fileFormat;
   private String fileName = null;
   private String filePassword;
   private ArrayList lstImages = null;
   private Boolean isDateMark;
   private AppLogRef log;
   private Boolean isFinish = Boolean.FALSE;
   private MemoryManagerInstance memoryManager;
   private boolean isLastFile = false;

   public WriteOneFile(DocumentWriter dw, ImageFactory imageFactory,
         AppLogRef log, SmbClient client, int fileFormat, String fileName,
         String filePassword, ArrayList lstImages, Boolean isDateMark,
         MemoryManagerInstance memoryManager, boolean isLastFile)
   {
      super();
      this.dw = dw;
      this.imageFactory = imageFactory;
      this.log = log;
      this.client = client;
      this.fileFormat = fileFormat;
      this.fileName = fileName;
      this.filePassword = filePassword;
      this.lstImages = lstImages;
      this.isDateMark = isDateMark;
      this.memoryManager = memoryManager;
      this.isLastFile = isLastFile;
   }

   public void run()
   {
      dw.setConsumer(
            new FileShareHandler(client, fileFormat, fileName, isDateMark));
      // dw.setCompression(Constants.eZLIB);

      if (fileFormat == Constants.e_SECURE_PDF)
      {
         dw.setPassword(filePassword, 2);
      }
      Image img = null;
      try
      {
         for (int i = 0; i < lstImages.size(); i++)
         {
            log.info("lee imagen: " + i);

            File file = (File) lstImages.get(i);
            img = imageFactory.newImage(file);
            dw.write(img);
            log.info("escribe imagen: " + i + " en one file");
            img.freeResources();
            img = null;

            if (isLastFile && file != null)
            {
               file.delete();
            }
         }
      }
      catch (Exception e)
      {
         log.info("error WriteOneFile: " + e.getMessage());
      }
      finally
      {
         dw.close();
         if (img != null)
         {
            img.freeResources();
            img = null;
         }
         if (memoryManager.getNativeMem() != null && isLastFile)
            memoryManager.releaseMemory();
      }

      isFinish = Boolean.TRUE;
      log.info("finaliza hilo: " + this);
   }

   public Boolean isFinish()
   {
      return isFinish;
   }

}
