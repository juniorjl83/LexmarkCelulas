package com.lexmark.example.docwriter;

public class Log
{
   private String serial;
   private String path;
   private String date;
   private String nameCelula;
   private String nameDocument;
   private String fileType;
   private int numSheets;
   private int numBlank;

   public Log()
   {
      super();

      serial = "";
      path = "";
      date = "";
      nameCelula = "";
      nameDocument = "";
      fileType = "";
      numSheets = 0;
      numBlank = 0;
   }

   public Log(String serial, String path, String date, String nameCelula,
         String nameDocument, String fileType, int numSheets, int numBlank)
   {
      super();
      this.serial = serial;
      this.path = path;
      this.date = date;
      this.nameCelula = nameCelula;
      this.nameDocument = nameDocument;
      this.fileType = fileType;
      this.numSheets = numSheets;
      this.numBlank = numBlank;
   }

   public String getSerial()
   {
      return serial;
   }

   public void setSerial(String serial)
   {
      this.serial = serial;
   }

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   public String getDate()
   {
      return date;
   }

   public void setDate(String date)
   {
      this.date = date;
   }

   public String getNameCelula()
   {
      return nameCelula;
   }

   public void setNameCelula(String nameCelula)
   {
      this.nameCelula = nameCelula;
   }

   public String getNameDocument()
   {
      return nameDocument;
   }

   public void setNameDocument(String nameDocument)
   {
      this.nameDocument = nameDocument;
   }

   public String getFileType()
   {
      return fileType;
   }

   public void setFileType(String fileType)
   {
      this.fileType = fileType;
   }

   public int getNumSheets()
   {
      return numSheets;
   }

   public void setNumSheets(int numSheets)
   {
      this.numSheets = numSheets;
   }

   public int getNumBlank()
   {
      return numBlank;
   }

   public void setNumBlank(int numBlank)
   {
      this.numBlank = numBlank;
   }

   public String toString()
   {
      StringBuffer builder = new StringBuffer();
      builder.append(serial);
      builder.append(",");
      builder.append(path);
      builder.append(",");
      builder.append(date);
      builder.append(",");
      builder.append(nameCelula);
      builder.append(",");
      builder.append(nameDocument);
      builder.append(",");
      builder.append(numSheets);
      builder.append(",");
      builder.append(fileType);
      builder.append(",");
      builder.append(numBlank);

      return builder.toString();
   }

}
