

public class server{
	private ArrayList<ClientForServer> clients;
	private ServerSocket serverSocket;
	
	//����ѧ��Ҫ
	private BigInteger e;
	private BigInteger n;
	private RSA encrytion;
	private RSA decrytion;
	private Random rnd;
	private MD5 md5;
	private BigInteger serverE;
	private BigInteger serverN;
	private BigInteger serverD;
	private String certi;
	private DES des;
	private String key;
	
	public static void main(String[] args) {
		md5 = new MD5();
		des = new DES();
		key = des.getKey();
		if(checkCerti()){
			createServer();
		}
	}
	
	private void createServer() {
		//����������
		serverSocket = new ServerSocket(12345);
		new Thread(new serverRunnable()).start();
	}
	
	class serverRunnable implements Runnable{
		//�����߳�
		@Override
		public void run() {
			try {
				while(true){
					socket = serverSocket.accept();
					if(socket.isConnected()) {
						Random rnd = new Random(new Date().getTime());
						token = new BigInteger(1024, rnd);
						PrintWriter output = new PrintWriter(socket.getOutputStream());
						BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						
						//�õ�����
						String name = input.readLine();
						
						//�õ�client��Կ
						BigInteger E = new BigInteger(input.readLine(), 16);
						BigInteger N = new BigInteger(input.readLine(), 16);
						System.out.println("�յ��Է���ԿE��"+E.toString(16));
						System.out.println("�յ��Է���ԿN��"+N.toString(16));
						encrytion = new RSA(E, N);
						
						//�ָ�client��Կ
						output.println(decrytion.getE().toString(16));
						output.println(decrytion.getN().toString(16));
						output.flush();

						//����֤��
						output.println(certi);
						output.flush();
						
						String result = input.readLine();
						
						if(result.equals("n")) {
							input.close();
							output.close();
							socket.close();
							System.out.println("�Է���Ϊ������ȫ�����ܾ��˴˴λỰ");
							return;
						}
						
						//����token
						output.println(encrytion.encryte(token.toString()));
						output.flush();
						String temp = input.readLine();
						temp = decrytion.decryte(temp, "GB2312");
						Ack = new BigInteger(temp);
						System.out.println("�Է�������ǣ�"+Ack);
						System.out.println("�ҵ�����ǣ�"+token);
						//���͹�Կ
						output.println(encrytion.encryte(key));
						output.flush();
						
						ClientForServer client = new ClientForServer(output, name, token, ack);
						clients.add(client);
						
						new Thread(new ClientHandler(input, Ack, name, socket)).start();
					}	
				}
			} catch (IOException e) {
				System.out.println("�Է���Ϊ������ȫ�����ܾ��˴˴λỰ");
			}
		}
	}
	
	private class ClientHandler implements Runnable{
		// ��ÿһ���߳̽��м���
		BufferedReader input;
		Socket socket;
		String name;
		BigInteger ack;
		public ClientHandler(BufferedReader input, BigInteger ack, String name, Socket socket){
			this.input = input;
			this.name = name;
			this.socket = socket;
			this.ack = ack;
		}
		
		@Override
		public void run(){
			while((String oral_mess = input.readLine()).equals != null){
				try{
					String[] mess = oral_mess.split("&");
					if(mess.length != 4){
						System.out.println("�յ�һ������"+name+"����Ч��Ϣ");
						continue;
					}
					if(md5.getMD5ofStr(mess[0]+ack+name).equals(mess[2])){
						broadCast(mess[0]+"&"+name+"&"+md5(mess[0]+token+name)+"&"+mess[3]);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

/*	private void broadCastImage(File file){
		try{
			Iterator<ClientForServer> it = clients.iterator();
			while(it.hasNext()){
				ClientForServer cfs = (ClientForServer)it.next();
				FileInputStream fis = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(cfs.socket.getOutputStream());
				byte[] bytes = new byte[128];
				int n = fis.read(bytes);
				while(n != -1){
					fos.write(bytes, 0, n);
					n = fis.read(bytes);
				}
				fos.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
*/	
	broadCast(String mess){
		//����Ϣ�㲥�������ͻ���
		Iterator<ClientForServer> it = clients.iterator();
		while(it.hasNext()){
			ClientForServer cfs = (ClientForServer)it.next();
			cfs.send(mess);
		}
	}

	
	private void makeKeyFile() {
		//��֤�鲻���ڣ�����֤��
		File publicK = new File(".\\public.key");
		File privateK = new File(".\\private.key");
		try {
			if(publicK.exists()) {
				publicK.delete();
				publicK.createNewFile();
			}
			if(privateK.exists()) {
				privateK.delete();
				privateK.createNewFile();
			}
			PrintWriter pubStream = new PrintWriter(new FileOutputStream(publicK));
			PrintWriter priStream = new PrintWriter(new FileOutputStream(privateK));
			pubStream.println(decrytion.getE()+"&"+decrytion.getN()+"&"+"IP=");
			pubStream.close();
			priStream.println(decrytion.getD());
			priStream.close();
			System.out.println("��Կ��˽Կ�Ѿ�ˢ��\n��ǰ��֤��䲼����������֤");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkCerti() {
		//��鱾��֤���Ƿ����
		boolean Certi = false;
		File publicK = new File(".\\public.key");
		File privateK = new File(".\\private.key");
		File certificate = new File(".\\Certificate.cert");
		if(publicK.exists() && privateK.exists() && certificate.exists()) {
			try {
				Scanner pubStream = new Scanner(new FileInputStream(publicK));
				Scanner priStream = new Scanner(new FileInputStream(privateK));
				Scanner certiStream = new Scanner(new FileInputStream(certificate));
				String pubStr = pubStream.nextLine();
				String[] temp = pubStr.split("&");
				if(temp.length != 3) {
					pubStream.close();
					priStream.close();
					certiStream.close();
					makeKeyFile();
					throw new Exception();
				}
				serverE = new BigInteger(temp[0]);
				serverN = new BigInteger(temp[1]);
				serverD = new BigInteger(priStream.nextLine());
				certi = certiStream.nextLine();
				decrytion.setKey(serverE, serverN, serverD);
				Certi = true;
				pubStream.close();
				priStream.close();
				certiStream.close();
			}catch(Exception e){
				e.printStackTrace();
				Certi = false;
				makeKeyFile();
			}
		}else{
			Certi = false;
			makeKeyFile();
		}
		return Certi;
	} 

}