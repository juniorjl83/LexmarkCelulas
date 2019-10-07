package com.lexmark.example.docwriter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.ungoverned.gravity.servicebinder.Lifecycle;
import org.ungoverned.gravity.servicebinder.ServiceBinderContext;

import com.lexmark.core.IntegerElem;
import com.lexmark.example.docwriter.customvlm.EditBoxPrompt;
import com.lexmark.example.docwriter.customvlm.FinishPrompt;
import com.lexmark.example.docwriter.customvlm.OmurPrompt;
import com.lexmark.example.docwriter.singleton.Id;
import com.lexmark.example.docwriter.singleton.Trabajo;
import com.lexmark.prtapp.newcharacteristics.DeviceCharacteristicsService;
import com.lexmark.prtapp.image.DocumentWriter;
import com.lexmark.prtapp.image.DocumentWriterFactory;
import com.lexmark.prtapp.image.ImageFactory;
import com.lexmark.prtapp.memoryManager.Memory;
import com.lexmark.prtapp.memoryManager.MemoryException;
import com.lexmark.prtapp.memoryManager.MemoryManager;
import com.lexmark.prtapp.memoryManager.NativeMemory;
import com.lexmark.prtapp.profile.BasicProfileContext;
import com.lexmark.prtapp.profile.PrtappProfile;
import com.lexmark.prtapp.profile.PrtappProfileException;
import com.lexmark.prtapp.profile.WelcomeScreenable;
import com.lexmark.prtapp.prompt.PromptException;
import com.lexmark.prtapp.prompt.PromptFactoryException;
import com.lexmark.prtapp.settings.SettingDefinition;
import com.lexmark.prtapp.settings.SettingDefinitionMap;
import com.lexmark.prtapp.settings.SettingsAdmin;
import com.lexmark.prtapp.settings.SettingsGroup;
import com.lexmark.prtapp.settings.SettingsStatus;
import com.lexmark.prtapp.smbclient.AuthOptions;
import com.lexmark.prtapp.smbclient.SmbClient;
import com.lexmark.prtapp.smbclient.SmbClientException;
import com.lexmark.prtapp.smbclient.SmbClientService;
import com.lexmark.prtapp.smbclient.SmbConfig.ConfigBuilder;
import com.lexmark.prtapp.std.prompts.ComboPrompt;
import com.lexmark.prtapp.std.prompts.MessagePrompt;
import com.lexmark.prtapp.std.prompts.StringPrompt;
import com.lexmark.prtapp.std.prompts.WaitMessagePrompt;
import com.lexmark.prtapp.std.prompts.WaitPrompt;
import com.lexmark.prtapp.std.prompts.WaitPrompt.Stoppable;
import com.lexmark.prtapp.storagedevice.StorageDevice;
import com.lexmark.prtapp.util.Messages;
import com.lexmark.ui.Constants;
import com.lexmark.ui.DocumentWorkflow;
import com.lexmark.ui.WorkflowFactory;
import com.lexmark.ui.WorkflowSetting;
import com.lexmark.prtapp.settings.RequiredSettingValidator;

/**
 * Profile that puts it all together. It scans the image, which is saved to disk
 * by the scan consumer. It then kicks off the document writer that turns the
 * images into PDFs.
 */
public class DocWriterProfile implements PrtappProfile, WelcomeScreenable,
      Lifecycle, ManagedService, RequiredSettingValidator
{
   private DocumentWriterFactory docWriterFactory = null;
   private StorageDevice disk = null;
   private ImageFactory imageFactory = null;
   private MemoryManager memoryManager = null;
   private SettingsAdmin settingsAdmin = null;
   private SmbClientService smbClientService = null;
   private static final String icon = "/celulas22.png";
   private Boolean isMultiTiff = Boolean.FALSE;
   private Boolean isDateMark = Boolean.FALSE;
   private byte[] iconUpImage = null;
   private String iconText = null;
   private ServiceRegistration profileRegistration = null;
   boolean activated = false;
   private ServiceBinderContext sbc = null;
   private String gralSmbUser = "";
   private String gralSmbPassword = "";
   private String emailNotificacion = "";
   private String sucursal = "";
   private String serialNumber = "";

   private DeviceCharacteristicsService characteristicsService = null;
   Log lineLog = new Log();

   /**
    * Helper class that does the document writer stuff. The array list of file
    * names comes from the scan consumer. We will create an image out of each of
    * those files, and then stuff it into the document writer.
    */
   public class MyStoppable implements Stoppable
   {
      MyScanConsumer myConsumer = null;
      ArrayList lstServers;
      BasicProfileContext context;
      MemoryManagerInstance memoryManager;

      /**
       * Constructor.
       * 
       * @param myConsumer
       *           The scan consumer. This is used to get the files saved to
       *           disk, and is also used to ensure it's safe to move forward
       * @param filePassword
       * @param fileType
       * @param context
       * @param memoryManager
       * @param destFile
       *           The file where we will save the PDF
       * @param isColor
       *           if this image color or not? We need this to determine the
       *           appropriate PDF compression
       */
      public MyStoppable(MyScanConsumer myConsumer, ArrayList lstServers, 
            BasicProfileContext context, MemoryManagerInstance memoryManager)
      {
         this.myConsumer = myConsumer;
         this.lstServers = lstServers;
         this.context = context;
         this.memoryManager = memoryManager;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.lexmark.prtapp.std.prompts.WaitPrompt.Stoppable#run()
       */
      public synchronized void run()
      {

         Activator.getLog().info("entra run stopable");
         myConsumer.waitForComplete();
         Map imagesOnDisk = Trabajo.getInstance();
         Iterator it = imagesOnDisk.entrySet().iterator();
         while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            Id id = (Id)entry.getKey();
            Activator.getLog().info("Procesando trabajo..... " + id);
            ArrayList fileNames = (ArrayList)entry.getValue();
            String fileName = id.getFilename();
            String fileType = id.getFileType();
            String filePassword = id.getFilePassword();
            
            Activator.getLog().info("Archivos en memoria inicio:: " + disk.getRootPath().list().length);
            ArrayList lstThreads = new ArrayList();
            for (int j = 0; j < lstServers.size(); j++)
            {
               SmbClient client = ((InfoServer) lstServers.get(j)).getClient();

               if (fileType.equals("JPG"))
               {
                  Activator.getLog().info("jpg archivo, servidor " + j);
                  WriteMultipleFiles mf = new WriteMultipleFiles(imageFactory,
                        client, fileNames, isDateMark, ".jpg",
                        Activator.getLog(), id);
                  lstThreads.add(mf);
                  mf.start();
               }
               else if (isMultiTiff.booleanValue())
               {
                  Activator.getLog().info("multitiff archivo, servidor " + j);
                  WriteMultipleFiles mf = new WriteMultipleFiles(imageFactory,
                        client, fileNames, isDateMark, ".tif",
                        Activator.getLog(), id);
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
                        Activator.getLog(), client, fileFormat,
                        filePassword, fileNames, isDateMark, id);
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
            String logCharacter = (String) ourAppSettings
                  .get("settings.log.caracter").getCurrentValue();

            lineLog.setSeparator(logCharacter);
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
            
            FinishProcess fp = new FinishProcess(lstThreads, fileNames, id, memoryManager);
            fp.start();
         }
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
       * @see
       * com.lexmark.prtapp.std.prompts.WaitPrompt.Stoppable#stop(java.lang.
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

   /**
    * Constructor called by Service Binder.
    */
   public DocWriterProfile(ServiceBinderContext sbc)
   {
      this.sbc = sbc;
   }

   // Service binder methods
   public void addDocumentWriterFactory(DocumentWriterFactory svc)
   {
      docWriterFactory = svc;
   }

   public void removeDocumentWriterFactory(DocumentWriterFactory svc)
   {
      docWriterFactory = null;
   }
   
   public void addImageFactory(ImageFactory svc)
   {
      imageFactory = svc;
   }

   public void removeImageFactory(ImageFactory svc)
   {
      imageFactory = null;
   }

   public void addStorageDevice(StorageDevice svc)
   {
      disk = svc;
      // Make sure we set up the proper spot in the file browse servlet
      FileBrowseWebApp.setRootPath(disk.getRootPath());
   }

   public void removeStorageDevice(StorageDevice svc)
   {
      disk = null;
      // No path, so no files to view
      FileBrowseWebApp.setRootPath(null);
   }

   public void addMemoryManager(MemoryManager svc)
   {
      memoryManager = svc;
   }

   public void removeMemoryManager(MemoryManager svc)
   {
      memoryManager = null;
   }

   // PrtappProfile methods

   public String getId()
   {
      return "DocWriterProfile";
   }

   public String getName(Locale locale)
   {
      Activator.getLog().info("Seteo inicial nombre icono");
      Messages messages = new Messages("Resources", locale,
            getClass().getClassLoader());
      return messages.getString("profile.name");
   }

   public int getShortcut()
   {
      return 0;
   }

   // This only has an effect for framework 2.0 and above
   public boolean showInHeldJobsList()
   {
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.lexmark.prtapp.profile.Profile#go(com.lexmark.prtapp.profile.
    * BasicProfileContext)
    */
   public void go(BasicProfileContext context) throws PrtappProfileException
   {
      NativeMemory nativeMem = null;
      Memory javaMem = null;
      MemoryManagerInstance memoryManagerInstance = null;
      int noMemoryErros = 0;
      isMultiTiff = Boolean.FALSE;
      SettingsGroup instances = settingsAdmin.getInstanceSettings("celulas");
      SettingDefinitionMap ourAppSettings = settingsAdmin
            .getGlobalSettings("celulas");
      
      int memory = ((Integer) ourAppSettings.get("settings.memory").getCurrentValue()).intValue();
      
      try
      {
         if(memoryManager != null) {
            try
            {
               int reserveMemory = memory * 10000000;
               Activator.getLog().info("memoria a reservar: " + reserveMemory);
               nativeMem = memoryManager.reserveNativeMemory(reserveMemory);
               javaMem = memoryManager.reserveNativeMemory(2000000);
            }
            catch(MemoryException e)
            {
               // If we don't get the memory, we can be nice and inform the user.
               String message = "No hay suficiente memoria para correr el aplicativo!";
               String title = "Memoria insuficiente.";
               MessagePrompt prompt = (MessagePrompt) context.getPromptFactory().newPrompt(MessagePrompt.ID);
               prompt.setLabel(title);
               prompt.setMessage(message);
               context.displayPrompt(prompt);
               return;
            }
         }
         
         memoryManagerInstance = new MemoryManagerInstance(nativeMem, javaMem,
               memoryManager);
         
         
         Set set = instances.getInstancePids();
         ArrayList names = new ArrayList();
         ArrayList pids = new ArrayList();
         ArrayList lstComboCelula = new ArrayList();
         Iterator i = set.iterator();

         while (i.hasNext())
         {
            String pid = (String) i.next();
            SettingDefinitionMap instance = instances.getInstance(pid);
            SettingDefinition name = instance.get("settings.instanceName");
            // names.add(name.getCurrentValue());
            // pids.add(pid);
            lstComboCelula
                  .add(new ComboCelula((String) name.getCurrentValue(), pid));
         }

         Collections.sort(lstComboCelula, new Comparator());

         for (int j = 0; j < lstComboCelula.size(); j++)
         {
            ComboCelula current = (ComboCelula) lstComboCelula.get(j);
            names.add(current.getName());
            pids.add(current.getPid());
         }

         if (names.size() > 0)
         {
            ArrayList lstServers = null;
            SettingDefinitionMap instance = null;
            boolean controlPantallas = true;
            int state = 0;
            String fileName = ""; 
            String fileType = "";
            String filePassword = "";
            String idDocumento = "";
            String prefijoGral = "";
            String prefijoApp = "";
            String prefijoProceso = "";
            Boolean isFileName = Boolean.FALSE;
            Boolean isProcessFileName = Boolean.FALSE;
            Boolean isAppFileName = Boolean.FALSE;
            StringPrompt inputPrompt = null;
            int initialSelection = 0;
            
            loop: while (controlPantallas){
               switch(state) {
               case 0:
                  boolean wasChange = false;
                  Activator.getLog()
                     .info("case 0");
                  //Back inicio primer pantallazo
                  String[] namesAsArray = (String[]) names.toArray(new String[0]);
                  ComboPrompt cp = (ComboPrompt) context.getPromptFactory()
                        .newPrompt(ComboPrompt.ID);
                  cp.setItems(namesAsArray);
                  cp.setLabel("Seleccione un proceso de escaneo");
                  cp.setSelection(initialSelection);
                  context.displayPrompt(cp);

                  int finalSelection = cp.getSelection();
                  
                  if (initialSelection != finalSelection){
                     Activator.getLog().info("was changed");
                     initialSelection = finalSelection;
                     wasChange = true;
                  }
                  
                  if ( wasChange ||  instance == null){
                     Activator.getLog().info("entra a setear valores");
                     String selectedPid = (String) pids.get(finalSelection);
                     instance = instances.getInstance(selectedPid);

                     inputPrompt = (StringPrompt) context.getPromptFactory()
                           .newPrompt(StringPrompt.ID);
                     prefijoProceso = (String) instance.get("settings.process.filename")
                           .getCurrentValue();
                     SettingDefinition instanceIsFileName = instance
                           .get("settings.isFileName");
                     SettingDefinition instanceIsProcessFileName = instance
                           .get("settings.isProcessFileName");
                     
                     isFileName = (Boolean) instanceIsFileName.getCurrentValue();
                     isProcessFileName = (Boolean) instanceIsProcessFileName.getCurrentValue();
   
                  }
                  //back fin primer pantallazo
               case 1:
                  Activator.getLog()
                     .info("case 1");
                  //back inicio segundo pantallazo
                  SettingDefinition instanceIsAppFileName = instance
                        .get("settings.isAppFileName");
                  isAppFileName = (Boolean) instanceIsAppFileName.getCurrentValue();
                  prefijoApp = (String) ourAppSettings.get("settings.app.filename")
                        .getCurrentValue();

                  if (isAppFileName.booleanValue() && prefijoApp.length() > 0){
                     prefijoGral = prefijoApp.trim() + "_";
                  }
                  if (isProcessFileName.booleanValue() && prefijoProceso.length() > 0){
                     prefijoGral += prefijoProceso.trim() + "_";
                  }
                  
                  if (isFileName.booleanValue())
                  {
                     String pregunta = (String) instance
                           .get("settings.fileName.question").getCurrentValue();
                     EditBoxPrompt editBox = new EditBoxPrompt("0", pregunta, idDocumento, prefijoGral);
                     context.displayPrompt(editBox);
                     
                     Activator.getLog().info(
                           "dismis button::: " + editBox.getDismissButton());
                     if ("cancel".equals(editBox.getDismissButton()))
                     {
                        throw new PromptException(
                              PromptException.PROMPT_CANCELLED_BY_USER);
                     }else if ("back".equals(editBox.getDismissButton())){
                        state = 0;
                        idDocumento = editBox.getRespuesta();
                        break;
                     }
                     //back fin segundo pantallazo
                     Activator.getLog().info(
                           "Respuesta::: " + editBox.getRespuesta());
                     idDocumento = editBox.getRespuesta();
                     
                  } 
               case 2: 
                  isMultiTiff = Boolean.FALSE;
                  Activator.getLog()
                     .info("case 2");
                  InfoServer infoServer = new InfoServer(Activator.getLog());
                  lstServers = infoServer.obtenerInforServers(instance,
                        ourAppSettings);
                  Activator.getLog()
                        .info("cantidad servidores:: " + lstServers.size());
                  infoServer.verificarConexiones(lstServers, smbClientService);
                  Activator.getLog()
                     .info("cantidad servidores despues de verificar:: " + lstServers.size());
                  String logServer = logServerReturn(lstServers);
                  Activator.getLog().info("Servers:" + logServer);
                  
                  if (idDocumento.length() > 0){
                     fileName = prefijoGral.trim()  + idDocumento.trim();   
                  } else {
                     fileName = prefijoGral;
                  }

                  emailNotificacion = (String) ourAppSettings.get("settings.email")
                        .getCurrentValue();
                  
                  SettingDefinition instanceIsFileFormat = instance
                        .get("settings.isFileFormat");
                  Boolean isFileFormat = (Boolean) instanceIsFileFormat
                        .getCurrentValue();

                  SettingDefinition instanceIsDateMark = instance
                        .get("settings.isFileDate");
                  isDateMark = (Boolean) instanceIsDateMark.getCurrentValue();

                  SettingDefinition instanceIntFileFormat = instance
                        .get("settings.fileFormat");
                  int intFileFormat = ((Integer) instanceIntFileFormat
                        .getCurrentValue()).intValue();
                  String[] fileTypeAsArray =
                  { "TIFF", "JPG", "PDF", "MULTI-TIFF", "PDF SEGURO" };

                  if (isFileFormat.booleanValue())
                  {
                     OmurPrompt omurPromt = new OmurPrompt(
                           "1",
                           "Seleccione el formato del archivo a generar.", fileTypeAsArray,
                           getValueFileFormat(intFileFormat));
                     context.displayPrompt(omurPromt);
                     Activator.getLog().info(
                           "dismiis button::: " + omurPromt.getDismissButton());
                     if ("cancel".equals(omurPromt.getDismissButton()))
                     {
                        throw new PromptException(
                              PromptException.PROMPT_CANCELLED_BY_USER);
                     }else if ("back".equals(omurPromt.getDismissButton())){
                        if (isFileName.booleanValue()){
                           state = 1;   
                        }else{
                           state = 0;
                        }
                        break;
                     }
                     Activator.getLog().info(
                           "Respuesta::: " + omurPromt.getRespuesta());
                     int selectionFileType = Integer.parseInt(omurPromt.getRespuesta());
                     if (selectionFileType == 3)
                     {
                        isMultiTiff = Boolean.TRUE;
                     }
                     fileType = fileTypeAsArray[selectionFileType];
                  }
                  else
                  {
                     if (intFileFormat == 3)
                     {
                        isMultiTiff = Boolean.TRUE;
                     }
                     fileType = fileTypeAsArray[getValueFileFormat(intFileFormat)];

                  }
                  filePassword = (String) instance
                        .get("settings.instanceFilePassword").getCurrentValue();
               case 3:
                  if (lstServers!= null && !lstServers.isEmpty())
                  {

                     try
                     {
                        DocumentWorkflow docWorkflow = (DocumentWorkflow) context
                              .getWorkflowFactory().create(WorkflowFactory.DOCUMENT);

                        Boolean removeWhitePages = (Boolean) instance
                              .get("settings.isWhitePage").getCurrentValue();
                        
                        int whitePageValue = ((Integer) ourAppSettings
                              .get("settings.whitepage").getCurrentValue()).intValue();
                        
                        setConfiguraciones(instance, docWorkflow);

                        context.displayWorkflow(docWorkflow);

                        MyScanConsumer myConsumer = new MyScanConsumer(imageFactory,
                              disk, fileName, fileType, filePassword, removeWhitePages, whitePageValue);

                        docWorkflow.setConsumer(myConsumer);
                        context.startWorkflow(docWorkflow,
                              BasicProfileContext.WAIT_FOR_SCAN_COMPLETE);

                        WaitMessagePrompt wmp = (WaitMessagePrompt) context
                              .getPromptFactory().newPrompt(WaitMessagePrompt.ID);

                        getlineLog(instance, lstServers, idDocumento, fileType, myConsumer.getNumBlank());

                        // This guy does all of the work
                        Activator.getLog().info("antes de estopable");
                        MyStoppable myStoppable = new MyStoppable(myConsumer,
                              lstServers, context, memoryManagerInstance);

                        wmp.setWorkerRunnable(myStoppable, "Creación del archivo");
                        wmp.setMessage("Enviando el archivo...");
                        context.displayPrompt(wmp);
                        
                        String[] fileTypeAsArrayFinal =
                           { "Si, al mismo destino", "Si, a un destino distinto", "No" };
                        
                        FinishPrompt finishPromt = new FinishPrompt(
                              "1",
                              "Desea digitalizar documentos adicionales.", fileTypeAsArrayFinal,
                              0, lineLog.getNumSheets());
                        context.displayPrompt(finishPromt);
                        
                        if ("cancel".equals(finishPromt.getDismissButton()))
                        {
                           throw new PromptException(
                                 PromptException.PROMPT_CANCELLED_BY_USER);
                        }
                        
                        Activator.getLog().info(
                              "Respuesta::: " + finishPromt.getRespuesta());
                        int selectionType = Integer.parseInt(finishPromt.getRespuesta());
                        idDocumento = "";
                        if (selectionType == 0)
                        {
                           if (isFileName.booleanValue()){
                              state = 1;   
                           }else{
                              state = 2;
                           }
                           break;
                        } else if (selectionType == 1){
                           state = 0;
                           break;
                        } 
                     }
                     catch (PromptException e)
                     {
                        Activator.getLog().info("Exception:: " + e.getMessage());
                     }
                     catch (Exception e)
                     {
                        Activator.getLog().info("Exception:: " + e.getMessage());
                        MessagePrompt noInstances = (MessagePrompt) context
                              .getPromptFactory().newPrompt(MessagePrompt.ID);
                        noInstances.setMessage(
                              "No es posible conectar al destino. contacte con el administrador!");
                        context.displayPrompt(noInstances);
                     }

                  }
                  else
                  {
                     MessagePrompt noInstances = (MessagePrompt) context
                           .getPromptFactory().newPrompt(MessagePrompt.ID);
                     noInstances.setMessage(
                           "No es posible conectar a ningún servidor destino. Contacte con el administrador!");
                     context.displayPrompt(noInstances);
                  }
               default:
                  break loop; 
               }
            }
         }
         else
         {
            MessagePrompt noInstances = (MessagePrompt) context
                  .getPromptFactory().newPrompt(MessagePrompt.ID);
            noInstances.setMessage(
                  "No se han configurado los procesos de escaneado!");
            context.displayPrompt(noInstances);
         }
      }
      catch (PromptException e)
      {
         Activator.getLog().info("Prompt stopped: " + e.getMessage());
         if (memoryManagerInstance != null)
            memoryManagerInstance.releaseMemory();
      }
      catch (Exception e)
      {
         Activator.getLog().info("Exception thrown", e);
         if (memoryManagerInstance != null)
            memoryManagerInstance.releaseMemory();
      }
   }

   private void getlineLog(SettingDefinitionMap instance, ArrayList lstServers,
         String idDocumento, String fileType, int numBlank)
   {
      StringBuffer line = new StringBuffer("");
      serialNumber = characteristicsService.get("serialNumber");
      String celula = (String) instance.get("settings.instanceName")
            .getCurrentValue();
      SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
      String date = sdf.format(new Date());
      String path = getRutasEscaneo(lstServers);

      lineLog.setSerial(serialNumber);
      lineLog.setPath(path);
      lineLog.setDate(date);
      lineLog.setNameCelula(celula);
      lineLog.setNameDocument(idDocumento);
      lineLog.setFileType(fileType);
      lineLog.setNumBlank(numBlank);

   }

   private String getRutasEscaneo(ArrayList lstServers)
   {
      StringBuffer server = new StringBuffer("\\\\");
      for (int i = 0; i < lstServers.size(); i++)
      {

         InfoServer infoServer = ((InfoServer) lstServers.get(i));
         server.append(infoServer.getInstanceServer());
         server.append("\\");
         server.append(infoServer.getInstanceSharedName());
         server.append(infoServer.getInstancePath());
         if ((i + 1) < lstServers.size())
         {
            server.append("-");
         }
      }
      return server.toString();
   }

   private int getValueFileFormat(int intFileFormat)
   {
      if (intFileFormat == 6)
      {
         return 4;
      }
      else if (intFileFormat == 3)
      {
         return 0;
      }
      else
      {
         return intFileFormat;
      }
   }

   private void setConfiguraciones(SettingDefinitionMap instance,
         DocumentWorkflow docWorkflow)
   {

      // Tamaño paapel
      setConfiguracion(instance, docWorkflow, "originalMediaSize",
            "settings.paperSize", "settings.isPaperSize");

      // Orientacion escaneado
      setConfiguracion(instance, docWorkflow, "orientation",
            "settings.orientation", "settings.isOrientation");

      // Contenido del documento
      setConfiguracion(instance, docWorkflow, "contentType", "settings.content",
            "settings.isContent");

      // Caras
      setConfiguracion(instance, docWorkflow, "scanDuplex", "settings.duplex",
            "settings.isDuplex");

      // Resolucion
      setConfiguracion(instance, docWorkflow, "resolution",
            "settings.resolution", "settings.isResolution");

      // Color
      setConfiguracion(instance, docWorkflow, "color", "settings.color",
            "settings.isColor");

      // Formato Archivo obligado es jpeg para tratar imagen por imagen
      WorkflowSetting fileFormat = docWorkflow.getSettingCollection()
            .getSetting("fileFormat");
      fileFormat.setInfo(new IntegerElem(Constants.e_TIFF));
      fileFormat.lock();

      WorkflowSetting multiPageTiff = docWorkflow.getSettingCollection()
            .getSetting("multiPageTiff");
      multiPageTiff.setInfo(new IntegerElem(0));
   }

   private void setConfiguracion(SettingDefinitionMap instance,
         DocumentWorkflow docWorkflow, String settingName, String valueName,
         String lockName)
   {

      WorkflowSetting wfSetting = docWorkflow.getSettingCollection()
            .getSetting(settingName);
      SettingDefinition instanceValue = instance.get(valueName);
      int value = ((Integer) instanceValue.getCurrentValue()).intValue();
      wfSetting.setInfo(new IntegerElem(value));
      SettingDefinition instanceIsLock = instance.get(lockName);
      Boolean isLock = (Boolean) instanceIsLock.getCurrentValue();
      if (isLock.booleanValue())
      {
         wfSetting.unlock();
      }
      else
      {
         wfSetting.lock();
      }
   }

   /**
    * Helper to clean out the directory. A normal program would do this at the
    * end, such as in the "finally" block of the go method, however we do this
    * at the beginning of the profile. The reason is that we leave the files
    * around after a run so that, for debug purposes, the user can look at the
    * files that were created.
    */
   private synchronized void cleanUpOldFiles()
   {
      File root = disk.getRootPath();
      String[] files = root.list();
      for (int i = 0; i < files.length; i++)
      {
         File fileToDelete = new File(root, files[i]);
         fileToDelete.delete();
      }
   }

   public InputStream getDownIcon()
   {
      if(iconUpImage == null || iconUpImage.length == 0)
      {
         InputStream iconStream = getClass().getResourceAsStream(icon);
         return iconStream;
      }
      else
      {
         return new ByteArrayInputStream(iconUpImage);
      }
   }

   public String getIconText(Locale locale)
   {
      return iconText;
   }

   public InputStream getUpIcon()
   {
      if(iconUpImage == null || iconUpImage.length == 0)
      {
         InputStream iconStream = getClass().getResourceAsStream(icon);
         return iconStream;
      }
      else
      {
         return new ByteArrayInputStream(iconUpImage);
      }
   }

   public String getWorkflowOveride()
   {
      return null;
   }

   /**
    * ServiceBinder method - called when SettingsAdmin arrives
    */
   public void addSettingsAdmin(SettingsAdmin svc)
   {
      settingsAdmin = svc;
   }

   /**
    * ServiceBinder method - called when SettingsAdmin leaves town
    */
   public void removeSettingsAdmin(SettingsAdmin svc)
   {
      settingsAdmin = null;
   }

   /**
    * ServiceBinder method - called when SmbClientService arrives
    */
   public void addSmbClientService(SmbClientService svc)
   {
      smbClientService = svc;
   }

   /**
    * ServiceBinder method - called when SmbClientService leaves town
    */
   public void removeSmbClientService(SmbClientService svc)
   {
      smbClientService = null;
   }

   /**
    * ServiceBinder method - called when DeviceCharacteristicsService arrives
    */
   public void addDeviceCharacteristics(DeviceCharacteristicsService svc)
   {
      characteristicsService = svc;
   }

   /**
    * ServiceBinder method - called when DeviceCharacteristicsService leaves
    * town
    */
   public void removeDeviceCharacteristics(DeviceCharacteristicsService svc)
   {
      characteristicsService = null;
   }

   private String logServerReturn(ArrayList lstServers)
   {
      StringBuffer sb = new StringBuffer("");

      for (int i = 0; i < lstServers.size(); i++)
      {
         sb.append("Servidor: ");
         sb.append(i + 1);
         sb.append(((InfoServer) lstServers.get(i)).toString());
         sb.append("\n");
      }

      return sb.toString();
   }

   public void updated(Dictionary settings) throws ConfigurationException
   {
      Activator.getLog().info("entra a updated!");
      if (settings != null)
      {
         boolean iconNeedsUpdate = false;

         Activator.getLog().info("We got new settings!");
         Enumeration elems = settings.keys();

         while (elems.hasMoreElements())
         {
            String key = elems.nextElement().toString();
            Object value = settings.get(key);
            Activator.getLog().info("\t" + key + " = " + value);

            if (key.equals("settings.icon.text"))
            {
               iconNeedsUpdate = true;
               iconText = (String) value;
            }else if(key.equals("settings.icon.image"))
            {
               iconNeedsUpdate = true;
               iconUpImage = (byte[])value;
            }
         }
         if (iconNeedsUpdate) updateIcon();
      }
   }

   private void updateIcon()
   {
      if (profileRegistration != null)
      {
         profileRegistration.unregister();
         profileRegistration = null;
      }

      if (activated)
      {
         Dictionary dict = new Hashtable();
         profileRegistration = sbc.getBundleContext().registerService(
               "com.lexmark.prtapp.profile.PrtappProfile", this, dict);
      }
   }

   public synchronized void activate()
   {
      activated = true;
      updateIcon();
   }

   public synchronized void deactivate()
   {
      activated = false;
      updateIcon();
   }

   public boolean validate(String pid, Dictionary settings, Locale locale,
         SettingsStatus status)
   {
      Activator.getLog().info("Pid is " + pid);
      Messages messages = new Messages(locale, getClass().getClassLoader());

      if (!pid.equals("celulas"))
      {
         Activator.getLog().info("Pid hijo");
         int fileFormat = ((Integer) settings.get("settings.fileFormat"))
               .intValue();
         String passPdf = (String) settings
               .get("settings.instanceFilePassword");
         Activator.getLog().info("fileFormat " + fileFormat);

         if (fileFormat == 6 && (passPdf != null && passPdf == ""))
         {
            Activator.getLog().info("Activar validación");
            String msg = messages.getString("setting.error.filePassword");
            status.addStatus("setting.settingvalidationexample.error",
                  "settings.instanceFilePassword", msg,
                  SettingsStatus.STATUS_TYPE_ERROR);
            return false;
         }

         ArrayList lstServer = new ArrayList();

         String server1 = (String) settings.get("settings.instanceServer1");
         String sharedName1 = (String) settings
               .get("settings.instanceShareName1");
         String domain1 = (String) settings.get("settings.instanceDomain1");
         String path1 = (String) settings.get("settings.instancePath1");
         /*
          * String userId1 = (String) settings.get("settings.network.user");
          * String password1 = (String)
          * settings.get("settings.network.password");
          */

         String server2 = (String) settings.get("settings.instanceServer2");
         String sharedName2 = (String) settings
               .get("settings.instanceShareName2");
         String domain2 = (String) settings.get("settings.instanceDomain2");
         String path2 = (String) settings.get("settings.instancePath2");
         /*
          * String userId2 = (String) settings.get("settings.network.user");
          * String password2 = (String)
          * settings.get("settings.network.password");
          */

         String server3 = (String) settings.get("settings.instanceServer3");
         String sharedName3 = (String) settings
               .get("settings.instanceShareName3");
         String domain3 = (String) settings.get("settings.instanceDomain3");
         String path3 = (String) settings.get("settings.instancePath3");
         /*
          * String userId3 = (String) settings.get("settings.network.user");
          * String password3 = (String)
          * settings.get("settings.network.password");
          */

         if (!isEmpty(server1) && !isEmpty(sharedName1) && !isEmpty(gralSmbUser)
               && !isEmpty(gralSmbPassword))
         {
            Activator.getLog().info("ENTRA A SETEAR SERVER 1");
            lstServer.add(new InfoServer(server1, sharedName1, domain1, path1,
                  gralSmbUser, gralSmbPassword));
         }

         if (!isEmpty(server2) && !isEmpty(sharedName2) && !isEmpty(gralSmbUser)
               && !isEmpty(gralSmbPassword))
         {
            Activator.getLog().info("ENTRA A SETEAR SERVER 2");
            lstServer.add(new InfoServer(server2, sharedName2, domain2, path2,
                  gralSmbUser, gralSmbPassword));
         }

         if (!isEmpty(server3) && !isEmpty(sharedName3) && !isEmpty(gralSmbUser)
               && !isEmpty(gralSmbPassword))
         {
            Activator.getLog().info("ENTRA A SETEAR SERVER 3");
            lstServer.add(new InfoServer(server3, sharedName3, domain3, path3,
                  gralSmbUser, gralSmbPassword));
         }

         InfoServer infoServer = new InfoServer(Activator.getLog());
         infoServer.verificarConexiones(lstServer, smbClientService);
         String msgServer = "";

         if (lstServer.size() > 0)
         {
            for (int i = 0; i < lstServer.size(); i++)
            {

               InfoServer server = ((InfoServer) lstServer.get(i));

               if (server.getClient() == null)
               {
                  msgServer += " No se ha podido conectar con el servidor: "
                        + (i + 1) + "\n";
                  break;
               }
            }
         }
         else
         {
            msgServer += " No se ha podido conectar con ningún servidor.";
         }

         if (msgServer.length() > 0)
         {
            status.addStatus("setting.settingvalidationexample.error",
                  "settings.instanceServer1", msgServer,
                  SettingsStatus.STATUS_TYPE_ERROR);
            return false;
         }

         Boolean isAppFileName = (Boolean) settings.get("settings.isAppFileName");
         Boolean isProcessFileName = (Boolean) settings.get("settings.isProcessFileName");
         
         if (!(isAppFileName.booleanValue() || isProcessFileName.booleanValue())){
            status.addStatus("setting.settingvalidationexample.error",
                  "settings.isAppFileName", "Debe activarse por lo menos un prefijo al nombre del archivo",
                  SettingsStatus.STATUS_TYPE_ERROR);
            return false;
         }
         
      }
      else if (pid.equals("celulas"))
      {

         Activator.getLog().info("Pid padre");
         ArrayList lstServer = new ArrayList();

         String serverLog = (String) settings.get("settings.log.server");
         String sharedNameLog = (String) settings.get("settings.log.shareName");
         String domainLog = (String) settings.get("settings.log.domain");
         String pathLog = (String) settings.get("settings.log.path");
         String userIdLog = (String) settings.get("settings.network.user");
         String passwordLog = (String) settings
               .get("settings.network.password");
         
         gralSmbUser = userIdLog;
         gralSmbPassword = passwordLog;
         
         if (!isEmpty(serverLog) && !isEmpty(sharedNameLog)
               && !isEmpty(userIdLog) && !isEmpty(passwordLog))
         {
            Activator.getLog().info("ENTRA A SETEAR SERVER L0G");
            lstServer.add(new InfoServer(serverLog, sharedNameLog, domainLog,
                  pathLog, userIdLog, passwordLog));

            InfoServer infoServer = new InfoServer(Activator.getLog());
            infoServer.verificarConexiones(lstServer, smbClientService);
            String msgServer = "";

            for (int i = 0; i < lstServer.size(); i++)
            {

               InfoServer server = ((InfoServer) lstServer.get(i));

               if (server.getClient() == null)
               {
                  msgServer += " No se ha podido conectar con el servidor log.";
               }
            }

            if (msgServer.length() > 0)
            {
               status.addStatus("setting.settingvalidationexample.error",
                     "settings.log.server", msgServer,
                     SettingsStatus.STATUS_TYPE_ERROR);
               return false;
            }
         }
      }
      return true;
   }

   private boolean isEmpty(String value)
   {
      Activator.getLog().info(value);
      if (value != null && value != "")
      {
         return false;
      }
      else
      {
         return true;
      }
   }
   
}
