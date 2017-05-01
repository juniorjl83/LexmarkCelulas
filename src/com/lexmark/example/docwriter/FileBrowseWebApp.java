//*************************** Lexmark Confidential ****************************
// $URL$
// $Id$
//
// Copyright (c) 2010 Lexmark International, Inc.
// All Rights Reserved.
//*****************************************************************************

package com.lexmark.example.docwriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpService;
import org.ungoverned.gravity.servicebinder.Lifecycle;

import com.lexmark.prtapp.util.Messages;
import com.lexmark.prtapp.util.Web;
import com.lexmark.prtapp.webadmin.Consts;
import com.lexmark.prtapp.webapp.WebApp;

/**
 * This is a helper class that provides a web app that allows a user
 * to browse the images created by this example.  For a more thorough
 * example of web applications, see the "webappexample".
 */
public class FileBrowseWebApp extends HttpServlet implements WebApp, Lifecycle
{
   private static final long serialVersionUID = 1L;

   private static final String SERVLET_ALIAS_INTERNAL = Consts.WEB_APPS_PATH + "info";
   
   private HttpService httpService = null;

   private static File rootPath = null;
   
   /**
    * Service Binder instantiates this class
    */
   public FileBrowseWebApp()
   {
   }

   /**
    * Sets the "root" path where we look for files.
    */
   public static void setRootPath(File path)
   {
      rootPath = path;
   }
   
   /**
    * Need the HttpService to register our servlet
    */
   public void addHttpService(HttpService svc)
   {
      httpService = svc;
   }
   
   /**
    * Called when the service goes away.
    */
   public void removeHttpService(HttpService svc)
   {
      httpService = null;
   }

   /**
    * Called when all of our dependent services are available and we're started
    */
   public void activate()
   {
      try
      {
         httpService.registerServlet(SERVLET_ALIAS_INTERNAL, this, null, null);
      }
      catch(Exception e)
      {
         Activator.getLog().info("Problem registering servlet", e);
      }
   }

   /**
    * Called when we're stopped or a service we need goes away
    */
   public void deactivate()
   {
      if(httpService != null)
      {
         httpService.unregister(SERVLET_ALIAS_INTERNAL);
      }
   }
   
   /* (non-Javadoc)
    * @see com.lexmark.prtapp.webapp.WebApp#getDescription(java.util.Locale)
    */
   /* (non-Javadoc)
    * @see com.lexmark.prtapp.webapp.WebApp#getDescription(java.util.Locale)
    */
   public String getDescription(Locale locale)
   {
      Messages messages = new Messages("Resources", locale, getClass().getClassLoader());
      return messages.getString("application.webapp.description");
   }

   /* (non-Javadoc)
    * @see com.lexmark.prtapp.webapp.WebApp#getIconURL()
    */
   public String getIconURL()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see com.lexmark.prtapp.webapp.WebApp#getId()
    */
   public String getId()
   {
      return "celulas.webapp";
   }

   /* (non-Javadoc)
    * @see com.lexmark.prtapp.webapp.WebApp#getName(java.util.Locale)
    */
   public String getName(Locale locale)
   {
      Messages messages = new Messages("Resources", locale, getClass().getClassLoader());
      return messages.getString("application.webapp.name");
   }

   /* (non-Javadoc)
    * @see com.lexmark.prtapp.webapp.WebApp#getUrl()
    */
   public String getUrl()
   {
      return Web.getRootPath() + SERVLET_ALIAS_INTERNAL;
   }

   /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException
   {
      doPost(req, resp);
   }

   /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException
   {
      String fileName = (String)req.getParameter("fileName");
      Activator.getLog().debug("File name is " + fileName);
      
      if(fileName != null)
      {
         downloadFile(resp, fileName);
      }
      else
      {
         resp.setContentType("text/html");
         PrintWriter out = resp.getWriter();
         out.println("<html><body>");
         
         String[] files = null;
         if(rootPath != null) files = rootPath.list();

         if(files == null || files.length == 0)
         {
            out.println("No image files to view!<br>");
         }
         else
         {
            String url = Web.getRootPath() + SERVLET_ALIAS_INTERNAL;
            out.println("<form method=\"GET\" action=\"" + url + "\">");
            out.println("<p>Stored images:<p>");
            out.println("<ul>");
            for(int i = 0; i < files.length; i++)
            {
               out.print("<li><input type=\"radio\" name=\"fileName\" value=\"" + files[i] +  "\">" + files[i] + "</li>");
            }
            out.println("</ul><p><input type=\"submit\">");
            out.println("</form>");
         }
         out.println("</body></html>");
      }
   }

   /**
    * Helper method to produce the appropriate image for download
    */
   private void downloadFile(HttpServletResponse resp, String fileName)
   {
      // Root path could be null if it was never set.  So make sure we
      // handle that case as an error.
      File imageFile = (rootPath == null ? null : new File(rootPath, fileName));
      if(imageFile != null && imageFile.exists())
      {
         InputStream i = null;
         OutputStream o = null;
         try
         {
            // This forces it to be a file download, and does not try to open it
            // in the same browser instance.
            resp.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            
            // See what the content type is based on extension.  Will either be TIFF, JPEG, or PDF.
            if(fileName.endsWith(".tif"))
            {         
               resp.setContentType("image/tiff");
            }
            else if(fileName.endsWith(".pdf"))
            {
               resp.setContentType("application/pdf");
            }
            else if(fileName.endsWith(".jpg"))
            {
               resp.setContentType("image/jpeg");
            }

            i = new FileInputStream(imageFile);
            o = resp.getOutputStream();
            
            byte[] buffer = new byte[16384];
            int bytesRead = 0;
            while((bytesRead = i.read(buffer)) != -1)
            {
               o.write(buffer, 0, bytesRead);
            }
         }
         catch(Exception e)
         {
            Activator.getLog().info("Problem downloading file", e);
         }
         finally
         {
            if(i != null)
            {
               try
               {
                  i.close();
               }
               catch(Exception thisSucks)
               {
                  Activator.getLog().debug("Problem closing stream", thisSucks);
               }
            }
            
            if(o != null)
            {
               try
               {
                  o.close();
               }
               catch(Exception thisSucks)
               {
                  Activator.getLog().debug("Problem closing stream", thisSucks);
               }
            }
         }
      }
      else
      {
         try
         {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html><body><h2>File does not exist: " + imageFile + "</h2></body></html>");
         }
         catch(IOException e)
         {
            Activator.getLog().debug("Problem getting print writer", e);
         }
      }
   }
   
   
}
