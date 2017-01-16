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
				int expediteur = 0;
				json = Client.lire();
				if(json != null) {
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
								System.out.println("Client "+expediteur+" : "+json.getString("msg"));
							}
							if (type.equals("hello")&&Client.socketOut.getPort()==json.getInt("oldPort")){
								try {
									Client.socketOut = new Socket("127.0.0.1",json.getInt("newPort"));
									Client.out = new BufferedWriter(new OutputStreamWriter(Client.socketOut.getOutputStream(),"UTF-8"));
									System.out.println("maintenant je parle à "+Client.socketOut.getPort());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							else//faire suivre
								Client.envoyer(json);
							if(type.equals("hello")&&Client.server.getLocalPort()==json.getInt("oldPort")){
								Client.precedent=json.getInt("newPort");
							}
						}
						else if (type.equals("msg")){
							System.out.println("Vous : "+json.getString("msg"));
						}
					}
					catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
}
