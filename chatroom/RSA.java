
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.io.UnsupportedEncodingException;

public class RSA {
	private Base64.Decoder decoder = Base64.getDecoder();
	private Base64.Encoder encoder = Base64.getEncoder();
	private BigInteger p = BigInteger.ONE;
	private BigInteger q = BigInteger.ONE;
	private BigInteger n = BigInteger.ONE;
	private BigInteger k = BigInteger.ONE;
	private BigInteger e = BigInteger.ONE;
	private BigInteger y = BigInteger.ONE;
	private BigInteger phi = BigInteger.ZERO;
	private final int MAX_BYTES_LENGTH = 240;
	int flag = 0;
	
	public RSA() {
		flag = 0;
		e = BigInteger.valueOf(65537);
		while(phi.mod(e).compareTo(BigInteger.ZERO) == 0 || phi.compareTo(e) != 1) {
			Random rnd = new Random(new Date().getTime());
			p = BigInteger.probablePrime(1024, rnd);
			rnd = new Random(new Date().getTime());
			q = BigInteger.probablePrime(1024, rnd);
			n = p.multiply(q);
			phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		}
		ExGcd(e, phi);
	}
	
	private  BigInteger ExGcd(BigInteger a, BigInteger b)
	{
		if (b.equals(BigInteger.ZERO))
		{
			k = BigInteger.ONE;
			y = BigInteger.ONE;
			return a;
		} else
		{

			BigInteger r = ExGcd(b, a.mod(b));
			BigInteger t = k;
			k = y;
			y = t.subtract(a.divide(b).multiply(y));

			return r;
		}

	}

	public RSA(BigInteger e, BigInteger n) {
		flag = 1;
		this.e = e;
		this.n = n;
	}
	
	public String encryte(String mess) throws IOException {
		if(flag == 0)
			return "---------";
		
		String encryteMess = new String("");
		byte[] messByte = mess.getBytes();
		InputStream is = new ByteArrayInputStream(messByte);
		int count = messByte.length/MAX_BYTES_LENGTH;
		int rest = messByte.length%MAX_BYTES_LENGTH;
		int i = 0;
		for(;i<count;i++) {
			byte[] tempByte = new byte[MAX_BYTES_LENGTH];
			is.readNBytes(tempByte, 0, MAX_BYTES_LENGTH);
			BigInteger tempBig = new BigInteger(1, tempByte);
			String Hex = tempBig.modPow(e, n).toString(16).toUpperCase();
			String str = encoder.encodeToString(Hex.getBytes());
			encryteMess = encryteMess + str + "-";
		}
		byte[] restByte = new byte[rest];
		is.readNBytes(restByte, 0, rest);
		BigInteger tempBig = new BigInteger(1, restByte);
		String Hex = tempBig.modPow(e, n).toString(16).toUpperCase();
		String str = encoder.encodeToString(Hex.getBytes());
		encryteMess = encryteMess + str;
		return encryteMess;
	}
	
	public String decryte(String mess, String encode) {
		if(flag == 1)
			return "++++++++++";
		
		String decryteStr = new String("");
		String[] Hex = mess.split("-");
		StringBuffer buffer = new StringBuffer();
		for(int i = 0;i<Hex.length;i++) {
			Hex[i] = new String(decoder.decode(Hex[i]));
		}
//		OutputStream os = new OutputStream();
		byte[][] byteArr = new byte[Hex.length][];
		for(int i = 0;i<Hex.length;i++) {
			BigInteger tempInt = new BigInteger(Hex[i], 16);
			tempInt = tempInt.modPow(k, n);
			byteArr[i] = tempInt.toByteArray();
		}
		byte[] decryteBytes = new byte[0];
		
		for(int i = 0;i<byteArr.length;i++) {
			decryteStr = decryteStr + new String(byteArr[i]);
			byte[] temp =  unitByteArray(decryteBytes, byteArr[i]);
			decryteBytes = temp;
		}
/*		try{
			return new String(decryteBytes, encode);
			//		return decryteStr;
		}catch(UnsupportedEncodingException e){
			return "ERROR";
		}*/
		return decryteStr;
	}
	
	public BigInteger getE() {
//		return new String(decoder.decode(e.toString()));
		return e;
	}
	
	public BigInteger getN() {
//		return new String(decoder.decode(n.toString()));
		return n;
	}
	
	private byte[] unitByteArray(byte[] byte1,byte[] byte2){
		byte[] unitByte = new byte[byte1.length + byte2.length];
		System.arraycopy(byte1, 0, unitByte, 0, byte1.length);
		System.arraycopy(byte2, 0, unitByte, byte1.length, byte2.length);
		return unitByte;
	}
	
	public void setKey(BigInteger e, BigInteger n, BigInteger k) {
		this.e = e;
		this.n = n;
		this.k = k;
	}
	public BigInteger getD() {
		return k;
	}
}

