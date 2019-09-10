import java.io.*;
import java.security.*;
import javax.crypto.*;
import sun.misc.*;
import java.util.*;

public class DES {
    private Key true_key;
    private String key; //random key
    private byte[] ciphertext_byte = null;    //密文数组
    private byte[] plaintext_byte = null;  //明文数组
    private String ciphertext_str= "";  //密文字符串
    private String plaintext_str= "";  //明文字符串

    DES(){
        Random random = new Random();
        key = String.valueOf(random.nextLong());
        setKey(key);
    }

    public String getKey(){
        return key;
    }

    // 根据参数生成DES密钥
    public void setKey(String strKey){
        try{
            KeyGenerator _generator = KeyGenerator.getInstance("DES");
            _generator.init(new SecureRandom(strKey.getBytes()));
            this.true_key = _generator.generateKey();
            _generator=null;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    // 加密String明文输入,若是图片，则明文为图片地址
    public void encrypt_str(String plaintext_str, boolean isImage){
        BASE64Encoder base64en = new BASE64Encoder();
        try {
            if(!isImage){
                this.plaintext_byte = plaintext_str.getBytes("UTF8");
                this.ciphertext_byte = this.encrypt_byte(this.plaintext_byte);
                this.ciphertext_str = base64en.encode(this.ciphertext_byte);
            }
            else{
                File image = new File(plaintext_str);
                FileInputStream in = new FileInputStream(image);
                if(image.length() <= Integer.MAX_VALUE){  //若图片尺寸合适
                    byte[] graph = new byte[(int)(image.length())];    //最大是2G
                    in.read(graph);
                    this.plaintext_byte = graph;
                    this.ciphertext_byte = this.encrypt_byte(this.plaintext_byte);
                    this.ciphertext_str = base64en.encode(this.ciphertext_byte);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            this.plaintext_byte = null;
            this.ciphertext_byte = null;
        }
    }

    //加密以byte[]明文输入,byte[]密文输出
    private byte[] encrypt_byte(byte[] byteS){
        byte[] byteFina = null;
        Cipher cipher;
        try
        {
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE,true_key);
            byteFina = cipher.doFinal(byteS);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            cipher = null;
        }

        return byteFina;
    }

    // 解密:以String密文输入,String明文输出
    public void decrypt_string(String ciphertext_str){
        BASE64Decoder base64De = new BASE64Decoder();
        try
        {
            this.ciphertext_byte = base64De.decodeBuffer(ciphertext_str);
            this.plaintext_byte = this.decrypt_byte(ciphertext_byte);
            this.plaintext_str = new String(plaintext_byte,"UTF8");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            base64De = null;
            plaintext_byte = null;
            ciphertext_byte = null;
        }

    }

    // 解密以byte[]密文输入,以byte[]明文输出
    private byte[] decrypt_byte(byte[] byteD){
        Cipher cipher;
        byte[] byteFina=null;
        try{
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE,true_key);
            byteFina = cipher.doFinal(byteD);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            cipher=null;
        }
        return byteFina;
    }

    //返回加密后的密文strMi
    public String getCiphertext_str()
    {
        return ciphertext_str;
    }

    //返回解密后的明文字符串
    public String getPlaintext_str()
    {
        return plaintext_str;
    }

    //返回解密后的明文byte数组
    public byte[] getPlaintext_byte(){
        try{
            BASE64Decoder base64De = new BASE64Decoder();
            byte[] ciphertext_byte = base64De.decodeBuffer(ciphertext_str);
            byte[] plaintext_byte = this.decrypt_byte(ciphertext_byte);
            return plaintext_byte;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

/*    public static void main(String[] args)
    {
        DES des = new DES();
        System.out.println("密钥：" + des.getKey());
//        des.setKey(des.getKey());  //使用getKey()的返回值生成密钥
        String plaintext = "C://Users//Dingj//Desktop//其它//0.jpg";
//        String plaintext = "你好？";

        des.encrypt_str(plaintext,true);//加密
        String ciphertext_str = des.getCiphertext_str(); //返回密文字符串
        System.out.println("密文：" + ciphertext_str);
        if (ciphertext_str.length() == 0)              //length 为0， 表示图片过大
            System.out.println("图片过大，加密失败");

        des.decrypt_string(ciphertext_str); //解密
        String plaintext_str = des. getPlaintext_str(); //返回明文字符串
        byte[] plaintext_byte = des.getPlaintext_byte(); //返回明文byte数组
        System.out.println("明文-str: " + plaintext_str);
        System.out.println("明文-byte：" + Arrays.toString(plaintext_byte));

        if (plaintext_byte.length == 0)
            System.out.println("图片过大,解密失败");
    }*/
}
