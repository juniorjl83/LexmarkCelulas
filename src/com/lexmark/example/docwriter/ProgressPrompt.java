//*************************** Lexmark Confidential ****************************
// $URL$
// $Id$
//
// Copyright (c) 2008 Lexmark International, Inc.
// All Rights Reserved.
//*****************************************************************************

package com.lexmark.example.docwriter;

import com.lexmark.core.Element;
import com.lexmark.core.IntegerElem;
import com.lexmark.core.MapElem;
import com.lexmark.prtapp.prompt.PromptEventResult;
import com.lexmark.prtapp.prompt.PromptException;
import com.lexmark.prtapp.std.prompts.WaitPrompt;

/**
 * This is a custom wait prompt.  The base class has the capability of running a
 * worker thread while the prompt is shown, and the prompt will automatically dismiss
 * if the thread finishes or the user cancels.  So for this example, we'll put up
 * a pretty progress bar.
 */
public class ProgressPrompt extends WaitPrompt
{
   /** The public ID of this prompt, in case it ever gets put into a prompt factory */
   public static final String ID = "example.progressPrompt";
   
   // The percentage complete for the progress bar
   int percentageComplete = 0;
   
   // Has the prompt been shown?
   boolean shown = false;
   
   /**
    * Gratuitous constructor
    */
   public ProgressPrompt()
   {
   }
   
   /**
    * This will set the progress level and then refresh the VLML.  It is intended
    * to be called from within the worker thread periodically as it goes through
    * its task.
    * @param progressPercent A number from 0 to 100 representing the percentage of
    * work done to show on the progress bar.
    */
   public void setProgress(int progressPercent)
   {
      percentageComplete = progressPercent;
      if(percentageComplete < 0) percentageComplete = 0;
      if(percentageComplete > 100) percentageComplete = 100;

      // This could be called before or after the prompt has displayed.
      // No need to update the display if the prompt isn't showing
      if(shown)
      {
         // Now update the control.
         MapElem params = new MapElem();
         params.addChild("percentage", new IntegerElem(percentageComplete));
         try
         {
            getContext().call("ProgressPrompt.progressBar", "updateStatus", params);
         }
         catch(PromptException e)
         {
            Activator.getLog().debug("Problem updating progress", e);
         }
      }
   }

   /**
    * Get the VLML for the progress screen.  It is pretty simple - a title
    * message and a progress bar.  We could have made thie fancier and more
    * configurable, but that is left as an exercise to the reader.
    */
   public String getVlml()
   {
      // _label comes from the BasePrompt class and is set when
      // the "setLabel" method is called
      String title = _label;
      
      if(title == null || title.length() < 1)
      {
         title = " ";
      }
      
      StringBuffer vlml = new StringBuffer();

      // Add the title text
      vlml.append("<BoxLayout name=\"ProgressPrompt\" orientation=\"vertical\">\n");
      vlml.append("  <AttachChild>\n");
      vlml.append("    <Label name=\"titleText\"\n");
      vlml.append("           text=\"" + title + "\"\n");
      vlml.append("           pointSize=\"24\"\n");
      vlml.append("           lineWrap=\"true\"\n");
      vlml.append("           justification=\"center\"\n");
      vlml.append("           color=\"000080\" />\n");
      vlml.append("  </AttachChild>\n");

      // ... and the progress bar.
      vlml.append("  <AttachChild>\n");
      vlml.append("    <ProgressBar name = \"progressBar\"\n");
      vlml.append("                 text = \" \"\n");
      vlml.append("                 orientation = \"horizontal\"\n");
      vlml.append("                 startLocation = \"left\"\n");
      vlml.append("                 backgroundColor = \"dddddd\"\n");
      vlml.append("                 foregroundColor = \"0000ff\"\n");
      vlml.append("                 percentage = \"" + percentageComplete + "\"/>\n");
      
      vlml.append("  </AttachChild>\n");
      vlml.append("</BoxLayout>\n");

      shown = true;
      
      return vlml.toString();
   }

   /**
    * We generate the complete VLML ourselves in the "getVlml" method.
    */
   protected String doGetVlml()
   {
      return "";
   }

   /* (non-Javadoc)
    * @see com.lexmark.prtapp.prompt.VlmlPrompt#doHandleEvent(java.lang.String, java.lang.String, com.lexmark.core.Element)
    */
   public PromptEventResult doHandleEvent(String component, String event, Element data) throws PromptException
   {
      // The hardware home key is handled upstream of here and will automatically
      // cancel the thread, so no need to do much here.
      return null;
   }

   /* (non-Javadoc)
    * @see com.lexmark.prtapp.prompt.Prompt#getId()
    */
   public String getId()
   {
      return ID;
   }
}
