import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class MessageListener implements Runnable{

	BufferedReader in;
	public BufferedWriter out;
	private Socket socketOut;
	private int portIn;
	private int portOut;


	public MessageListener(Socket socketOut, ServerSocket server, BufferedReader in) {

		this.socketOut = socketOut;
		this.in = in;
		
		portOut = socketOut.getPort();
		portIn = server.getLocalPort();
		System.out.println("messagerie out "+portOut+" in "+portIn);

		//écriture
		try {
			OutputStreamWriter output = new OutputStreamWriter(socketOut.getOutputStream(), "UTF-8");
			out = new BufferedWriter(output);
		} catch (NumberFormatException | IOException e2) {
			e2.printStackTrace();
		}
	}


	@Override
	public void run() {//écoute et fait passer les msgs

		Messagerie chat = new Messagerie(out, portIn);
		Thread t = new Thread(chat);
		t.start();

		while(in==null){
			System.out.println("in null");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//boucle msgs
		while(!Thread.interrupted()){
			String maLigne = null;
			System.out.println("attente msg");
			try {
				maLigne = in.readLine();
				System.out.println("reception ' "+maLigne+" '");
			} catch (IOException e) {
				e.printStackTrace();
			}
			int expediteur = Integer.parseInt(maLigne.substring(0, 5));
			System.out.println("exp: "+expediteur);
			//faire entrer un nouveau client dans la boucle
			if(maLigne.length()==18){
				if(maLigne.substring(5, 12)=="Bonjour"){
					if(Integer.parseInt(maLigne.substring(13, 18))==portIn){
						portOut=expediteur;
						System.out.println("maintenant je parle à "+portOut);
						try {
							socketOut = new Socket("127.0.0.1", portOut);
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							OutputStreamWriter output = new OutputStreamWriter(socketOut.getOutputStream(), "UTF-8");
							out = new BufferedWriter(output);
						} catch (NumberFormatException | IOException e2) {
							e2.printStackTrace();
						}
					}
				}
			}
			//faire suivre
			if(expediteur!=portIn){
				ecrire(maLigne);
			}
		}

	}

	private void ecrire(String str){
		if(str!=null){
			str=portIn+": "+str;
			System.out.println(str);

			try {
				out.write(str+"\n");
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


}
