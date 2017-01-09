import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;

public class Annuaire {

	private static int myId;
	private static BufferedWriter out;
	static ArrayList dest = new ArrayList();
	
	public static void main(String Args[]) {

		ServerSocket server = null;
		Socket socket = null;
		myId = 0;
		int port = 0;
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
				OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream(),"UTF-8");
				out = new BufferedWriter(output);
				InputStreamReader input = new InputStreamReader(socket.getInputStream());
				BufferedReader in = new BufferedReader(input);
				out.write(port+"\n");
				out.flush();
				//ajouterDest(socket.getPort());
				//envoyer("ID", id);
				//envoyer("DEST", port);
				if(port==0){
					port = Integer.parseInt(in.readLine());
					System.out.println("1er client ecoute port "+port);
				}
				else
					System.out.println("nouveau client");
				id++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	
	private static void ajouterDest(int port) {
		dest.add(port);
		
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
