import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;

public class ClientSide extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5261903818373181455L;
	private JTextArea taEditor = new JTextArea("Type your message");
	private JTextArea taDisplay = new JTextArea();
	private JList liUsers = new JList();
	private PrintWriter writer;
	private BufferedReader reader;
	private JScrollPane scrollTaVisor = new JScrollPane(taDisplay);
	private JButton jButtonAll = new JButton("All");
	String userName;

	public ClientSide() {
		
		setTitle("Chat w/ sockets - djv0012");
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		liUsers.setBackground(Color.GRAY);
		taEditor.setBackground(Color.CYAN);

		taEditor.setPreferredSize(new Dimension(400, 40));
		taDisplay.setEditable(false);
		liUsers.setPreferredSize(new Dimension(100, 140));
		
		add(taEditor, BorderLayout.SOUTH);
		add(scrollTaVisor , BorderLayout.CENTER);
		add(new JScrollPane(liUsers), BorderLayout.WEST);
		add(jButtonAll, BorderLayout.EAST);
		
		pack();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		String[] users = new String[]{"all", "daniel", "john", "paul", "holly", "mariah", "matheus", "robert"};
		fillUsersList(users);
		
	}	
	
	/**
	 * Fill Users List
	 * @param users
	 */
	private void fillUsersList(String[] users) {
		DefaultListModel modelo = new DefaultListModel();
		liUsers.setModel(modelo);
		for(String user: users){
			modelo.addElement(user);			
		}		
		
	}

	private void startWriter() {
		taEditor.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
//				taEditor.getText()
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					// writing to the server
					if(taDisplay.getText().isEmpty()){
						return;
					}
					
					Object user = liUsers.getSelectedValue();
					if(user != null){  // Here I have to put All for broadcast msg
						// sending on the display
						taDisplay.append("Me: ");
						taDisplay.append(taEditor.getText());
						taDisplay.append("\n");						
						
						writer.println(Commands.MESSAGE + user);
						writer.println(taEditor.getText());
						
						// Creating a log file 
						PrintWriter out;
						try {
							DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
							String formattedDate = formatter.format(LocalDate.now());
														
							out = new PrintWriter("E:\\University\\2019_2020\\CS6002 Distributed and Internet Systems\\CourseWork\\CW1\\" + userName + "_" + formattedDate + ".txt");
							out.println(taDisplay.getText());
							//System.out.println(taVisor.getText());
							out.close();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}						
						
						
						// cleaning up the editor
						taEditor.setText("");
						e.consume();
					}else{
						if(taDisplay.getText().equalsIgnoreCase(Commands.EXIT)){
							System.exit(0);
						}
						JOptionPane.showMessageDialog(ClientSide.this, "Select a user");
						return ;
					}
				}				
			}

			@Override
			public void keyReleased(KeyEvent e) {}
		});
		
		jButtonAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send msg to all
				
				taDisplay.append("Me: ");
				taDisplay.append(taEditor.getText());
				taDisplay.append("\n");

				ListModel model = liUsers.getModel();

				for(int i=0; i < model.getSize(); i++){
				     Object user =  model.getElementAt(i);				     						
						
				     writer.println(Commands.MESSAGE + user);
						writer.println(taEditor.getText());
					
					// Creating a log file 
						PrintWriter out;
						try {
							DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
							String formattedDate = formatter.format(LocalDate.now());
														
							out = new PrintWriter("E:\\University\\2019_2020\\CS6002 Distributed and Internet Systems\\CourseWork\\CW1\\" + userName + "_" + formattedDate + ".txt");
							out.println(taDisplay.getText());
							//System.out.println(taVisor.getText());
							out.close();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}											
				}					
				// cleaning up the editor
				taEditor.setText("");
			}			
			
		});
		
	}
	
	public void startChat() {
		try {
			final Socket client = new Socket("127.0.0.1", 9999);
			writer = new PrintWriter(client.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(client.getInputStream()));			
		} catch (UnknownHostException e) {
			System.out.println("Passed address is invalid");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("The server may be down");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ClientSide client = new ClientSide();
		client.startChat();
		client.startWriter();
		client.startReader();
	}
	
	private void updateUsersList() {
		writer.println(Commands.LIST_USERS);
	}

	private void startReader() {
		// reading server message
		try {
			while(true){
				String message = reader.readLine();
								
				if(message == null || message.isEmpty())
					continue;
				
				// receive the text
				if(message.equals(Commands.LIST_USERS)){
					String[] users = reader.readLine().split(",");
					fillUsersList(users);
				}else if(message.equals(Commands.LOGIN)){
					String login = JOptionPane.showInputDialog("What is your login?");
					writer.println(login);
					setTitle(login);  // put the username in the top bar of the window
					userName = login;
				}else if(message.equals(Commands.LOGIN_DENIED)){
					JOptionPane.showMessageDialog(ClientSide.this, "login is invalid");
					// System.exit(0);
				}else if(message.equals(Commands.LOGIN_ACCEPTED)){
					updateUsersList();	
				}else{
					taDisplay.append(message);
					taDisplay.append("\n");
					taDisplay.setCaretPosition(taDisplay.getDocument().getLength());
				}
				
			}     
			
		} catch (IOException e) {
			System.out.println("unable to read server message");
			e.printStackTrace();
		}		
	}


	private DefaultListModel getListaUsuarios() {
		return (DefaultListModel) liUsers.getModel();
	}
}
