import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Messagerie  implements Runnable {

	public BufferedWriter out;
	public int portIn;
	
	public Messagerie(BufferedWriter out, int portIn) {
		this.out=out;
		this.portIn = portIn;
	}

	@Override
	public void run() {
		String str;
		InputStream inputS = System.in;
		InputStreamReader inputSR = new InputStreamReader(inputS);
		BufferedReader in = new BufferedReader(inputSR);
		
		System.out.println("messagerie :");
		
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
		if(str!=null){
			str=portIn+": "+str;
			System.out.println(str);

			try {
				out.write(str+"\n");
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
