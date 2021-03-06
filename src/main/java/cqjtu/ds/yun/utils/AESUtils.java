package cqjtu.ds.yun.utils;


import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

@Component
public class AESUtils {

    //CBC模式的初始化向量IV
    private static final Integer IVSize=16;

    /**
     * 创建16位向量，不够则用0填充
     */
    private static IvParameterSpec createIv(String sKey){
        StringBuffer sb=new StringBuffer(IVSize);
        sb.append(sKey);
        if(sb.length()>IVSize){
            sb.setLength(IVSize);
        }
        if(sb.length()<IVSize){
            while (sb.length()<IVSize){
                sb.append("0");
            }
        }
        byte[] data=null;
        data=sb.toString().getBytes();
        return new IvParameterSpec(data);
    }

    /**
     * 初始化 AES Cipher
     * @param sKey
     * @param cipherMode     模式
     * @return
     */
    private static Cipher initAESCipher(String sKey,int cipherMode) {

        //创建密钥生成器
        KeyGenerator keyGenerator=null;
        //为加密和解密提供密码功能的类
        Cipher cipher=null;
        try {
            keyGenerator=KeyGenerator.getInstance("AES");//指定算法
            /*SecureRandom random=SecureRandom.getInstance("SHA1PRNG" );
            random.setSeed(sKey.getBytes());*/
            keyGenerator.init(128,new SecureRandom(sKey.getBytes()));//使用用户提供的随机源初始化此密钥生成器，使其具有确定的密钥大小。
            SecretKey secretKey=keyGenerator.generateKey();     //生成一个密钥
            byte[] codeFormat=secretKey.getEncoded();           //基本编码格式的密钥
            /*byte[] sekey=sKey.getBytes("UTF-8");
            sekey=Arrays.copyOf(sekey,16);*/
            SecretKeySpec key=new SecretKeySpec(codeFormat,"AES");//根据给定的字节数组构造一个密钥，以与 provider 无关的方式指定一个密钥。
            cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");    //转换   算法/模式/填充
            //初始化
            cipher.init(cipherMode,key,createIv(sKey.substring(sKey.length()-16,sKey.length())));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return cipher;
    }


    /**
     * 对文件进行加密
     * @param sourceFile     明文
     * @param encrypfile     密文文件
     * @param sKey           密码
     * @return               加密后的密文
     */
    public static File encryptFile(File sourceFile,File encrypfile,String sKey){

        InputStream inputStream=null;
        OutputStream outputStream=null;
        try {
            inputStream=new FileInputStream(sourceFile);
            outputStream=new FileOutputStream(encrypfile);
            //初始化加密器
            Cipher cipher=initAESCipher(sKey,Cipher.ENCRYPT_MODE);    //Cipher.ENCRYPT_MODE 加密模式的常量
            //以加密流写入文件
            CipherInputStream cipherInputStream=new CipherInputStream(inputStream,cipher);
            byte[] cache=new byte[1024];
            int nRead=0;
            while ((nRead=cipherInputStream.read(cache))!=-1){
                outputStream.write(cache,0,nRead);
                outputStream.flush();
            }
            cipherInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return encrypfile;
    }


    /**
     * 对文件解密
     * @param inputStream     密文输入流
     * @param decryptFile     明文文件
     * @param sKey            密码
     * @return                解密后的文件
     */
    public static File decryptFile(InputStream inputStream,File decryptFile,String sKey){
        OutputStream outputStream=null;
        try {
            Cipher cipher=initAESCipher(sKey,Cipher.DECRYPT_MODE);//Cipher.DECRYPT_MODE  解密模式的常量
            outputStream=new FileOutputStream(decryptFile);
            CipherOutputStream cipherOutputStream=new CipherOutputStream(outputStream,cipher);
            byte[] buffer=new byte[1024];
            int r;
            while ((r=inputStream.read(buffer))>=0){
                cipherOutputStream.write(buffer,0,r);
            }
            cipherOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return decryptFile;
    }


}
