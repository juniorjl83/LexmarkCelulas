package com.lexmark.example.docwriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.lexmark.example.docwriter.singleton.Id;
import com.lexmark.example.docwriter.singleton.Trabajo;
import com.lexmark.prtapp.image.DocumentWriter;
import com.lexmark.prtapp.image.DocumentWriterFactory;
import com.lexmark.prtapp.image.ImageFactory;
import com.lexmark.prtapp.profile.BasicProfileContext;
import com.lexmark.prtapp.settings.SettingDefinitionMap;
import com.lexmark.prtapp.settings.SettingsAdmin;
import com.lexmark.prtapp.smbclient.AuthOptions;
import com.lexmark.prtapp.smbclient.SmbClient;
import com.lexmark.prtapp.smbclient.SmbClientException;
import com.lexmark.prtapp.smbclient.SmbClientService;
import com.lexmark.prtapp.smbclient.SmbConfig.ConfigBuilder;
import com.lexmark.prtapp.std.prompts.WaitPrompt;
import com.lexmark.prtapp.std.prompts.WaitPrompt.Stoppable;
import com.lexmark.prtapp.storagedevice.StorageDevice;
import com.lexmark.ui.Constants;

/**
 * Helper class that does the document writer stuff. The array list of file
 * names comes from the scan consumer. We will create an image out of each of
 * those files, and then stuff it into the document writer.
 */
public class MyStoppable implements Stoppable
{
   Log lineLog = null;
   MyScanConsumer myScanConsumer = null;
   BasicProfileContext context = null;
   MemoryManagerInstance memoryManager = null;
   DocumentWriterFactory docWriterFactory = null;
   ImageFactory imageFactory = null;
   StorageDevice disk = null;
   SettingsAdmin settingsAdmin = null;
   SmbClientService smbClientService = null;
   
   ArrayList lstServers;
   Boolean isFinish = Boolean.TRUE;
   Boolean isMultiTiff = Boolean.FALSE;
   Boolean isDateMark = Boolean.FALSE;
   
   

   public MyStoppable()
   {
      lineLog = new Log();
   }

   public MyStoppable withMyScanConsumer(MyScanConsumer myScanConsumer)
   {
      if ( this.myScanConsumer == null )
         this.myScanConsumer = myScanConsumer;
      return this;
   }

   public MyStoppable withServers(ArrayList lstServers)
   {
      do {
         Activator.getLog().info(
               "withServers:: Esperando a que acabe el proceso de guardado.");
      } while (this.isFinish == Boolean.FALSE);
      this.lstServers = lstServers;   
      return this;
   }

   public MyStoppable withBasicProfileContext(BasicProfileContext context)
   {
      if ( this.context == null )
         this.context = context;
      return this;
   }

   public MyStoppable withMemoryManagerInstance(
         MemoryManagerInstance memoryManager)
   {
      if ( this.memoryManager == null )
         this.memoryManager = memoryManager;
      return this;
   }

   public MyStoppable withDocumentWriterFactory(
         DocumentWriterFactory docWriterFactory)
   {
      if ( this.docWriterFactory == null )
         this.docWriterFactory = docWriterFactory;
      return this;
   }

   public MyStoppable withStorageDevice(StorageDevice disk)
   {
      if ( this.disk == null )
         this.disk = disk;
      return this;
   }

   public MyStoppable withImageFactory(ImageFactory imageFactory)
   {
      if ( this.imageFactory == null )
         this.imageFactory = imageFactory;
      return this;
   }

   public MyStoppable withIsMultiTiff(Boolean isMultiTiff)
   {
      do {
         Activator.getLog().info(
               "withIsMultiTiff:: Esperando a que acabe el proceso de guardado.");
      } while (this.isFinish == Boolean.FALSE);
      this.isMultiTiff = isMultiTiff;
      return this;
   }

   public MyStoppable withIsDateMark(Boolean isDateMark)
   {
      do {
         Activator.getLog().info(
               "withIsMultiTiff:: Esperando a que acabe el proceso de guardado.");
      } while (this.isFinish == Boolean.FALSE);
      this.isDateMark = isDateMark;
      return this;
   }

   public MyStoppable withSettingsAdmin(SettingsAdmin settingsAdmin)
   {
      if ( this.settingsAdmin == null )
         this.settingsAdmin = settingsAdmin;
      return this;
   }

   public MyStoppable withSmbClientService(SmbClientService smbClientService)
   {
      if ( this.smbClientService == null )
         this.smbClientService = smbClientService;
      return this;
   }

   public synchronized void run()
   {
      this.isFinish = Boolean.FALSE;
      Activator.getLog().info("entra run stopable");
      myScanConsumer.waitForComplete();
      Map imagesOnDisk = Trabajo.getInstance();
      Iterator it = imagesOnDisk.entrySet().iterator();
      while (it.hasNext())
      {
         boolean isLastFile = false;
         Map.Entry entry = (Map.Entry) it.next();
         Id id = (Id) entry.getKey();
         Activator.getLog().info("Procesando trabajo..... " + id);
         ArrayList fileNames = (ArrayList) entry.getValue();
         String fileName = id.getFilename();
         String fileType = id.getFileType();
         String filePassword = id.getFilePassword();

         Activator.getLog().info(
               "Archivos en memoria :: " + disk.getRootPath().list().length);
         
         ArrayList lstThreads = new ArrayList();
         
         for (int j = 0; j < lstServers.size(); j++)
         {
            if ((j + 1) == lstServers.size())
            {
               isLastFile = true;
               Activator.getLog().info("is last file");
            }
            SmbClient client = ((InfoServer) lstServers.get(j)).getClient();

            if (fileType.equals("JPG"))
            {
               Activator.getLog().info("jpg archivo, servidor " + j);
               WriteMultipleFiles mf = new WriteMultipleFiles(imageFactory,
                     client, fileNames, isDateMark, ".jpg", Activator.getLog(),
                     memoryManager, isLastFile, id);
               lstThreads.add(mf);
               mf.start();
            }
            else if (isMultiTiff.booleanValue())
            {
               Activator.getLog().info("multitiff archivo, servidor " + j);
               WriteMultipleFiles mf = new WriteMultipleFiles(imageFactory,
                     client, fileNames, isDateMark, ".tif", Activator.getLog(),
                     memoryManager, isLastFile, id);
               lstThreads.add(mf);
               mf.start();
            }
            else
            {
               Activator.getLog().info("un archivo, servidor " + j);
               int fileFormat = getFileFormat(fileType);
               DocumentWriter dw = docWriterFactory
                     .newDocumentWriter(fileFormat);
               WriteOneFile of = new WriteOneFile(dw, imageFactory,
                     Activator.getLog(), client, fileFormat, filePassword,
                     fileNames, isDateMark, memoryManager, isLastFile, id);
               lstThreads.add(of);
               of.start();
            }
         }
         Activator.getLog().info("began lg ");
         SettingDefinitionMap ourAppSettings = settingsAdmin
               .getGlobalSettings("celulas");
         String shareName = (String) ourAppSettings
               .get("settings.log.shareName").getCurrentValue();
         String serverAddress = (String) ourAppSettings
               .get("settings.log.server").getCurrentValue();
         String initialPath = (String) ourAppSettings.get("settings.log.path")
               .getCurrentValue();
         String domainLog = (String) ourAppSettings.get("settings.log.domain")
               .getCurrentValue();
         String userName = (String) ourAppSettings.get("settings.network.user")
               .getCurrentValue();
         String password = (String) ourAppSettings
               .get("settings.network.password").getCurrentValue();
         String logFileName = (String) ourAppSettings
               .get("settings.log.promptName").getCurrentValue();

         ConfigBuilder configBuilder = smbClientService.getSmbConfigBuilder();
         configBuilder.setAuthType(AuthOptions.NTLMv2);
         configBuilder.setServer(serverAddress);
         configBuilder.setShare(shareName);
         configBuilder.setPath(initialPath);
         configBuilder.setUserId(userName);
         configBuilder.setPassword(password);

         if (domainLog != null && domainLog.length() > 0)
         {
            configBuilder.setDomain(domainLog);
         }

         SmbClient clientLog = null;
         try
         {
            clientLog = smbClientService.getNewSmbClient(configBuilder.build());
         }
         catch (com.lexmark.prtapp.smbclient.ConfigurationException e)
         {
            Activator.getLog().info("err abre lg ");
            e.printStackTrace();
         }
         catch (SmbClientException e)
         {
            Activator.getLog().info("err abre lg ");
            e.printStackTrace();
         }
         int numSheets = fileNames.size();
         lineLog.setNumSheets(numSheets);
         WriteLog wl = new WriteLog(clientLog, Activator.getLog(), logFileName,
               lineLog);
         lstThreads.add(wl);
         wl.start();
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
               }else {
                  if (((WriteLog) lstThreads.get(i)).isFinish().booleanValue())
                  {
                     numThreadFinish++;
                  }
               }
            }
         } while (numThreadFinish < lstServers.size());
      }
      this.isFinish = Boolean.TRUE;
   }

   private int getFileFormat(String fileType)
   {
      if (fileType.equals("PDF"))
      {
         Activator.getLog().info("PDF format");
         return Constants.e_PDF;
      }
      else if (fileType.equals("PDF SEGURO"))
      {
         Activator.getLog().info("PDF SECURE format");
         return Constants.e_SECURE_PDF;
      }
      else if (fileType.equals("JPG"))
      {
         Activator.getLog().info("JPG");
         return Constants.e_JPEG;
      }
      else if (fileType.equals("TIFF"))
      {
         Activator.getLog().info("TIFF");
         return Constants.e_TIFF;
      }
      else
      {
         return Constants.e_PDF;
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.lexmark.prtapp.std.prompts.WaitPrompt.Stoppable#stop(java.lang.
    * Thread, com.lexmark.prtapp.std.prompts.WaitPrompt)
    */
   public void stop(Thread thread, WaitPrompt prompt)
   {
      /**
       * This gets called if the user presses the "cancel" button on the wait
       * prompt. We'll just set the interrupted flag, which allows the run()
       * method above to know it's been interrupted and act accordingly.
       */
      thread.interrupt();
   }
}