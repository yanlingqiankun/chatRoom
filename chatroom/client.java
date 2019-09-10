public class client{
	private BufferedReader input;
	private PrintWriter output;
	private BigInteger token;
	private BigInteger ack;
	private Socket socket;
	private String name;
	
	private BigInteger e;
	private BigInteger n;
	private RSA encrytion;
	private RSA decrytion;
	private Random rnd;
	
	private DES des;
	
	public client(String name){
		Random rnd = new Random(new Date().getTime());
		token = new BigInteger(1024, rnd);
		this.name = name;
	}
	
	public void connect(String ip, String port, String name){
		
	}
	
	public void sendMess(String mess, boolean isImage){
		if(!isImage){
			//若只是简单的文字
			String cipher;
			des.encrypt_str(mess, false);
			cipher = des. getCiphertext_str();
			String temp = cipher + token + name;
			cipher = cipher+"&"+name+"&"+md5(temp)+"&"+"text";
			output.println(cipher);
			output.flush();
		}else{
			//如果是图片
			String cipher;
			String[] splitArray = mess.split(".");
			String type = splitArray[splitArray.length - 1]; 
			des.encrypt_str(mess, true);
			cipher = des.getCiphertext_str();
			String temp = cipher + token + name;
			cipher = cipher+"&"+name+"&"+md5(temp)+"&"+type;
			output.println(cipher);
			output.flush();
		}
	}
	
	
	public class listenRunnable implements Runnable(){
		@Override
		public void run(){
			while(socket.isConnect()){
				oral_mess = input.readLine();
				String[] mess = oral_mess.split("&");
				if(mess.length() != 4){
					System.out.println("格式不正确");
					continue;
				}
				if(name.equals(mess[1]))
					continue;	//是自己发的消息就不处理了
				if(md5.getMD5ofStr(mess[0]+ack+mess[1]).equals(mess[2])){
					String id = mess[1];
					String text = des.decrypt_string(mess[0]);
					if(mess[3].equals("text")){
						//不是图片
						String plaintext = des.getPlaintext_str();
						showMess(plaintext, id);
					}else{
						//是图片
						byte[] image = des.getPlaintext_byte();
						showImage(text, id, mess[3]);
					}
				}
			}
		}
	}
	
/*	private void recieve(String name){
		try{
			File file = new File("./"+text);
			if(file.isExist())
				file.delete();
			file.createNewFile();
			FileInputStream fis = new FileInputStream(socket.getInputStream());
			FileOutputStream fos = new FileOutputStream(file);
			byte[] bytes = new byte[128];
			int n = fis.read(bytes);
			while(n != -1){
				fos.write(bytes, 0, n);
				n = fis.read(bytes);
			}
			fis.close();
			fos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
*/	
	class clientRunnable implements Runnable{
		//握手过程
		@Override
		public void run() {
			try {
				socket = new Socket(findIP.getText(), (new Integer(findPort.getText())).intValue());
				if(socket.isConnected()) {
					output = new PrintWriter(socket.getOutputStream());
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
					output.println(name);
					output.flush();
					
					//发给server公钥
					output.println(decrytion.getE().toString(16));
					output.flush();
					output.println(decrytion.getN().toString(16));
					output.flush();
					
					//收到server公钥和token
					Random rnd = new Random(new Date().getTime());
					token = new BigInteger(1024, rnd);
					

					BigInteger E = new BigInteger(input.readLine(), 16);
					BigInteger N = new BigInteger(input.readLine(), 16);
					System.out.println("收到对方公钥E："+E.toString(16));
					System.out.println("收到对方公钥N："+N.toString(16));
					encrytion = new RSA(E, N);
					//验证服务器证书
					certi = input.readLine();
					if(!checkServer()) {
						output.println("n");
						output.flush();
						input.close();
						output.close();
						socket.close();
						System.out.println("对方或许是有人冒充");
						return;
					}
					output.println("y");
					output.flush();
					//交换token					
					String tempStr = input.readLine();
					tempStr = decrytion.decryte(tempStr, "GB2312");
					Ack = new BigInteger(tempStr);
					output.println(encrytion.encryte(token.toString()));
					output.flush();

					System.out.println("对方的序号是："+Ack);
					System.out.println("我的序号是"+token);
					
					String key = decrytion.decryte(input.readLine());
					des.setKey(key);
					
					listen = new Thread(new listenRunnable());
					listen.start();
				}
			} catch(Exception e){
				System.out.println("连接发生错误");
		}	
	}
	
	private boolean checkServer() {
		//检查服务器的真实性
		try {
			boolean isReal = false;
			String m = encrytion.getE()+"&"+encrytion.getN()+"&IP="+serverIP;
			String hash = md5.getMD5ofStr(m);
			BigInteger certiE = new BigInteger("65537");
			BigInteger certiN = new BigInteger("20074216763590379274207809820502956172438368907460460664289098486135186945831669339457161713076674322535956266172433903093234600699299712297827284853864743108531643216396407932918312345552643140776399668915892923877444016254039515412694105739871054089559816611601147135590760165400772490546755971928185385120819773308967022034084709711912116224075586806729536930889800924850839356818202488619055148064115876358333348977338976500152600092297592179986377007969399155412704983416461266412260213623901183837818857896668132553106979342888730071473623940256854059382427434876622420524808616804887755823950889412250964484311");
			BigInteger temp = new BigInteger(certi);
			BigInteger tempTemp = temp.modPow(certiE, certiN);
			if(hash.equals(tempTemp.toString(16).toUpperCase())) {
				System.out.println("--------------------------服务器证书已经过验证--------------------------");
				isReal = true;
			}
			return isReal;
		}catch(Exception e) {
			return false;
		}
	}
}