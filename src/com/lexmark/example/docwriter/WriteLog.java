package com.lexmark.example.docwriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lexmark.prtapp.smbclient.ConnectionException;
import com.lexmark.prtapp.smbclient.SmbClient;
import com.lexmark.prtapp.smbclient.SmbClientException;
import com.lexmark.prtapp.util.AppLogRef;

public class WriteLog extends Thread
{
   private SmbClient client = null;
   private AppLogRef log;
   private String fileName;
   private String aa = "99";
   private String mm = "99";
   private String dd = "99";
   private Log line;
   private Boolean isFinish = Boolean.FALSE;

   public WriteLog(SmbClient client, AppLogRef log, String fileName, Log line)
   {
      super();
      this.client = client;
      this.log = log;
      this.fileName = fileName;
      this.line = line;
   }

   public synchronized void run()
   {
      log.info("entra escribir log");
      SimpleDateFormat faa = new SimpleDateFormat("yy");
      SimpleDateFormat fmm = new SimpleDateFormat("MM");
      SimpleDateFormat fdd = new SimpleDateFormat("dd");
      Date date = new Date();

      aa = faa.format(date);
      mm = fmm.format(date);
      dd = fdd.format(date);
      // disable date, just one file
      // fileName = fileName + aa + mm + dd + ".txt";
      fileName = fileName + ".txt";

      InputStream inputStream = null;
      OutputStream fileStream = null;

      try
      {
         client.connect();
         boolean exists = client.doesFileExist("", fileName);
         if (exists)
         {
            inputStream = client.getInputStream("", fileName);

            BufferedReader br = new BufferedReader(
                  new InputStreamReader(inputStream));
            String temp = "";
            StringBuffer sb1 = new StringBuffer("");

            while ((temp = br.readLine()) != null)
            {
               sb1.append(temp);
               sb1.append("\n");
            }
            br.close();
            sb1.append(line);

            fileStream = client.getOutputStream("", fileName);
            fileStream.write(sb1.toString().getBytes());

         }
         else
         {
            fileStream = client.getOutputStream("", fileName);
            fileStream.write(line.toString().getBytes());
         }
      }
      catch (ConnectionException e)
      {
         log.info("error WriteLog: " + e.getMessage());
         client.close();
         return;
      }
      catch (SmbClientException e)
      {
         log.info("error WriteLog: " + e.getMessage());
         client.close();
         return;
      }
      catch (IOException e)
      {
         log.info("error WriteLog: " + e.getMessage());
         client.close();
         return;
      }
      finally
      {
         try
         {
            if (fileStream != null) fileStream.close();
            if (inputStream != null) inputStream.close();
            if (client != null) client.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

      }
      isFinish = Boolean.TRUE;
      log.info("finaliza hilo lg: " + this);
   }

   public Boolean isFinish()
   {
      return isFinish;
   }

}
