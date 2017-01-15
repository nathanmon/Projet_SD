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
	static BufferedWriter out;
	static BufferedReader in;
	private static JSONArray listDest;
	static Socket socketOut = null;
	static ServerSocket server = null;
	static Socket socketIn = null;

	public static void main(String[] args) throws InterruptedException {
		InputStreamReader input = null;
		OutputStreamWriter output = null;
		int portOut = 0;
		int portIn = 0;
		JSONObject json = null;

		//creation de son ecoute dans la boucle
		try {
			server = new ServerSocket(0);
			portIn = server.getLocalPort();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//connexion à l'annuaire
		try {
			socketOut = new Socket("127.0.0.1", 12000);
			output = new OutputStreamWriter(socketOut.getOutputStream());
			out = new BufferedWriter(output);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//envoi du port sur ecoute
		try {
			json = new JSONObject().put("port", portIn);
			envoyer(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		//récupération de mon id et du port du 1er client
		try {
			input = new InputStreamReader(socketOut.getInputStream());
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		in = new BufferedReader(input);
		try {
			json = lire();
			myId = json.getInt("id");
			listDest = (JSONArray) json.get("listDest");
			portOut = (int) listDest.get(0);
		} catch (NumberFormatException | JSONException e2) {
			e2.printStackTrace();
		}
		try {
			socketOut = new Socket("127.0.0.1", portOut);
			output = new OutputStreamWriter(socketOut.getOutputStream());
			out = new BufferedWriter(output);
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		System.out.println("je suis le Client "+myId+", je parle au port "+socketOut.getPort());

		//insertion dans la boucle : envoi de son port sur ecoute
		try {
			json = new JSONObject().put("type", "hello").put("oldPort",Client.socketOut.getPort()).put("newPort",Client.server.getLocalPort()).put("id", myId);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		envoyer(json);
		
		MessageListener msgL = new MessageListener();
		Thread t = new Thread(msgL);
		t.start();

		Messagerie chat = new Messagerie();
		Thread t2 = new Thread(chat);
		t2.start();

		//boucle attente de nouveau client
		while(!Thread.interrupted()){
			//écoute
			try {
				socketIn = server.accept();
				in=new BufferedReader(new InputStreamReader(socketIn.getInputStream()));
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

}
