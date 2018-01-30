package com.lexmark.example.docwriter;

import java.io.File;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.lexmark.example.docwriter.singleton.Id;
import com.lexmark.example.docwriter.singleton.Trabajo;
import com.lexmark.prtapp.image.Image;
import com.lexmark.prtapp.image.ImageFactory;
import com.lexmark.prtapp.image.JpegImageWriter;
import com.lexmark.prtapp.image.TiffImageWriter;
import com.lexmark.prtapp.memoryManager.MemoryManager;
import com.lexmark.prtapp.smbclient.SmbClient;
import com.lexmark.prtapp.util.AppLogRef;

public class WriteMultipleFiles extends Thread
{
   private ImageFactory imageFactory;
   private SmbClient client = null;
   private ArrayList lstImages = null;
   private Boolean isDateMark;
   private String ext;
   private AppLogRef log;
   private Boolean isFinish = Boolean.FALSE;
   private MemoryManagerInstance memoryManager;
   private boolean isLastFile = false;
   private Id id;

   public WriteMultipleFiles(ImageFactory imageFactory, SmbClient client,
         ArrayList lstImages, Boolean isDateMark, String ext,
         AppLogRef log, MemoryManagerInstance memoryManager, boolean isLastFile, Id id)
   {
      super();
      this.imageFactory = imageFactory;
      this.client = client;
      this.lstImages = lstImages;
      this.isDateMark = isDateMark;
      this.ext = ext;
      this.log = log;
      this.memoryManager = memoryManager;
      this.isLastFile = isLastFile;
      this.id = id;
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
                     id.getFilename() + dateMark + "_" + i + ext);
               TiffImageWriter jw = new TiffImageWriter(TiffImageWriter.G4, TiffImageWriter.OVERWRITE);
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
         Map imagesOnDisk = Trabajo.getInstance();
         log.info("trabajos en memoria: " + imagesOnDisk.size());
         log.info("Borrando imagenes de memoria: " + this);
         imagesOnDisk.remove(id);
         log.info("trabajos en memoria: " + imagesOnDisk.size());       
         
         
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
