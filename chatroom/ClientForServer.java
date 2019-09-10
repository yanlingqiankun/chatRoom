import java.io.PrintWriter;

public class ClientForServer{
	public PrintWriter output;
	public String name;
	public BigInteger token;	//服务器和这台客户的token
	public MD5 md5;
	public Socket socket;
	public ClientForServer(PrintWriter output, String name, BigInteger token, Socket socket) {
		this.name = name;
		this.socket = socket;
		this.output = output;
		this.token = token;
		md5 = new MD5();
	}
	
	public sendMess(String mess) {
		String sign = md5.getMD5ofStr(mess+ack);
		ack = ack.add(BigInteger.ONE);
		output.println(mess+"&"+sign);
		output.flush();
	}
}