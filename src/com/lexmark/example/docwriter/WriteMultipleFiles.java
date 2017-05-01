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
   
   public WriteMultipleFiles(ImageFactory imageFactory, SmbClient client,
         ArrayList lstImages, String fileName, Boolean isDateMark, String ext,
         AppLogRef log)
   {
      super();
      this.imageFactory = imageFactory;
      this.client = client;
      this.lstImages = lstImages;
      this.fileName = fileName;
      this.isDateMark = isDateMark;
      this.ext = ext;
      this.log = log;
   }

   public void run()
   {
      String dateMark = "";
      if (isDateMark.booleanValue()){
         log.info("entra marca fecha");
         dateMark = dateMark + "-";
         SimpleDateFormat sdf = new SimpleDateFormat("MMddyyHHmmss");
         Date date = new Date();
         dateMark = dateMark +  sdf.format(date);
         Activator.getLog().info("fecha texto: " + dateMark);
      }
      
      for(int i = 0; i < lstImages.size(); i++)
      {  
         try
         {  
            File file = (File)lstImages.get(i);
            Image img = imageFactory.newImage(file);
            log.info("lee imagen: " + i);
            
            OutputStream os = client.getOutputStream("",  fileName + dateMark + "-" + i + ext);
            JpegImageWriter jw = new JpegImageWriter(80, true);
            img.write(os, jw);
            log.info("escribe imagen: " + i);
            os.close();
            os = null;
            img.freeResources();
            img = null;
         }
         catch (Exception e)
         {
            log.info("error WriteMultipleOneFile: " + e.getMessage());
         }
      }
      isFinish = Boolean.TRUE;
      log.info("finaliza hilo: " + this);
   }

   public Boolean isFinish()
   {
      return isFinish;
   }
}
