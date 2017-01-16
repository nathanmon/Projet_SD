import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONException;
import org.json.JSONObject;

public class Messagerie implements Runnable {
	
	public Messagerie() {
	}

	@Override
	public void run() {
		String str;
		InputStream inputS = System.in;
		InputStreamReader inputSR = null;
		inputSR = new InputStreamReader(inputS);
		BufferedReader in = new BufferedReader(inputSR);

		while(!Thread.interrupted()){
			str=null;
			try {
				str = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(str!=null)
				ecrire(str);
		}
	}

	private void ecrire(String str){
		JSONObject json = null;
		try {
			json = new JSONObject().put("type", "msg").put("msg", str).put("id", Client.myId);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		Client.envoyer(json);
	}
}
