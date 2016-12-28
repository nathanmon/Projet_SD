import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Annuaire {

	public static void main(String Args[]) {

		ServerSocket server = null;
		Socket socket = null;
		int port = 0;

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
				BufferedWriter out = new BufferedWriter(output);
				InputStreamReader input = new InputStreamReader(socket.getInputStream());
				BufferedReader in = new BufferedReader(input);
				out.write(port+"\n");
				out.flush();
				if(port==0){
					port = Integer.parseInt(in.readLine());
					System.out.println("1er client ecoute port "+port);
				}
				else
					System.out.println("nouveau client");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
