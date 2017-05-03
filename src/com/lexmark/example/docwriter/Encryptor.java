package com.lexmark.example.docwriter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class Encryptor
{
   public static String encrypt(String value)
         throws Exception
   {
      // Create an array to hold the key
      byte[] encryptKey = "This is a test DESede key".getBytes();

      // Create a DESede key spec from the key
      DESedeKeySpec spec = new DESedeKeySpec(encryptKey);

      // Get the secret key factor for generating DESede keys
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");

      // Generate a DESede SecretKey object
      SecretKey theKey = keyFactory.generateSecret(spec);

      // Create a DESede Cipher
      Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");

      // Create an initialization vector (necessary for CBC mode)

      IvParameterSpec IvParameters = new IvParameterSpec(new byte[]
      { 12, 34, 56, 78, 90, 87, 65, 43 });

      // Initialize the cipher and put it into encrypt mode
      cipher.init(Cipher.ENCRYPT_MODE, theKey, IvParameters);

      byte[] plaintext = value.getBytes();

      // Encrypt the data
      byte[] encrypted = cipher.doFinal(plaintext);

      return encrypted.toString();
   }

   public static String decrypt(String encrypted) throws Exception
   {
      // Create an array to hold the key
      byte[] encryptKey = "This is a test DESede key".getBytes();

      // Create a DESede key spec from the key
      DESedeKeySpec spec = new DESedeKeySpec(encryptKey);

      // Get the secret key factor for generating DESede keys
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");

      // Generate a DESede SecretKey object
      SecretKey theKey = keyFactory.generateSecret(spec);

      // Create a DESede Cipher
      Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");

      // Create the initialization vector required for CBC mode
      IvParameterSpec ivParameters = new IvParameterSpec(new byte[]
      { 12, 34, 56, 78, 90, 87, 65, 43 });

      // Initialize the cipher and put it in decrypt mode
      cipher.init(Cipher.DECRYPT_MODE, theKey, ivParameters);

      // Decrypt the data
      byte[] plaintext = cipher.doFinal(encrypted.getBytes());

      String plaintextStr = new String(plaintext);

      return plaintextStr;
   }
}
