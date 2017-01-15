
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageListener implements Runnable{


	public MessageListener() {
	}


	@Override
	public void run() {//écoute et fait passer les msgs

		JSONObject json = null;

		while(true){
			//boucle msgs
			while(!Thread.interrupted()){
				String maLigne = null;
				int expediteur = 0;
				json = Client.lire();
				String type = null;
				try {
					type = json.getString("type");
				} catch (JSONException e2) {
					e2.printStackTrace();
				}
				try {
					expediteur = json.getInt("id");
					if(expediteur != Client.myId){
						if(type.equals("msg")){//réception d'un msg
							maLigne = json.getString("msg");
							System.out.println("Client "+expediteur+" : "+maLigne);
						}
						else if (type.equals("hello")){//réception d'une insertion dans l'anneau
							if(Client.socketOut.getPort()==json.getInt("oldPort")){
								try {
									Client.socketOut = new Socket("127.0.0.1",json.getInt("newPort"));
									Client.out = new BufferedWriter(new OutputStreamWriter(Client.socketOut.getOutputStream()));
									System.out.println("maintenant je parle à "+Client.socketOut.getPort());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
						}
						//faire suivre
						Client.envoyer(json);
					}
				}
				catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
