package com.lexmark.example.docwriter;

import java.io.File;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.lexmark.prtapp.image.Image;
import com.lexmark.prtapp.image.ImageFactory;
import com.lexmark.prtapp.image.JpegImageWriter;
import com.lexmark.prtapp.memoryManager.MemoryManager;
import com.lexmark.prtapp.smbclient.SmbClient;
import com.lexmark.prtapp.util.AppLogRef;

public class WriteMultipleFiles extends Thread
{
   private ImageFactory imageFactory;
   private SmbClient client = null;
   private ArrayList lstImages = null;
   private String fileName = null;
   private Boolean isDateMark;
   private String ext;
   private AppLogRef log;
   private Boolean isFinish = Boolean.FALSE;
   private MemoryManagerInstance memoryManager;
   private boolean isLastFile = false;

   public WriteMultipleFiles(ImageFactory imageFactory, SmbClient client,
         ArrayList lstImages, String fileName, Boolean isDateMark, String ext,
         AppLogRef log, MemoryManagerInstance memoryManager, boolean isLastFile)
   {
      super();
      this.imageFactory = imageFactory;
      this.client = client;
      this.lstImages = lstImages;
      this.fileName = fileName;
      this.isDateMark = isDateMark;
      this.ext = ext;
      this.log = log;
      this.memoryManager = memoryManager;
      this.isLastFile = isLastFile;
   }

   public synchronized void run()
   {
      String dateMark = "";
      if (isDateMark.booleanValue())
      {
         log.info("entra marca fecha");
         dateMark = dateMark + "_";
         SimpleDateFormat sdf = new SimpleDateFormat("MMddyyHHmmss");
         Date date = new Date();
         dateMark = dateMark + sdf.format(date);
         Activator.getLog().info("fecha texto: " + dateMark);
      }
      Image img = null;
      try
      {
         for (int i = 0; i < lstImages.size(); i++)
         {
            try
            {
               File file = (File) lstImages.get(i);
               log.info("lee imagen: " + file.getName());
               img = imageFactory.newImage(file);

               OutputStream os = client.getOutputStream("",
                     fileName + dateMark + "_" + i + ext);
               JpegImageWriter jw = new JpegImageWriter(80, true);
               img.write(os, jw);
               log.info("escribe imagen: " + i);
               os.close();
               os = null;
               if (img != null)
               {
                  img.freeResources();
                  img = null;
               }
               if (isLastFile && file != null)
               {
                  file.delete();
               }
            }
            catch (Exception e)
            {
               log.info("error WriteMultipleOneFile: " + e.getMessage());
            }
         }

         isFinish = Boolean.TRUE;
         log.info("finaliza hilo write multiple files: " + this);
      }
      catch (Exception e)
      {
         log.info("error WriteMultipleOneFile: " + e.getMessage());
      }
      finally
      {
         if (img != null)
         {
            img.freeResources();
            img = null;
         }
         if (memoryManager.getNativeMem() != null && isLastFile)
            memoryManager.releaseMemory();
      }
   }

   public Boolean isFinish()
   {
      return isFinish;
   }
}
