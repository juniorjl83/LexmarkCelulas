package com.lexmark.example.docwriter;

import java.util.ArrayList;
import java.util.List;

import com.lexmark.prtapp.settings.SettingDefinitionMap;
import com.lexmark.prtapp.smbclient.AuthOptions;
import com.lexmark.prtapp.smbclient.SmbClient;
import com.lexmark.prtapp.smbclient.SmbClientException;
import com.lexmark.prtapp.smbclient.SmbClientService;
import com.lexmark.prtapp.smbclient.SmbConfig.ConfigBuilder;
import com.lexmark.prtapp.util.AppLogRef;

public class InfoServer
{
   private AppLogRef activator;
   private String instanceServer;
   private String instanceSharedName;
   private String instanceDomain;
   private String instancePath;
   private String instanceUserId;
   private String instancePassword;
   private SmbClient client = null;
   
   public InfoServer(String instanceServer, String instanceSharedName,
         String instanceDomain, String instancePath, String instanceUserId,
         String instancePassword)
   {
      super();
      this.instanceServer = instanceServer;
      this.instanceSharedName = instanceSharedName;
      this.instanceDomain = instanceDomain;
      this.instancePath = instancePath;
      this.instanceUserId = instanceUserId;
      this.instancePassword = instancePassword;
   }

   public InfoServer(AppLogRef activator)
   { 
      super();
      this.activator = activator;
     
   }


   public ArrayList obtenerInforServers(SettingDefinitionMap instance){
      
      activator.info("entra obtenerInforServers");
      ArrayList lstServer = new ArrayList();
      
      validateAddServerValues(instance, lstServer, "1");
      validateAddServerValues(instance, lstServer, "2");
      validateAddServerValues(instance, lstServer, "3");
      
      return lstServer;
      
   }

   private void validateAddServerValues(SettingDefinitionMap instance, 
         ArrayList lstServer, String numServer)
   {
      activator.info("entra validateAddServerValues");
      
      String instanceServer = (String)instance.get("settings.instanceServer"+numServer).getCurrentValue();
      String instanceSharedName = (String)instance.get("settings.instanceShareName"+numServer).getCurrentValue();
      String instanceDomain = (String)instance.get("settings.instanceDomain"+numServer).getCurrentValue();
      String instancePath = (String)instance.get("settings.instancePath"+numServer).getCurrentValue();
      String instanceUserId = (String)instance.get("settings.instanceUserId"+numServer).getCurrentValue();
      String instancePassword = (String)instance.get("settings.instancePassword"+numServer).getCurrentValue();
      
      activator.info("instanceServer"+numServer + " - " + instanceServer);
      activator.info("instanceSharedName"+numServer + " - " + instanceSharedName);
      activator.info("instanceDomain"+numServer + " - " + instanceDomain);
      activator.info("instancePath"+numServer + " - " + instancePath);
      activator.info("instanceUserId"+numServer + " - " + instanceUserId);
      activator.info("instancePassword"+numServer + " - " + instancePassword);
      
      if ( !isEmpty(instanceServer) && !isEmpty(instanceSharedName) &&
            !isEmpty(instanceUserId) && !isEmpty(instancePassword)){
         
         lstServer.add(new InfoServer(instanceServer, instanceSharedName, instanceDomain, 
               instancePath, instanceUserId, instancePassword));
      }
   }


   private boolean isEmpty(String value){
      if ( value != null && value != ""){
         return false;
      }else{
         return true;
      }
   }
   
   public String getInstanceServer()
   {
      return instanceServer;
   }

   public void setInstanceServer(String instanceServer)
   {
      this.instanceServer = instanceServer;
   }

   public String getInstanceSharedName()
   {
      return instanceSharedName;
   }

   public void setInstanceSharedName(String instanceSharedName)
   {
      this.instanceSharedName = instanceSharedName;
   }

   public String getInstanceDomain()
   {
      return instanceDomain;
   }

   public void setInstanceDomain(String instanceDomain)
   {
      this.instanceDomain = instanceDomain;
   }

   public String getInstancePath()
   {
      return instancePath;
   }

   public void setInstancePath(String instancePath)
   {
      this.instancePath = instancePath;
   }

   public String getInstanceUserId()
   {
      return instanceUserId;
   }

   public void setInstanceUserId(String instanceUserId)
   {
      this.instanceUserId = instanceUserId;
   }

   public String getInstancePassword()
   {
      return instancePassword;
   }

   public void setInstancePassword(String instancePassword)
   {
      this.instancePassword = instancePassword;
   }

   public SmbClient getClient()
   {
      return client;
   }

   public void setClient(SmbClient client)
   {
      this.client = client;
   }

   public void verificarConexiones(ArrayList lstServers,
         SmbClientService smbClientService)
   {
      int validaNServer = 1;
      activator.info("verificarConexiones");
      for ( int i=0; i < lstServers.size(); i++){
         
         ConfigBuilder configBuilder = smbClientService.getSmbConfigBuilder();
         InfoServer infoServer = ((InfoServer) lstServers.get(i));
         configBuilder.setAuthType(AuthOptions.NTLMv2);
         configBuilder.setServer(infoServer.getInstanceServer());
         
         String domain = infoServer.getInstanceDomain();
         if (domain != null && domain.length() > 0){
            configBuilder.setDomain(domain);
         }

         configBuilder.setShare(infoServer.instanceSharedName);
         configBuilder.setPath(infoServer.instancePath);
         configBuilder.setUserId(infoServer.instanceUserId);
         configBuilder.setPassword(infoServer.getInstancePassword());
         
         try
         {
            client = smbClientService.getNewSmbClient(configBuilder.build());
            client.connect();
            infoServer.setClient(client);
         }
         catch (Exception e)
         {
            activator.info("Error conexion servidor " + validaNServer);
            //lstServers.remove(i);
            //i--;
         }
         validaNServer++;
      }
   }

   public String toString()
   {
      return "InfoServer [instanceServer=" + instanceServer
            + ", instanceSharedName=" + instanceSharedName + ", instanceDomain="
            + instanceDomain + ", instancePath=" + instancePath
            + ", instanceUserId=" + instanceUserId + ", instancePassword="
            + instancePassword + ", client=" + client + "]";
   }

   
}