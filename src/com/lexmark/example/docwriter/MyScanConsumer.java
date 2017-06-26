package com.lexmark.example.docwriter;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import com.lexmark.prtapp.image.Image;
import com.lexmark.prtapp.image.ImageFactory;
import com.lexmark.prtapp.image.JpegImageWriter;
import com.lexmark.prtapp.image.TiffImageWriter;
import com.lexmark.prtapp.image.TiffImageWriter.TiffCompression;
import com.lexmark.prtapp.scan.ScanConsumer;
import com.lexmark.prtapp.scan.ScanData;
import com.lexmark.prtapp.storagedevice.StorageDevice;

/**
 * Consume the data from the scanner.  This will create an Image from each
 * page, and then the Image can be used to create our big PDF.  Of course,
 * a PDF could be created directly by just setting the file format to PDF.
 * This example demonstrates how to use the document writer, which would
 * be necessary if anything is done to the pages, such as de-skew or some
 * other image manipulation.
 */
public class MyScanConsumer implements ScanConsumer
{
   private ImageFactory imageFactory;
   private StorageDevice disk;
   private boolean isFinished = false;
   private Object synch = new Object();
   private MemoryManagerInstance memoryManager;
   
   public ArrayList imagesOnDisk = new ArrayList();
   
   /**
    * Constructor.
    * @param imageFactory Used in the consume method to create an Image for each
    * scan file.
    * @param memoryManagerInstance 
    * @param docWriterFactory Used in the consume method to create the document (PDF)
    * from the image files.
    */
   public MyScanConsumer(ImageFactory imageFactory, StorageDevice disk, MemoryManagerInstance memoryManager)
   {
      this.imageFactory = imageFactory;
      this.disk = disk;
      this.memoryManager = memoryManager;
   }

   /* (non-Javadoc)
    * @see com.lexmark.prtapp.scan.ScanConsumer#consume(com.lexmark.prtapp.scan.ScanData)
    */
   public void consume(ScanData data)
   {
      isFinished = false;
      InputStream is = null;
      Image currentImage = null;
      int n = 0;
      try
      {
         while((is = data.nextImageFile()) != null)
         {
            n++;
            Activator.getLog().info("MyScanConsumer.consume: received image # " + n);
            
            currentImage = imageFactory.newImage(is);
            
            if(!currentImage.isBlank(180)){
            
               File file = new File(disk.getRootPath(), "scan" + n + ".tif");
               TiffImageWriter jw = new TiffImageWriter(TiffImageWriter.G4, TiffImageWriter.OVERWRITE);
               currentImage.write(file, jw);
               imagesOnDisk.add(file);

               Activator.getLog().info("MyScanConsumer.consume: saved image as a tiff here: " + file);

               currentImage.freeResources();
               currentImage = null;
               
            }
         }
      }
      catch(Exception e)
      {
         // TODO: we could kill off the doc writer and delete anything it may have
         // written, since it would just be a partial file
         Activator.getLog().info("Problem creating document", e);
         cleanUpOldFiles();
         if(memoryManager.getNativeMem() != null) memoryManager.releaseMemory();
         
      }
      finally
      {
         if(currentImage != null) currentImage.freeResources();
         
      }
      
      // This will notify anyone who has called waitForComplete that we're finished processing.
      Activator.getLog().info("MyScanConsumer.consume: Done consuming! ");
      synchronized(synch)
      {
         isFinished = true;
         synch.notifyAll();
      }
   }

   /**
    * This will block until the consumer has finished doing its job.  This is intended
    * to be called from a different thread - in this example, it is called from within the
    * wait message prompt to ensure the processing is complete before moving on.
    * @return true if finished, false if it was interrupted
    */
   public boolean waitForComplete()
   {
      synchronized(synch)
      {
         if(isFinished == false)
         {
            try
            {
               synch.wait();
            }
            catch(InterruptedException e)
            {
               Activator.getLog().info("Thread was interrupted!");
            }
         }
      }
      
      return isFinished;
   }
   
   private void cleanUpOldFiles()
   {
      File root = disk.getRootPath();
      String[] files = root.list();
      for (int i = 0; i < files.length; i++)
      {
         File fileToDelete = new File(root, files[i]);
         fileToDelete.delete();
      }
   }
}
