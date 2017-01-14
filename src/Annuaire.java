import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Annuaire {

	private static BufferedWriter out;
	private static BufferedReader in;
	private static JSONArray listDest = new JSONArray();

	public static void main(String args[]) {

		ServerSocket server = null;
		Socket socket = null;
		int id = 1;

		try {
			server = new ServerSocket(12000);
			System.out.println("annuaire ecoute port 12000");
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(!Thread.interrupted()){
			try {
				socket = server.accept();
				System.out.println("connexion ! port client : "+socket.getPort());
				OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream(),"UTF-8");
				out = new BufferedWriter(output);
				InputStreamReader input = new InputStreamReader(socket.getInputStream());
				in = new BufferedReader(input);
				try {
					listDest.put(lire().get("port"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				envoyer(listDest, id);
			} catch (IOException e) {
				e.printStackTrace();
			}
			id++;
		}

	}

	private static JSONObject lire() {
		JSONObject json = null;
		String maLigne = null;
		System.out.println("attente d'un msg");
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

	private static void envoyer(JSONArray listDest, int id) {
		JSONObject json = new JSONObject();
		try {
			json.put("id", id);
			json.put("listDest", listDest);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			System.out.println("json : "+json);
			out.write(json+"\n");
			out.flush();
			System.out.println("msg envoyé");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
