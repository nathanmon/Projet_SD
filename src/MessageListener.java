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
							if(type.equals("salon")&&json.getString("salon").equals(Client.salon)){
								Client.tchat.setText(json.getString("pseudo")+" a rejoint le salon.\n"+Client.tchat.getText());
								json.put("roomList", json.getJSONArray("roomList").put(Client.pseudo));
							}
							if(type.equals("msg")){//réception d'un msg
								System.out.println(json.getString("pseudo")+" : "+json.getString("msg"));
								if(json.getString("salon").equals(Client.salon)){
									Client.tchat.setText(json.getString("pseudo")+" : "+json.getString("msg")+"\n"+Client.tchat.getText());
								}
							}
							if (type.equals("ring")&&Client.socketOut.getPort()==json.getInt("oldPort")){
								try {
									Client.socketOut.close();
									Client.socketOut = new Socket("127.0.0.1",json.getInt("newPort"));
									Client.out = new BufferedWriter(new OutputStreamWriter(Client.socketOut.getOutputStream(),"UTF-8"));
									System.out.println("maintenant je parle à "+Client.socketOut.getPort());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if(type.equals("ring")&&Client.server.getLocalPort()==json.getInt("myNext")){
								Client.precedent=json.getInt("myPort");
								System.out.println(Client.precedent+" me parle");
							}
							Client.envoyer(json);
						}
						else{
							if (type.equals("msg")){
								System.out.println("Vous : "+json.getString("msg"));
								Client.tchat.setText("Vous : "+json.getString("msg")+"\n"+Client.tchat.getText());
							}
							if (type.equals("salon")){
								Client.tchat.setText("Vous avez rejoint "+json.getJSONArray("roomList")+" au "+json.getString("salon")+".\n"+Client.tchat.getText());
							}
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
