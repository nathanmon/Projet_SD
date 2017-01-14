import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {

	public static int myId = 1;
	private static BufferedWriter out;
	private static BufferedReader in;
	private static JSONArray listDest;
	public static void main(String[] args) {
		ServerSocket server = null;
		Socket socketOut = null;
		Socket socketIn = null;
		InputStreamReader input = null;
		OutputStreamWriter output = null;
		int portOut = 0;
		int portIn = 0;
		JSONObject json = null;

		//creation de son ecoute dans la boucle
		try {
			server = new ServerSocket(0);
			portIn = server.getLocalPort();
			System.out.println("j'ecouterai port "+portIn);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//connexion à l'annuaire
		try {
			socketOut = new Socket("127.0.0.1", 12000);
			output = new OutputStreamWriter(socketOut.getOutputStream(), "UTF-8");
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
		System.out.println("attente du port du 1er client");
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
			output = new OutputStreamWriter(socketOut.getOutputStream(), "UTF-8");
			out = new BufferedWriter(output);
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		System.out.println(myId+" je parle au port "+socketOut.getPort());

		MessageListener msgL = new MessageListener(myId, socketOut, server);
		Thread t = new Thread(msgL);
		t.start();
		
		//boucle attente de nouveau client
		while(!Thread.interrupted()){
			//écoute
			System.out.println("attente connexion d'un client n-1");
			try {
				socketIn = server.accept();
				System.out.println("connexion !");
				msgL.in=new BufferedReader(new InputStreamReader(socketIn.getInputStream()));
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

	}

	private static void envoyer(JSONObject json) {
		try {
			out.write(json+"\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		


	private static JSONObject lire() {
		JSONObject json = null;
		String maLigne = null;
		System.out.println("attente d'un msg");
		try {
			maLigne = in.readLine();
			System.out.println("reception client : "+maLigne);
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
