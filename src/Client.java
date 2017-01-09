import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {

	public static int myId;
	private static BufferedWriter out;
	private static BufferedReader in;
	public static void main(String[] args) {
		ServerSocket server = null;
		Socket socketOut = null;
		Socket socketIn = null;

		//creation de son ecoute dans la boucle
		int portIn = 0;
		try {
			server = new ServerSocket(0);
			portIn = server.getLocalPort();
			System.out.println("j'ecouterai port "+portIn);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//connection à l'annuaire
		try {
			socketOut = new Socket("127.0.0.1", 12000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String maLigne = null;
		int portOut = 0;
		InputStreamReader input = null;
		OutputStreamWriter output = null;
		//récupération du port du 1er client
		try {
			input = new InputStreamReader(socketOut.getInputStream());
			in = new BufferedReader(input);
			System.out.println("attente du port du 1er client");
			try {
				maLigne = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			portOut = Integer.parseInt(maLigne);
			//si on est le 1er client envoi du port écouté à l'annuaire
			if(portOut == 0){
				portOut=portIn;
				try {
					output = new OutputStreamWriter(socketOut.getOutputStream(), "UTF-8");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				out = new BufferedWriter(output);
				try {
					out.write(portIn+"\n");
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//envoyer("")
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//connexion à un client
		try {
			socketOut = new Socket("127.0.0.1", portOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("je parle à "+socketOut.getPort());

		//envoi du port sur écoute
		try {
			out.write(portIn+"Bonjour "+portOut+"\n");
			out.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while(!Thread.interrupted()){
			//écoute
			System.out.println("attente de connexion");
			try {
				socketIn = server.accept();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			portIn = socketIn.getLocalPort();
			System.out.println("connexion "+portIn);
			try {
				input = new InputStreamReader(socketIn.getInputStream());
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			MessageListener msgL = new MessageListener(socketOut, server, new BufferedReader(input));
			Thread t = new Thread(msgL);
			t.start();
			msgL.in = new BufferedReader(input);

		}

	}
	
	private static void envoyer(String type, Object msg) {
		JSONObject json = new JSONObject();
		try {
			json.put("id", myId);
			json.put("type", type);
			json.put("msg", msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			out.write(json.toString());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
