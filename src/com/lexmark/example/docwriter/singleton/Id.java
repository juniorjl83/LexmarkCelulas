package com.lexmark.example.docwriter.singleton;

public class Id
{
   private int id;
   private String filename;
   private String filePassword;
   private String fileType;
   public Id(int id, String filename, String filePassword, String fileType)
   {
      super();
      this.id = id;
      this.filename = filename;
      this.filePassword = filePassword;
      this.fileType = fileType;
   }
   public int getId()
   {
      return id;
   }
   public void setId(int id)
   {
      this.id = id;
   }
   public String getFilename()
   {
      return filename;
   }
   public void setFilename(String filename)
   {
      this.filename = filename;
   }
   public String getFilePassword()
   {
      return filePassword;
   }
   public void setFilePassword(String filePassword)
   {
      this.filePassword = filePassword;
   }
   public String getFileType()
   {
      return fileType;
   }
   public void setFileType(String fileType)
   {
      this.fileType = fileType;
   }

   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((filePassword == null) ? 0 : filePassword.hashCode());
      result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
      result = prime * result + ((filename == null) ? 0 : filename.hashCode());
      result = prime * result + id;
      return result;
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Id other = (Id) obj;
      if (filePassword == null)
      {
         if (other.filePassword != null) return false;
      }
      else if (!filePassword.equals(other.filePassword)) return false;
      if (fileType == null)
      {
         if (other.fileType != null) return false;
      }
      else if (!fileType.equals(other.fileType)) return false;
      if (filename == null)
      {
         if (other.filename != null) return false;
      }
      else if (!filename.equals(other.filename)) return false;
      if (id != other.id) return false;
      return true;
   }
}
