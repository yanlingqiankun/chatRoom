import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;


public class MainApplication extends Application {
	//全局的变量	
	private ServerSocket serverSocket;
	private Socket socket;
	private int flag;		//0为服务器，1为客户端
	private int isExitServer;
	private Stage stage;
//	private double count;		//信息位置的计数器
	private PrintWriter output;	//输出流
	private BufferedReader input;	//输入流
	private boolean status = true;  //运行状态
	private Thread listen;		//监听线程
	private Thread serverThread;
	private Thread clientThread;
	private String serverIP;
	
	//密码需要
	private BigInteger e;
	private BigInteger n;
	private RSA encrytion;
	private RSA decrytion;
	private BigInteger token;	//自己的标志
	private BigInteger Ack;		//对方的标志
	private Random rnd;
	private MD5 md5;
	private BigInteger serverE;
	private BigInteger serverN;
	private BigInteger serverD;
	private String certi;

	
	//初始界面的组件
	private Button server;
	private Button client;
	private Pane primaryPane;
	private Scene primaryScene;
	
	//服务器等待界面组件
	private Scene waitScene;
	private Pane waitPane;
	private Label waitLabel;
	
	//客户端选择界面组件
	private Scene findScene;
	private Pane findPane;
	private Button findButton;
	private Label IPLabel;
	private Label portLabel;
	private TextField findIP;
	private TextField findPort;
	
	//聊天界面组件
	private TextArea chatField;
	private Button rsaButton;
	private VBox chatPane;
	private Pane bottomPane;
	private ScrollPane chatRecord;
	private Scene chatScene;
	private VBox chatRecoPane;
	
    @Override
    public void start(Stage primaryStage) {
    	try {
			isExitServer = 0;
    		md5 = new MD5();
    		stage = primaryStage;
    		decrytion = new RSA();
//    		count = 0;
    		//初始界面的创建
    		server = new Button("创建聊天室");
    		server.setId("server");
    		server.setPrefHeight(63.0);
    		server.setPrefWidth(134.0);
    		server.setLayoutX(128.0);
    		server.setLayoutY(139.0);
    		server.setOnAction((ActionEvent e)->{
    			createServer(e);
    		});
    		server.setTextAlignment(TextAlignment.CENTER);
    		client = new Button("登录已有聊天室");
    		client.setPrefHeight(63.0);
    		client.setPrefWidth(134.0);
    		client.setId("client");
    		client.setLayoutX(128.0);
    		client.setLayoutY(356.0);
    		client.setOnAction((ActionEvent e)->{
    			createClient(e);
    		});
    		primaryPane = new Pane();
    		primaryPane.getChildren().addAll(server, client);
    		primaryScene = new Scene(primaryPane, 400, 600);
    		//end
    		
//    		private TextArea chatField;
//    		private Button rsaButton;
//    		private VBox chatPane;
//    		private Pane bottomPane;
//    		private ScrollPane chatRecord;
//    		private Scene chatScene;
//    		private Pane chatRecoPane;
    		
    		//聊天界面的构建
    		chatRecoPane = new VBox();
    		chatRecoPane.setPrefWidth(400);
    		
//    		Label label = new Label("test");
//    		Label label1 = new Label("test1");
//    		label1.setLayoutY(600);
//    		label1.setWrapText(true);
//    		label1.setStyle("-fx-border-color: black;");
//    		chatRecoPane.getChildren().add(label);
//    		chatRecoPane.getChildren().add(label1);
    		
    		chatPane = new VBox(0);
    		chatPane.setPrefSize(400, 600);
    		chatRecord = new ScrollPane();
    		chatRecord.setPrefSize(400, 500);
    		bottomPane = new Pane();
    		bottomPane.setPrefSize(400, 100);
    		chatField = new TextArea();
    		chatField.setPrefSize(300, 100);
    		rsaButton = new Button("RSA");
    		rsaButton.setPrefSize(100, 100);
    		rsaButton.setLayoutX(300);
    		rsaButton.setLayoutY(0);
    		rsaButton.setOnAction((ActionEvent e)->{
    			
//    			System.out.println("点击RSA！！！");
    			
    			sendMess(chatField.getText());
    			chatField.setText("");
    		});
    		bottomPane.getChildren().addAll(chatField, rsaButton);
    		chatPane.getChildren().addAll(chatRecord, bottomPane);
    		chatRecord.setContent(chatRecoPane);
    		chatRecord.setHbarPolicy(ScrollBarPolicy.NEVER);
    		chatRecord.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    		chatScene = new Scene(chatPane, 400, 600);
    		//end
    		
            primaryStage.setScene(primaryScene);
            primaryStage.setTitle("聊天室");
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }    

    public static void main(String[] args) {
        launch(args);
    }
    
	private void createServer(ActionEvent event) {
		try {
			serverSocket = new ServerSocket(12345);
			if(isExitServer == 0){
				isExitServer = 1;
				//创建进入等待阻塞时的面
				waitPane = new Pane();
				waitLabel = new Label("正在等待连接。。。。。。");
				waitLabel.setFont(new Font(18));
				waitLabel.setPrefSize(300, 50);
				waitLabel.setLayoutX(50);
				waitLabel.setLayoutY(275);
				waitPane.getChildren().add(waitLabel);
				waitScene = new Scene(waitPane, 400, 600);
			}
			if(!checkCerti()){
				serverSocket.close();
				return;
			}
			stage.setScene(waitScene);
//			System.out.println("-----QUIT-------");
			//end
//			System.out.println(flag);
			serverThread = new Thread(new serverRunnable());
			serverThread.start();
		}catch(IOException e) {
			showError(new String("在创建时发生错误！"));
		}
	}


	private void createClient(ActionEvent event) {
		try {
			findPane = new Pane();
			findScene = new Scene(findPane, 400, 600);
			
			IPLabel = new Label("IP:");
			IPLabel.setLayoutX(20);
			IPLabel.setLayoutY(200);
			
			portLabel = new Label("Port:");
			portLabel.setLayoutX(20);
			portLabel.setLayoutY(300);
			
			findIP = new TextField("localhost");
			findIP.setLayoutX(100);
			findIP.setLayoutY(200);
			findIP.setPrefSize(200, 10);
			
			findPort = new TextField("12345");
			findPort.setLayoutX(100);
			findPort.setLayoutY(300);
			findPort.setPrefSize(200, 10);
			
			findButton = new Button("连接");
			findButton.setTextAlignment(TextAlignment.CENTER);
			findButton.setPrefSize(50, 15);
			findButton.setLayoutX(185);
			findButton.setLayoutY(400);
			findButton.setOnAction((ActionEvent e)->{
				serverIP = findIP.getText();
				clientThread = new Thread(new clientRunnable());
				clientThread.start();
			});
			
			findPane.getChildren().addAll(findButton, findIP, findPort, IPLabel, portLabel);
			
			stage.setScene(findScene);
			
		}catch(Exception e) {
			showError(new String("发生错误，请重试"));
		}
	}
	
	class serverRunnable implements Runnable{

		@Override
		public void run() {
			try {
				socket = serverSocket.accept();
				flag = 0;
				if(socket.isConnected()) {
		    		Random rnd = new Random(new Date().getTime());
					output = new PrintWriter(socket.getOutputStream());
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					token = new BigInteger(1024, rnd);
					
					//得到client公钥
					BigInteger E = new BigInteger(input.readLine(), 16);
					BigInteger N = new BigInteger(input.readLine(), 16);
					System.out.println("收到对方公钥E："+E.toString(16));
					System.out.println("收到对方公钥N："+N.toString(16));
					encrytion = new RSA(E, N);
					
					//分给client公钥
					output.println(decrytion.getE().toString(16));
					output.println(decrytion.getN().toString(16));
					output.flush();
//					System.out.println("-------------");

					
					//发送证书
					output.println(certi);
					output.flush();
					
					String result = input.readLine();
					if(result.equals("n")) {
						input.close();
						output.close();
						socket.close();
						serverSocket.close();
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								stage.setScene(primaryScene);
								showError("对方认为您的证书不安全\n并拒接了此次会话");
							}
						});
						return;
					}
					
					//交换token
					output.println(encrytion.encryte(token.toString()));
					output.flush();
					String temp = input.readLine();
					temp = decrytion.decryte(temp, "GB2312");
					Ack = new BigInteger(temp);
					System.out.println("对方的序号是："+Ack);
					System.out.println("我的序号是："+token);
					
//					System.out.println("++++++++++++");
					
					listen = new Thread(new listenRunnable());
					listen.start();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							stage.setScene(chatScene);
						}
						
					});
				}
			} catch (IOException e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						showError(new String("在创建时发生错误！"));
					}
					
				});
			}
		}
		
	}

	class listenRunnable implements Runnable{

		@Override
		public void run() {
			while(socket.isConnected()) {
//				System.out.println("-----listenning-------");
				try {
//					byte[] typeBytes = new byte[7];
//					Scanner in = new Scanner(socket.getInputStream());
//					byte[][] messBytes = new byte[1][];
					String messStr = input.readLine();
					if(messStr == null)
						continue;
					System.out.println("收到的原始消息："+messStr);
					String[] str = messStr.split("&");
					if(str.length != 3) {
						System.out.println("此条信息格式不正确，可能会有风险，已经屏蔽");
						return;
					}
					String mess = str[0];
					System.out.println("收到的消息MD5值为："+md5.getMD5ofStr(mess+Ack));
					if(!check(str[0], Ack, str[1])) {
						System.out.println("消息验证未通过，已屏蔽");
						return;
					}
					System.out.println("--------------------------上面消息已经经过验证--------------------------");
					mess = decrytion.decryte(mess, str[2]);
					
					final String finalString = mess;
					
					Ack = Ack.add(BigInteger.ONE);
					
//					System.out.println(messStr);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
//							System.out.println("------show-------");
							showMess(finalString, false);
						}
					});
//					in.close();
				} catch (Exception e) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
//							showError("发生错误，请检查网络");
						}
					});
				}
			}
		}
		
	}
	
	private boolean check(String mess, BigInteger ack, String sign) {
		String str = md5.getMD5ofStr(mess+ack);
		return str.equals(sign);
	}
	
	class clientRunnable implements Runnable{

		@Override
		public void run() {
			try {
				socket = new Socket(findIP.getText(), (new Integer(findPort.getText())).intValue());
				if(socket.isConnected()) {
					output = new PrintWriter(socket.getOutputStream());
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
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
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								showError("对方或许是有人冒充\n已断开通信");
							}
						});
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
					
					
					
					flag = 1;
//					System.out.println("已连接");
					listen = new Thread(new listenRunnable());
					listen.start();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							stage.setScene(chatScene);
						}
						
					});
				}
			} catch(Exception e){
				Platform.runLater(new Runnable(){
					@Override
					public void run(){
						showError(new String("发生错误，请重试(client)"));						
					}
					
				});
			}
		}
		
	}
	
	private void sendMess(String mess) {
		if(socket.isConnected()) {
//			System.out.println("prepare to send message");
			
//			System.out.println("准备send！！！");
			
			if(mess.equals(new String("")) || mess == null) {
				return ;
			}else {
//				InputStream is = socket.getInputStream();
				try {
					showMess(mess, true);
					String encoding = getEncoding(mess);
					mess = encrytion.encryte(mess);
					String sign = md5.getMD5ofStr(mess+token);
					mess = mess +"&"+sign+"&"+encoding;
					output.println(mess);
					output.flush();
					token = token.add(BigInteger.ONE);
				} catch (Exception e) {
					showError(new String("发生错误，请重试"));
				}
			}
		}else return;
	}
	
	private void showError(String mess) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("ERROR");
		System.out.println("ERROR");
//		e.printStackTrace();
		alert.setContentText(mess);
		alert.show();
	}

	private void showMess(String mess, boolean isMe) {
		if(mess == null)
			return;
		if(isMe)
			mess = "我："+mess;
		else {
			//mess = decrytion.decryte(mess);
			mess = "他："+mess;
		}
		Label textview = new Label(mess);
		textview.setFont(new Font(20));
		textview.setWrapText(true);
//		textview.setMaxWidth(300);
//		textview.setPrefWidth(300);
//		textview.setMinHeight(6);
		StackPane textpane = new StackPane();
//		textpane.setStyle("-fx-border-color: red");
		textpane.getChildren().add(textview);
//		textpane.setPrefWidth(400);
//		textpane.setMaxWidth(300);
		if(isMe) {
//			textpane.setMaxWidth(400);
//			textpane.setMinWidth(00);
			textview.setMaxWidth(300);
			textpane.setAlignment(Pos.BOTTOM_RIGHT);
			textview.setTextAlignment(TextAlignment.LEFT);
			textview.setStyle("-fx-border-color: red;");
		}else {

			textview.setMaxWidth(300);
			textpane.setAlignment(Pos.BOTTOM_LEFT);
			textview.setTextAlignment(TextAlignment.LEFT);
			textview.setStyle("-fx-border-color: black;");
		}
//		textview.setLayoutY(count+5);
//		count = count + textview.BASELINE_OFFSET_SAME_AS_HEIGHT+ 5;
		chatRecoPane.getChildren().add(textpane);
		chatRecord.setVvalue(1);
//		System.out.println(mess);
	}
	
	private String getEncoding(String str) {      
         String encode = "GB2312";      
        try {      
            if (str.equals(new String(str.getBytes(encode), encode))) {      
                 String s = encode;      
                return s;      
             }      
         } catch (Exception exception) {      
         }      
         encode = "ISO-8859-1";      
        try {      
            if (str.equals(new String(str.getBytes(encode), encode))) {      
                 String s1 = encode;      
                return s1;      
             }      
         } catch (Exception exception1) {      
         }      
         encode = "UTF-8";      
        try {      
            if (str.equals(new String(str.getBytes(encode), encode))) {      
                 String s2 = encode;      
                return s2;      
             }      
         } catch (Exception exception2) {      
         }      
         encode = "GBK";      
        try {      
            if (str.equals(new String(str.getBytes(encode), encode))) {      
                 String s3 = encode;      
                return s3;      
             }      
         } catch (Exception exception3) {      
         }      
        return "";      
     }   

	@Override
	public void stop() {
		if(socket.isConnected()) {
			try {
				socket.close();
				serverSocket.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
		try {
			super.stop();
			serverThread.interrupt();
			clientThread.interrupt();
			listen.interrupt();
		} catch (Exception e) {
		}finally {
			System.exit(0);
		}
	}
	
	private boolean checkCerti() {
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
	
	private void makeKeyFile() {
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
			showError("公钥和私钥已经刷新\n请前往证书颁布机构进行验证");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkServer() {
		try {
			boolean isReal = false;
			String m = encrytion.getE()+"&"+encrytion.getN()+"&IP="+serverIP;
//			System.out.println(m);
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

