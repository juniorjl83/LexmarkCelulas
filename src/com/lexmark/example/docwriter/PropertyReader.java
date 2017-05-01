package com.lexmark.example.docwriter;

import java.util.Locale;

import com.lexmark.prtapp.util.Messages;

public class PropertyReader
{
   private static PropertyReader instance;
   private Messages messages;
   
   private static final String PROPERTY_FILE_NAME = "Resources";
   public static final String ICON_NAME = "icon.name";

   public static PropertyReader getInstance(Locale locale) {
      if (instance == null) {
          instance = new PropertyReader(locale);
      }

      return instance;
  }

  private PropertyReader(Locale locale) {
      try {
          messages = new Messages(PROPERTY_FILE_NAME, locale, getClass().getClassLoader());
      } catch (RuntimeException e) {
          Activator.getLog().info("Error obteniendo el archivo de propiedades", e);
          throw new ServiceException("El archivo de propiedades no puede ser obtenido");
      }
  }

  public String getProperty(String key) {

      if (messages == null) {
          throw new ServiceException(
                                     new IllegalStateException(
                                                               "El archivo de propiedades no puede ser obtenido"));
      }
      return messages.getString(key);
  }
}
