import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Client extends JFrame  implements ActionListener {

	private static final long serialVersionUID = 1L;
	public static int myId = 1;
	public static BufferedWriter out;
	public static BufferedReader in;
	public static Socket socketOut = null;
	public static ServerSocket server = null;
	public static Socket socketIn = null;
	public static int precedent;
	public static JTextField entree;
	public static JTextPane tchat;
	private static JSONArray listDest;
	private static JPanel lePanel;
	private static JButton salon1, salon2, salon3, salon4;
	public static String salon = "salon 1";
	public static String pseudo = "Anonyme";
	private static JTextField pseudoField;
	
	public Client() {
		super("Tchat");
		setMinimumSize(new Dimension(500, 500));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		lePanel = new JPanel(new BorderLayout());
		lePanel.setLayout(new BoxLayout(lePanel, BoxLayout.PAGE_AXIS));
		setContentPane(lePanel);
		pseudoField = new JTextField();
		pseudoField.setMaximumSize(new Dimension(500,150));
		pseudoField.addActionListener(this);
		lePanel.add(pseudoField, BorderLayout.PAGE_START);
		JPanel salons = new JPanel(new GridLayout(1,4));
		salons.setMaximumSize(new Dimension(500,100));
		lePanel.add(salons);
		salon1 = new JButton("Salon 1");
		salon1.addActionListener(this);
		salons.add(salon1);
		salon2 = new JButton("Salon 2");
		salon2.addActionListener(this);
		salons.add(salon2);
		salon3 = new JButton("Salon 3");
		salon3.addActionListener(this);
		salons.add(salon3);
		salon4 = new JButton("Salon 4");
		salon4.addActionListener(this);
		salon1.setBackground(Color.blue);
		salon2.setBackground(Color.white);
		salon3.setBackground(Color.white);
		salon4.setBackground(Color.white);
		salons.add(salon4);
		tchat = new JTextPane();
		tchat.setBackground(new Color(160,200,250));
		tchat.setText("vous avez rejoint le salon 1.\n");
		tchat.setEditable(false);
		JScrollPane jsp = new JScrollPane(tchat);
		jsp.setMinimumSize(new Dimension(500,200));
		lePanel.add(jsp);
		entree = new JTextField();
		entree.setMaximumSize(new Dimension(500,150));
		entree.addActionListener(this);
		lePanel.add(entree, BorderLayout.PAGE_END);
		setVisible(true);
	}
	
	public static void main(String[] args) throws InterruptedException {

		new Client();
		
		//creation de son ecoute dans la boucle
		try {
			server = new ServerSocket(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//connexion à l'annuaire
		try {
			socketOut = new Socket("127.0.0.1", 12000);
			out = new BufferedWriter(new OutputStreamWriter(socketOut.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//envoi du port sur ecoute
		try {
			envoyer(new JSONObject().put("port", server.getLocalPort()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		//récupération de mon id et du port du 1er client
		try {
			in = new BufferedReader(new InputStreamReader(socketOut.getInputStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			JSONObject json = lire();
			myId = json.getInt("id");
			pseudo+=myId;
			listDest = (JSONArray) json.get("listDest");
			socketOut.close();
			if(listDest.length()>1)
				try{
					socketOut = new Socket("127.0.0.1", listDest.getInt(1));
				} catch (IOException e1) {
					try{
						socketOut = new Socket("127.0.0.1", listDest.getInt(2));
					} catch (IOException e2) {
						socketOut = new Socket("127.0.0.1", listDest.getInt(3));
					}
				}
			else
				socketOut = new Socket("127.0.0.1", listDest.getInt(0));
			out = new BufferedWriter(new OutputStreamWriter(socketOut.getOutputStream(),"UTF-8"));
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		System.out.println("je suis le Client "+myId+", j'ecoute port "+server.getLocalPort());

		//insertion dans la boucle : envoi de son port sur ecoute
		try {
			envoyer(new JSONObject().put("type", "hello").put("oldPort",socketOut.getPort()).put("newPort",server.getLocalPort()).put("id", myId));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		MessageListener msgL = new MessageListener();
		Thread t = new Thread(msgL);
		t.start();

		Messagerie chat = new Messagerie();
		Thread t2 = new Thread(chat);
		t2.start();

		//boucle attente de nouveau client
		while(!Thread.interrupted()){
			//écoute
			try {
				socketIn = server.accept();
				in=new BufferedReader(new InputStreamReader(socketIn.getInputStream(),"UTF-8"));
				t.interrupt();
				t = new Thread(msgL);
				t.start();
				System.out.println("un client s'est connecté");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

	}

	static void envoyer(JSONObject json) {
		System.out.println("envoi "+json+" au port "+socketOut.getPort());
		try {
			out.write(json+"\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	


	static JSONObject lire() {
		JSONObject json = null;
		String maLigne = null;
		try {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
			}
			maLigne = in.readLine();
			if(maLigne!=null)
				System.out.println("reception : "+maLigne);
			try {
				if(maLigne!=null)
					json = new JSONObject(maLigne);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			//insertion dans la boucle : envoi de son port sur ecoute
			if(precedent!=socketOut.getPort())
				try {
					envoyer(new JSONObject().put("type", "hello").put("oldPort",precedent).put("newPort",server.getLocalPort()).put("id", myId));
				} 
			catch (JSONException e1) {
				e1.printStackTrace();
			}
			else{
				try {
					socketOut.close();
					socketOut = new Socket("127.0.0.1",precedent);
					out = new BufferedWriter(new OutputStreamWriter(socketOut.getOutputStream(),"UTF-8"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return json;
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		if(action.getSource()==entree){
		try {
			envoyer( new JSONObject().put("type", "msg").put("msg", entree.getText()).put("id", Client.myId).put("salon", salon).put("pseudo", pseudo));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		entree.setText("");
		}
		if(action.getSource()==salon1){
			salon1.setBackground(Color.blue);
			salon2.setBackground(Color.white);
			salon3.setBackground(Color.white);
			salon4.setBackground(Color.white);
			salon="salon 1";
			tchat.setText("vous rejoignez le salon 1\n");
			try {
				envoyer( new JSONObject().put("type", "salon").put("pseudo", pseudo).put("id", Client.myId).put("salon", salon));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(action.getSource()==salon2){
			salon1.setBackground(Color.white);
			salon2.setBackground(Color.blue);
			salon3.setBackground(Color.white);
			salon4.setBackground(Color.white);
			salon="salon 2";
			tchat.setText("vous rejoignez le salon 2\n");
			try {
				envoyer( new JSONObject().put("type", "salon").put("pseudo", pseudo).put("id", Client.myId).put("salon", salon));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(action.getSource()==salon3){
			salon1.setBackground(Color.white);
			salon2.setBackground(Color.white);
			salon3.setBackground(Color.blue);
			salon4.setBackground(Color.white);
			salon="salon 3";
			tchat.setText("vous rejoignez le salon 3\n");
			try {
				envoyer( new JSONObject().put("type", "salon").put("pseudo", pseudo).put("id", Client.myId).put("salon", salon));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(action.getSource()==salon4){
			salon1.setBackground(Color.white);
			salon2.setBackground(Color.white);
			salon3.setBackground(Color.white);
			salon4.setBackground(Color.blue);
			salon="salon 4";
			tchat.setText("vous rejoignez le salon 4\n");
			try {
				envoyer( new JSONObject().put("type", "salon").put("pseudo", pseudo).put("id", Client.myId).put("salon", salon));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(action.getSource()==pseudoField){
			pseudo=pseudoField.getText();
		}
	}

}
