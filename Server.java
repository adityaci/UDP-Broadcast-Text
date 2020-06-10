package udpbroadcast;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 *
 * @author Aditya.C.I
 */
public class Server {
    
    private DatagramSocket socket;
    private int port;
    private Boolean keepGoing;
    private startServer thread;
    private JTextArea textBox;
    private JFrame window;
    private JTextField inputPort, inputText;
    private JButton send, start;
    private ArrayList<Server.clientThread> clients;
    
    public static void main(String[] args) {
        Server start = new Server();
        start.init();
    }
    
    public Server(){
        clients = new ArrayList<>();
    }
    
    public class startServer extends Thread {
        
        public void run() {
            keepGoing = true;
            try {
                socket = new DatagramSocket( port );
                System.out.print("Broadcast Server Running pada port " + port + ".");
                    textBox.setText("Broadcast Server Running pada port " + port + ".\n" +
                            "Gunakan --list untuk melihat list client\n" +
                            "--------------------------------------------------------------------------------------------------------------");
                while (keepGoing) {
                    byte[] buffer = new byte[20];
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    
                    socket.receive( request );
                    String requestValue = new String(buffer, 0, request.getLength());
                    if( !requestValue.split("~")[0].equals("bongko") ){
                        
                        InetAddress clientAddress = request.getAddress();
                        int clientPort = request.getPort();
                        
                        int tmp = 0;
                        for( int i = 0; i < clients.size(); i++ ){
                            if( clients.get(i).getUsername().equals( requestValue )){
                                ++tmp;
                                requestValue = requestValue + tmp;
                            }
                        }
                        
                        clientThread ct = new clientThread( requestValue, clientAddress, clientPort );
                        clients.add( ct );

                        String respondText = "Sukses terhubung pada server.~" + requestValue;
                        ct.sendText( respondText );

                        System.out.println( "\n" + requestValue + " telah terhubung.");
                    }else{
                        for( int i = 0; i < clients.size(); i++ ){
                            if( requestValue.split("~")[1].equals( clients.get(i).getUsername() ) ){
                                clients.remove(i);
                            }
                        }
                    }
                    
                }
            } catch (IOException e) {
                String msg = "\nBroadcast Server Disconnected.\n";
                System.out.println(msg);
                textBox.setText("Broadcast Server Disconnected.\n" +
                            "--------------------------------------------------------------------------------------------------------------");
            }
        }
        
        public void stopListen() {
            keepGoing = false;
            socket.close();
        }
    }
    
    public class clientThread extends Thread {

        private final String namaUser;
        private final InetAddress clientAddress;
        private final int clientPort;
        
        public clientThread( String namaUser, InetAddress clientAddress, int clientPort ){

            this.namaUser = namaUser;
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;
        }
        
        public void sendText( String respondText ){
            try {
                byte[] bufferRespond = respondText.getBytes();
                DatagramPacket respond = new DatagramPacket(bufferRespond, bufferRespond.length, clientAddress, clientPort );
                socket.send(respond);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public String getUsername(){
            return namaUser;
        }
    }
    
    public void init(){
        window = new JFrame("Server Broadcast");
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        
        JLabel ipText = new JLabel("IP Address : 127.0.0.1");
        ipText.setBounds( 10, 10, 150, 20 );
        
        JLabel portText = new JLabel("Insert Port Number : ");
        portText.setBounds( 155, 10, 120, 20 );
        
        inputPort = new JTextField();
        inputPort.setBounds( 275, 11, 50, 20 );
        inputPort.setTransferHandler( null );
        inputPort.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if ( c < '0' || c > '9' || inputPort.getText().length() > 4 ) {
                   e.consume();
                }
             }
        });
        inputPort.setBorder(BorderFactory.createCompoundBorder(border, 
            BorderFactory.createEmptyBorder( 0, 5, 0, 5 )));
        
        start = new JButton("Start");
        start.setBounds( 345, 11, 135, 20 );
        start.addMouseListener( new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed( java.awt.event.MouseEvent evt ) {
                if( inputPort.getText().equals("") ){
                    JOptionPane.showMessageDialog(null, "Port Harus Diisi.");
                    inputPort.requestFocus();
                }else if( Integer.parseInt( inputPort.getText() ) > 65535 ){
                    JOptionPane.showMessageDialog(null, "Range port 1 - 65535.");
                    inputPort.requestFocus();
                }else{
                    port = Integer.parseInt( inputPort.getText() );
                    start.setEnabled( false );
                    inputPort.setEditable( false );
                    inputText.setEditable( true );
                    send.setEnabled( true );
                    thread = new startServer();
                    thread.start(); 
                }
            }
        });
        
        textBox = new JTextArea();
        textBox.setEditable( false );
        textBox.setLineWrap(true);
        textBox.setWrapStyleWord(true);
        textBox.setBorder(BorderFactory.createCompoundBorder(border, 
            BorderFactory.createEmptyBorder( 5, 5, 5, 5 )));
        
        JScrollPane skroll = new JScrollPane( textBox );
        skroll.setBounds( 10, 40, 473, 480 );
        
        inputText = new JTextField();
        inputText.setEditable( false );
        inputText.setBounds( 10, 530, 400, 30 );
        inputText.setBorder(BorderFactory.createCompoundBorder(border, 
            BorderFactory.createEmptyBorder( 0, 5, 0, 5 )));
        
        send = new JButton("Send");
        send.setEnabled( false );
        send.setBounds( 415, 530, 67, 30 );
        send.addMouseListener( new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed( java.awt.event.MouseEvent evt ) {
                String inputTextValue = inputText.getText().toString();
                inputText.setText("");
                if( inputTextValue.equals("") ){
                    JOptionPane.showMessageDialog(null, "Isi Pesan Harus Diisi.");
                    inputText.requestFocus();
                }else if( inputTextValue.equals("--list") ){
                    textBox.setText( textBox.getText().toString() + "\nList user yang terhubung pada server : \n" );
                    for( int i = 0; i < clients.size(); i++ ){
                        textBox.setText( textBox.getText().toString() + (i+1) + ". " + clients.get(i).getUsername() + "\n" );
                    }
                    textBox.setText( textBox.getText().toString() + 
                            "--------------------------------------------------------------------------------------------------------------");
                }else{
                    textBox.setText( textBox.getText().toString() + "\nBroadcast ke semua klien : \n\n" + inputTextValue + "\n" +
                            "--------------------------------------------------------------------------------------------------------------");
                    for( int i = 0; i < clients.size(); i++ ){
                        clients.get(i).sendText( inputTextValue );
                    }
                }
            }
        });
        
        window.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                for( int i = 0; i < clients.size(); i++ ){
                    clients.get(i).sendText( "Server Disconnected." );
                }
                System.exit(0);
            }
        });
        
        window.setSize( 500, 600 );
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setLayout(null);
        window.add( ipText );
        window.add( portText );
        window.add( inputPort );
        window.add( start );
        window.add( skroll );
        window.add( inputText );
        window.add( send );
        window.setVisible( true );
    }
}