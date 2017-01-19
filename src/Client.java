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
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	public static String salon = "salon 1";
	public static JComboBox<String> boxSalon;
	public static JButton newSalon;
	public static String pseudo = "Anonyme";
	private static JTextField pseudoField;
	public static JSONArray listSalon =  new JSONArray().put("Salon 1");

	public Client() {
		super("Tchat");
		setMinimumSize(new Dimension(500, 500));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
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
		try {
			boxSalon = new JComboBox(JSONArrayToStringTab(listSalon));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boxSalon.addActionListener(this);
		salons.add(boxSalon);
		newSalon = new JButton("Creer nouveau salon");	
		newSalon.addActionListener(this);
		salons.add(newSalon);
		tchat = new JTextPane();
		tchat.setBackground(new Color(160,200,250));
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
	
	public String[] JSONArrayToStringTab(JSONArray jsonA) throws JSONException{
		String[] newTab = new String[jsonA.length()];
		for(int i=0; i<jsonA.length(); i++){
			newTab[i] = jsonA.getString(i);
		}
		return newTab;
	}

	public static void main(String[] args) throws InterruptedException {

		new Client();

		//creation du port sur ecoute
		try {
			server = new ServerSocket(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//contacter l'annuaire afin de récupérer la liste JSONArray listDest des clients les plus récents
		FindMainChord();

		//entrer dans l'anneau
		JoinMainChord();
		
		//thread qui attend l'arrivee de messages
		MessageListener msgL = new MessageListener();
		Thread t = new Thread(msgL);
		t.start();

		//thread qui attend les commandes par le terminal
		Messagerie chat = new Messagerie();
		Thread t2 = new Thread(chat);
		t2.start();

		//boucle attente connexion de nouveaux clientx
		while(!Thread.interrupted()){
			try {
				socketIn = server.accept();
				in=new BufferedReader(new InputStreamReader(socketIn.getInputStream(),"UTF-8"));
				t.interrupt();
				t = new Thread(msgL);
				t.start();
				System.out.println("connexion");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

	}

	private static void JoinMainChord() {
		try{
			socketOut.close();
			if(listDest.length()>1){
				try{
					socketOut = new Socket("127.0.0.1", listDest.getInt(1));
				} catch (IOException e1) {
					if(listDest.length()>2){
						try{
							socketOut = new Socket("127.0.0.1", listDest.getInt(2));
						} catch (IOException e2) {
							if(listDest.length()>3){
								try{
								socketOut = new Socket("127.0.0.1", listDest.getInt(3));
								} catch (IOException e3) {
									socketOut = new Socket("127.0.0.1", listDest.getInt(0));
								}
							}
							else {
								socketOut = new Socket("127.0.0.1", listDest.getInt(0));
							}
						}
					}
					else {
						socketOut = new Socket("127.0.0.1", listDest.getInt(0));
					}
				}
			}
			else {
				socketOut = new Socket("127.0.0.1", listDest.getInt(0));
			}
			listDest = new JSONArray().put(server.getLocalPort());
			out = new BufferedWriter(new OutputStreamWriter(socketOut.getOutputStream(),"UTF-8"));
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		System.out.println("je suis le Client "+myId+", j'ecoute port "+server.getLocalPort());
		if(server.getLocalPort()==socketOut.getPort())
			precedent=socketOut.getPort();
		
		//envoi de son port sur ecoute
		try {
			envoyer(new JSONObject().put("type", "ring").put("oldPort",socketOut.getPort()).put("newPort",server.getLocalPort()).put("id", myId).put("myPort", server.getLocalPort()).put("myNext", socketOut.getPort()));
			JoinChatRoom(salon);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

	}

	private static void FindMainChord() {
		//connexion à l'annuaire
		try {
			socketOut = new Socket("127.0.0.1", 12000);
			out = new BufferedWriter(new OutputStreamWriter(socketOut.getOutputStream(),"UTF-8"));
		} catch (IOException e) {
			tchat.setText("Connexion impossible.");
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
			JSONObject json = lire();
			myId = json.getInt("id");
			pseudo+=myId;
			pseudoField.setText(pseudo);
			listDest = (JSONArray) json.get("listDest");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void envoyer(JSONObject json) {
		System.out.println("envoi "+json+" au port "+socketOut.getPort());
		try {
			out.write(json+"\n");
			out.flush();
		} catch (IOException e) {
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
			//il a pas dit enrevoir : réinsertion dans la boucle : envoi de son port sur ecoute
			if(precedent!=socketOut.getPort()){//si il y avait au moins 3 clients
				try {
					if(precedent!=0){
						envoyer(new JSONObject().put("type", "ring").put("oldPort",precedent).put("newPort",server.getLocalPort()).put("id", myId).put("myPort", server.getLocalPort()).put("myNext", socketOut.getPort()));
						try {
							Thread.sleep(10);
						} catch (InterruptedException e1) {
						}
					}
				} 
				catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
			else{//si il y avait 2 clients
				try {
					precedent=server.getLocalPort();
					System.out.println("connexion à moi meme port "+precedent);
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

	private static void JoinChatRoom(String room){
		try {
			envoyer( new JSONObject().put("type", "salon").put("pseudo", pseudo).put("id", Client.myId).put("salon", salon).put("roomList", new JSONArray()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
		if(action.getSource()==boxSalon){
			salon = boxSalon.getSelectedItem().toString();
			JoinChatRoom(salon);
			
			}
//			salon="salon 1";
//			salon1.setBackground(Color.blue);
//			salon2.setBackground(Color.white);
//			salon3.setBackground(Color.white);
//			salon4.setBackground(Color.white);
//			tchat.setText("");
//			JoinChatRoom(salon);
		
//		if(action.getSource()==salon2){
//			salon1.setBackground(Color.white);
//			salon2.setBackground(Color.blue);
//			salon3.setBackground(Color.white);
//			salon4.setBackground(Color.white);
//			salon="salon 2";
//			tchat.setText("");
//			JoinChatRoom(salon);
//		}
//		if(action.getSource()==salon3){
//			salon1.setBackground(Color.white);
//			salon2.setBackground(Color.white);
//			salon3.setBackground(Color.blue);
//			salon4.setBackground(Color.white);
//			salon="salon 3";
//			tchat.setText("");
//			JoinChatRoom(salon);
//		}
//		if(action.getSource()==salon4){
//			salon1.setBackground(Color.white);
//			salon2.setBackground(Color.white);
//			salon3.setBackground(Color.white);
//			salon4.setBackground(Color.blue);
//			salon="salon 4";
//			tchat.setText("");
//			JoinChatRoom(salon);
//		}
		if(action.getSource()==pseudoField){
			pseudo=pseudoField.getText();
		}
		
		if(action.getSource()==newSalon){
			ajouterSalon();
		}
	}
	
	public void ajouterSalon(){
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(300, 300));
		frame.setTitle("Ajout d'un nouveau salon");
		JPanel panel = new JPanel(new GridLayout(3,1));
		JLabel label = new JLabel("Entrez nom du salon");
		JTextField zoneName = new JTextField();
		JButton valider = new JButton("ok");
		panel.add(label, BorderLayout.NORTH);
		panel.add(zoneName, BorderLayout.SOUTH);
		panel.add(valider);
		frame.add(panel);
		frame.setVisible(true);
		JSONObject newSalon;
		valider.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boxSalon.addItem(zoneName.getText());
				listSalon.put(zoneName.getText());
				boxSalon.revalidate();
				boxSalon.repaint();
				boxSalon.setVisible(false);
				boxSalon.setVisible(true);
				try {
					envoyer(new JSONObject().put("type", "newSalon").put("nomSalon", zoneName.getText()).put("id", myId));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});

	}
	

}
