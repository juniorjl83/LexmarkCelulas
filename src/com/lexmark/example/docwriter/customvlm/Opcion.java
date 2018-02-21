package com.lexmark.example.docwriter.customvlm;

public class Opcion
{
   private int numero;
   private String descripcion;
   private String valor;

   public Opcion()
   {
      super();
   }

   public Opcion(int numero, String descripcion, String valor)
   {
      super();
      this.numero = numero;
      this.descripcion = descripcion;
      this.valor = valor;
   }

   public int getNumero()
   {
      return numero;
   }

   public void setNumero(int numero)
   {
      this.numero = numero;
   }

   public String getDescripcion()
   {
      return descripcion;
   }

   public void setDescripcion(String descripcion)
   {
      this.descripcion = descripcion;
   }

   public String getValor()
   {
      return valor;
   }

   public void setValor(String valor)
   {
      this.valor = valor;
   }

   public String toString()
   {
      return "Opcion [numero=" + numero + ", descripcion=" + descripcion
            + ", valor=" + valor + "]";
   }
}
