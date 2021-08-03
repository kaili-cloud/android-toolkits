package com.winsth.libs.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class SecurityUtil {
    private static final String ALGORITHM = "DES";
    private static final String ENCODINGTYPE = ConfigUtil.TextCode.GB2312;

    private SecurityUtil() {
    }

    /**
     * 加密
     *
     * @param string 原始字符串
     * @return 加密后的字符串
     */
    public static byte[] Encryption(byte[] key, String string) {
        byte[] encryptedData = null;

        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            // 从原始密钥数据创建一个DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);
            // 创建一个密钥工厂，然后用他把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey sk = skf.generateSecret(dks);
            // Cipher对象实际完成加密
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 用密钥初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, sk, sr);
            // 获取数据并加密
            byte[] data = string.getBytes(ENCODINGTYPE);
            // 正式执行加密操作
            encryptedData = cipher.doFinal(data);
        } catch (InvalidKeyException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "InvalidKeyException:" + e.getMessage(), true);
        } catch (NoSuchAlgorithmException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "NoSuchAlgorithmException:" + e.getMessage(), true);
        } catch (InvalidKeySpecException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "InvalidKeySpecException:" + e.getMessage(), true);
        } catch (NoSuchPaddingException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "NoSuchPaddingException:" + e.getMessage(), true);
        } catch (IllegalBlockSizeException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "IllegalBlockSizeException:" + e.getMessage(), true);
        } catch (BadPaddingException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "BadPaddingException:" + e.getMessage(), true);
        } catch (UnsupportedEncodingException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "UnsupportedEncodingException:" + e.getMessage(), true);
        }

        return encryptedData;
    }

    /**
     * 解密
     *
     * @param key         密钥
     * @param encryptData 加过密的数据
     * @return 返回解密后的字符串
     */
    public static String Decryption(byte[] key, byte[] encryptData) {
        String result = "";

        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            // 从原始密钥数据创建一个DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);
            // 创建一个密钥工厂，然后用他把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey sk = skf.generateSecret(dks);
            // Cipher对象实际完成加密
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 用密钥初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, sk, sr);
            // 正式执行解密操作
            byte[] decryptedData = cipher.doFinal(encryptData);

            result = new String(decryptedData);
        } catch (InvalidKeyException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "InvalidKeyException:" + e.getMessage(), true);
        } catch (NoSuchAlgorithmException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "NoSuchAlgorithmException:" + e.getMessage(), true);
        } catch (InvalidKeySpecException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "InvalidKeySpecException:" + e.getMessage(), true);
        } catch (NoSuchPaddingException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "NoSuchPaddingException:" + e.getMessage(), true);
        } catch (IllegalBlockSizeException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "IllegalBlockSizeException:" + e.getMessage(), true);
        } catch (BadPaddingException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new
					Exception()), "BadPaddingException:" + e.getMessage(), true);
        }

        return result;
    }

    /**
     * 根据给定的字符串产生密钥
     *
     * @param key 密钥字符串
     * @return 返回密钥
     */
    public static byte[] getSecretKey(String key) {
        byte[] secretKey = null;

        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            // 为DES算法生成一个KeyGenerator对象
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
            kg.init(sr);
            // 生成密钥
            SecretKey sk = kg.generateKey();
            // 获取密钥数据
            secretKey = sk.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new Exception()), "NoSuchPaddingException:" + e.getMessage(), true);
        }

        return secretKey;
    }

    /**
     * 返回字符的MD5格式
     * @param strSrc 字符串
     * @return
     */
    public static String md5(String strSrc) {
        return encrypt(strSrc, "MD5");
    }
    /**
     * 加密字符串
     * @param strSrc 字符串
     * @param encName MD5, SHA-1, SHA-256
     * @return
     */
    private static String encrypt(String strSrc, String encName) {
        // parameter strSrc is a string will be encrypted,
        // parameter encName is the algorithm name will be used.
        // encName dafault to "MD5"
        MessageDigest md = null;
        String strDes = null;

        byte[] bt = strSrc.getBytes();
        try {
            if (encName == null || encName.equals("")) {
                encName = "MD5";
            }
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Invalid algorithm.");
            return null;
        }
        return strDes;
    }
    /**
     *
     * @param bts 字节
     * @return
     */
    private static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

    public static String getFileMD5(File file) throws UnsupportedEncodingException {
        if (file == null || !file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }

        } catch (Exception e) {
            Log.e("", "e："+e);
            return null;
        }
        finally{
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                Log.e("", "e："+e);
            }
        }
        return DigestUtil.md5DigestAsHex(digest.digest());
    }

    //根据byte获取md5
    public static String getMd5ByFile(File file) throws Exception {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}
