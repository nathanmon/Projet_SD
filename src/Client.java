import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {

	public static int myId = 1;
	public static BufferedWriter out;
	public static BufferedReader in;
	public static Socket socketOut = null;
	public static ServerSocket server = null;
	public static Socket socketIn = null;
	public static int precedent;
	private static JSONArray listDest;

	public static void main(String[] args) throws InterruptedException {
		
		//creation de son ecoute dans la boucle
		try {
			server = new ServerSocket(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//connexion à l'annuaire
		try {
			socketOut = new Socket("127.0.0.1", 12000);
			out = new BufferedWriter(new OutputStreamWriter(socketOut.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//envoi du port sur ecoute
		try {
			envoyer(new JSONObject().put("port", server.getLocalPort()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		//récupération de mon id et du port du 1er client
		try {
			in = new BufferedReader(new InputStreamReader(socketOut.getInputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			JSONObject json = lire();
			myId = json.getInt("id");
			listDest = (JSONArray) json.get("listDest");
			if(listDest.length()>1)
				socketOut = new Socket("127.0.0.1", listDest.getInt(1));
			else
				socketOut = new Socket("127.0.0.1", listDest.getInt(0));
			out = new BufferedWriter(new OutputStreamWriter(socketOut.getOutputStream(),"UTF-8"));
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		System.out.println("je suis le Client "+myId+", j'ecoute port "+server.getLocalPort());

		//insertion dans la boucle : envoi de son port sur ecoute
		try {
			envoyer(new JSONObject().put("type", "hello").put("oldPort",socketOut.getPort()).put("newPort",server.getLocalPort()).put("id", myId));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		MessageListener msgL = new MessageListener();
		Thread t = new Thread(msgL);
		t.start();

		Messagerie chat = new Messagerie();
		Thread t2 = new Thread(chat);
		t2.start();

		//server.

		//boucle attente de nouveau client
		while(!Thread.interrupted()){
			//écoute
			try {
				socketIn = server.accept();
				in=new BufferedReader(new InputStreamReader(socketIn.getInputStream(),"UTF-8"));
				t.interrupt();
				t = new Thread(msgL);
				t.start();
				System.out.println("un client s'est connecté");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

	}

	static void envoyer(JSONObject json) {
		System.out.println("envoi "+json+" au port "+socketOut.getPort());
		try {
			out.write(json+"\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	


	static JSONObject lire() {
		JSONObject json = null;
		String maLigne = null;
		try {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
			}
			maLigne = in.readLine();
			System.out.println("reception : "+maLigne);
			try {
				json = new JSONObject(maLigne);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			//insertion dans la boucle : envoi de son port sur ecoute
			try {
				if(precedent!=socketOut.getPort())
					envoyer(new JSONObject().put("type", "hello").put("oldPort",precedent).put("newPort",server.getLocalPort()).put("id", myId));
				else{
					try {
						socketOut = new Socket("127.0.0.1",precedent);
						out = new BufferedWriter(new OutputStreamWriter(Client.socketOut.getOutputStream(),"UTF-8"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return json;
	}

}
