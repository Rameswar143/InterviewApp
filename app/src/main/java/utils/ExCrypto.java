package utils;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ExCrypto {
    public static String encrypt(String value, String key)
    {
        try {
            byte[] value_bytes = value.getBytes("UTF-8");
            byte[] key_bytes = getKeyBytes(key);
            return Base64.encodeToString(encrypt(value_bytes, key_bytes, key_bytes), Base64.NO_WRAP);
        } catch (Exception ex) {
        }
        return "";
    }
    public static byte[] encrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3)
    {
        try {
            Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            localCipher.init(1, new SecretKeySpec(paramArrayOfByte2, "AES"), new IvParameterSpec(paramArrayOfByte3));
            return localCipher.doFinal(paramArrayOfByte1);
        } catch (Exception e) {
        }
        return new byte[0];
    }
    public static String decrypt(String value, String key)
    {
        try {
            byte[] value_bytes = Base64.decode(value, 0);
            byte[] key_bytes = getKeyBytes(key);
            return new String(decrypt(value_bytes, key_bytes, key_bytes), "UTF-8");
        }
        catch(Exception ex) {
        }
        return "";
    }
    public static byte[] decrypt(byte[] ArrayOfByte1, byte[] ArrayOfByte2, byte[] ArrayOfByte3)
    {
        try {
            Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            localCipher.init(2, new SecretKeySpec(ArrayOfByte2, "AES"), new IvParameterSpec(ArrayOfByte3));
            return localCipher.doFinal(ArrayOfByte1);
        }
        catch(Exception ex) {
        }
        return new byte[0];
    }
    private static byte[] getKeyBytes(String paramString)
    {
        try {
            byte[] arrayOfByte1 = new byte[16];
            byte[] arrayOfByte2 = paramString.getBytes("UTF-8");
            System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 0, Math.min(arrayOfByte2.length, arrayOfByte1.length));
            return arrayOfByte1;
        }
        catch(Exception ex) {
        }
        return new byte[0];
    }
}
