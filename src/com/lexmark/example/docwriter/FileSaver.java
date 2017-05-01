package com.lexmark.example.docwriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.lexmark.prtapp.scan.ScanConsumer;
import com.lexmark.prtapp.scan.ScanData;

/**
 * This consumer is required for using DocumentWriter.  This one simply takes the data
 * coming from the document writer and saves it to a file.  We assume there will be just
 * one file, since we force the format to PDF.
 */
public class FileSaver implements ScanConsumer
{
   // This is the file to which the data will be saved.  We assume we are
   // writing a single file here.
   private File saveFile = null;
   
   public FileSaver(File file)
   {
      saveFile = file;
   }

   /* (non-Javadoc)
    * @see com.lexmark.prtapp.scan.ScanConsumer#consume(com.lexmark.prtapp.scan.ScanData)
    */
   public void consume(ScanData data)
   {
      // Technically, there could be more than one image, but that will never be true for
      // the file formats typically used with DocumentWriter (XPS, PDF, secure PDF)
      Activator.getLog().debug("FileSaver.consume: Getting our one image file.");
      FileOutputStream fos = null;
      try
      {
         fos = new FileOutputStream(saveFile);
         InputStream is = data.nextImageFile();

         byte[] buff = new byte[16384];
         int bytesRead;
         
         while((bytesRead = is.read(buff)) != -1)
         {
            fos.write(buff, 0, bytesRead);
         }
      }
      catch(Exception e)
      {
         Activator.getLog().debug("The document writer had a problem", e);
      }
      finally
      {
         // Note: scan data stream gets closed automatically.
         try
         {
            if(fos != null) fos.close();
         }
         catch(IOException ignore)
         {
            Activator.getLog().debug("Problem closing streams", ignore);
         }
      }
      Activator.getLog().debug("FileSaver.consume: Finished!");
   }
   
}
