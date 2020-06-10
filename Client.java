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
import java.net.UnknownHostException;
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
public class Client {
    
    private JFrame window;
    private JTextField ipAddress, inputPort, userName;
    private JButton connect;
    private JTextArea textBox;
    private Boolean keepGoing;
    private DatagramSocket socket;
    private int port;
    private listenServer tls;
    private String user = "";
    
    public static void main(String[] args) {
        Client start = new Client();
        start.init();
    }
    
    public class listenServer extends Thread {
        private InetAddress ipAddressServer;
        private int portAddress;
        
        public listenServer( InetAddress ipAddressServer, int portAddress ){
            this.ipAddressServer = ipAddressServer;
            this.portAddress = portAddress;
        }
        
        public void run() {
            keepGoing = true;
            try {
                socket = new DatagramSocket( port );
                String username = userName.getText().toString();
                sendRequest( username );
                
                if( user.equals("") )
                    socket.setSoTimeout(5000);
                
                while (keepGoing) {
                    byte[] bufferRespond = new byte[512];
                    DatagramPacket respond = new DatagramPacket(bufferRespond, bufferRespond.length);
                    socket.receive( respond );
                    String respondText = new String(bufferRespond, 0, respond.getLength());
                    if( respondText.split("~")[0].equals("Sukses terhubung pada server.")){
                        textBox.setText( respondText.split("~")[0] + 
                                "\n--------------------------------------------------------------------------------------------------------------\n");
                        user = respondText.split("~")[1];
                        socket.setSoTimeout(0);
                    }else{
                        textBox.setText( textBox.getText().toString() + "Broadcast from server : \n\n" + respondText + 
                                "\n--------------------------------------------------------------------------------------------------------------\n");
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Tidak dapat terhubung ke server, pastikan IP Address dan Port sudah benar.");
                socket.close();
                inputPort.setEditable( true );
                ipAddress.setEditable( true );
                userName.setEditable( true );
                connect.setEnabled( true );
            } 
        }
        
        public void sendRequest( String text ){
            try {
                DatagramPacket request;
                byte[] buffer;
                if( text.equals("bongko") ){
                    text = "bongko~" + user;
                }
                buffer = text.getBytes();
                request = new DatagramPacket(buffer, buffer.length, ipAddressServer, portAddress);
                socket.send( request ); 
                
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void init(){
        window = new JFrame("Client Broadcast");
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        
        JLabel ipText = new JLabel("IP Address");
        ipText.setBounds( 10, 10, 75, 20 );
        
        ipAddress = new JTextField();
        ipAddress.setBounds( 80, 11, 110, 20 );
        ipAddress.setTransferHandler( null );
        ipAddress.setBorder(BorderFactory.createCompoundBorder(border, 
            BorderFactory.createEmptyBorder( 0, 5, 0, 5 )));
        
        JLabel portText = new JLabel("Insert Port Number");
        portText.setBounds( 205, 10, 120, 20 );
        
        inputPort = new JTextField();
        inputPort.setBounds( 322, 11, 50, 20 );
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
        
        connect = new JButton("Connect");
        connect.setBounds( 390, 10, 93, 50 );
        connect.addMouseListener( new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed( java.awt.event.MouseEvent evt ) {
                if( ipAddress.getText().toString().equals("") ){
                    JOptionPane.showMessageDialog(null, "IP Address harus diisi.");
                    ipAddress.requestFocus();
                }else if( inputPort.getText().toString().equals("") ){
                    JOptionPane.showMessageDialog(null, "Port harus diisi.");
                    inputPort.requestFocus();
                }else if( userName.getText().toString().equals("") ){
                    JOptionPane.showMessageDialog(null, "Username harus diisi.");
                    userName.requestFocus();
                }else{
                    inputPort.setEditable( false );
                    ipAddress.setEditable( false );
                    userName.setEditable( false );
                    connect.setEnabled( false );
                    try {
                        InetAddress hostAddress = InetAddress.getByName( ipAddress.getText().toString() );
                        tls = new listenServer( hostAddress, Integer.parseInt( inputPort.getText().toString() ) );
                        tls.start();
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        JLabel usernameText = new JLabel("Username");
        usernameText.setBounds( 10, 40, 75, 20 );
        
        userName = new JTextField();
        userName.setBounds( 80, 40, 292, 20 );
        userName.setTransferHandler( null );
        userName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if ( ((c < '0') || (c > '9')) && ((c < 'a') || (c > 'z')) && ((c < 'A') || (c > 'Z')) && (c != KeyEvent.VK_SPACE) || userName.getText().length() > 20 ) {
                   e.consume();
                }
             }
        });
        userName.setBorder(BorderFactory.createCompoundBorder(border, 
            BorderFactory.createEmptyBorder( 0, 5, 0, 5 )));
        
        textBox = new JTextArea();
        textBox.setEditable( false );
        textBox.setLineWrap(true);
        textBox.setWrapStyleWord(true);
        textBox.setBorder(BorderFactory.createCompoundBorder(border, 
            BorderFactory.createEmptyBorder( 5, 5, 5, 5 )));
        
        JScrollPane skroll = new JScrollPane( textBox );
        skroll.setBounds( 10, 70, 473, 490 );
        
        window.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                if( !user.equals("") ){
                    tls.sendRequest("bongko");
                }
                System.exit(0);
            }
        });
        
        window.setSize( 500, 600 );
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setLayout(null);
        window.add( ipText );
        window.add( ipAddress );
        window.add( portText );
        window.add( inputPort );
        window.add( connect );
        window.add( usernameText );
        window.add( userName );
        window.add( skroll );
       
        window.setVisible( true );
    }
}
