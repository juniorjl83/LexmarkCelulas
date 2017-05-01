package com.lexmark.example.docwriter;

import org.osgi.framework.BundleContext;
import com.lexmark.prtapp.util.AppLogRef;
import com.lexmark.prtapp.util.PrtAppActivator;

/**
 * This is the activator for "celulas" bundle.
 */

public class Activator extends PrtAppActivator
{
   static private AppLogRef _log = null;

   // Return AppLogRef so logging can be done easily.  Should not be null.
   public static AppLogRef getLog()
   {
      return _log;
   }

   // Set up the log ref and then do the regular activation
   public void start(BundleContext bc) throws Exception
   {
      _log = new AppLogRef(bc);

      super.start(bc);
   }
}
