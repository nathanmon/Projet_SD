import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageListener implements Runnable{

	BufferedReader in;
	private BufferedWriter out;
	private Socket socketOut;
	private ServerSocket server;


	public MessageListener(Socket socketOut, ServerSocket server) {
		this.socketOut = socketOut;
		this.server = server;
	}


	@Override
	public void run() {//écoute et fait passer les msgs

		int portOut = socketOut.getPort();
		int portIn = server.getLocalPort();
		System.out.println("messagerie out "+portOut+" in "+portIn);

		//écriture
		try {
			OutputStreamWriter output = new OutputStreamWriter(socketOut.getOutputStream(), "UTF-8");
			out = new BufferedWriter(output);
		} catch (NumberFormatException | IOException e2) {
			e2.printStackTrace();
		}


		//envoi du port sur écoute
		try {
			out.write(portIn+"Bonjour "+portOut+"\n");
			out.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		while(in==null){
			System.out.println("in null");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("messagerie :");

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
			//faire suivre
			if(maLigne!=null){
				System.out.println(maLigne);
				if(expediteur!=portIn){

					try {
						out.write(maLigne+"\n");
						out.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}

}
