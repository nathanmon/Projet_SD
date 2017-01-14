import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageListener implements Runnable{

	public static int myId;
	static BufferedReader in;
	public BufferedWriter out;
	private Socket socketOut;
	private int portIn;
	private int portOut;
	public Socket socketIn;
	private ServerSocket server;

	public MessageListener(int id, Socket socketOut, ServerSocket server) {

		this.server = server;
		this.myId = id;
		this.socketOut = socketOut;

		portIn = server.getLocalPort();
		portOut = socketOut.getPort();
		System.out.println("id "+myId+" port out "+portOut+" port in "+portIn);

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

		JSONObject json = null;
		//insertion dans la boucle : envoi de son port sur ecoute
		try {
			json = new JSONObject().put("id", myId).put("type", "hello").put("oldPort",socketOut.getPort()).put("newPort",portIn);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		envoyer(json);

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
			int expediteur = 0;
			json = lire();
			try {
				expediteur = (int) json.get("id");
				if(expediteur != myId){
					if(json.get("type").toString()=="msg"){//réception d'un msg
						System.out.println("ouai c un msg !");
						maLigne = (String) json.get("msg");
						System.out.println("Client "+expediteur+" : "+maLigne);
					}
					else if (json.get("type")=="hello"){//réception d'une insertion dans l'anneau
						if(socketOut.getPort()==(int)json.get("oldPort")){
							try {
								socketOut = new Socket("127.0.0.1", (int)json.get("newPort"));
								OutputStreamWriter output = new OutputStreamWriter(socketOut.getOutputStream(), "UTF-8");
								out = new BufferedWriter(output);
								System.out.println("maintenant je parle à "+socketOut.getPort());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					//faire suivre
					ecrire(maLigne);
				}

			}
			catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}



	private static JSONObject lire() {
		JSONObject json = null;
		String maLigne = null;
		System.out.println("msgL : attente d'un msg");
		try {
			maLigne = in.readLine();
			System.out.println("reception : "+maLigne);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			json = new JSONObject(maLigne);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	private void envoyer(JSONObject json) {
		try {
			out.write(json+"\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
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
				e.printStackTrace();
			}

		}
	}


}
