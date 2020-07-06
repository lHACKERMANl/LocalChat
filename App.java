import java.awt.BorderLayout;
import java.beans.Customizer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.applet.Applet;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.*;

public class App extends JFrame
{ 
    private FileWriter fw;
    private String dialog = "";
    private File file;

    private JTextArea taMain;
    private JTextField tfMsg;
    private JTextField ipFor;
    private JFileChooser fileopen;

    private final String FRM_TITLE = "Chat";
    private final int FRM_LOC_X = 100;
    private final int FRM_LOC_Y = 100;
    private final int FRM_WIDTH = 400;
    private final int FRM_HEIGHT = 400;

    private final int PORT = 9876;
    private  String Name;
    private  String IP_BROADCUST = "192.168.0.12";

    private class theReceiver extends Thread{
    @Override
    public void start(){
        super.start();
        try{
            customize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }  
    }

    private void customize() throws Exception{
        DatagramSocket receiveSocket = new DatagramSocket(PORT);
        Pattern regex = Pattern.compile("[\u0020-\uFFFF]");
        while(true){
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            receiveSocket.receive(receivePacket);
            InetAddress iPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String sentence = new String (receivePacket.getData());
            Matcher m = regex.matcher(sentence);
            //System.out.println(sentence.trim());
            dialog += iPAddress.toString() + ": " + sentence.trim() + "\r\n";
            System.out.println(dialog);
            GetDialog();
            taMain.append(iPAddress.toString() + ": ");
            while(m.find()){
                taMain.append(sentence.substring(m.start(), m.end()));
            }
            taMain.append("\r\n");
        }
    }
}

private void btnSend_Handler() throws Exception{
    DatagramSocket sendSocket = new DatagramSocket();
    InetAddress IPAddress = InetAddress.getByName(IP_BROADCUST);
    byte[] sendData;
    String sentence = tfMsg.getText();
    dialog += "Me: " + sentence + "\r\n";
    taMain.append("Me: " + sentence + "\r\n");
    System.out.println(dialog);
    GetDialog();
    tfMsg.setText("");
    sendData = sentence.getBytes("UTF-8");
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress,PORT);
    sendSocket.send(sendPacket);
}

private void btnSendFile_Handler() throws Exception{
    File fileSend;
    JFileChooser fileopen = new JFileChooser();             
    int ret = fileopen.showDialog(null, "Open file");  

    if (ret == JFileChooser.APPROVE_OPTION) {
        fileSend = fileopen.getSelectedFile();
        if(fileSend.length()/1000 <= 5000/1000){
            DatagramSocket sendSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(IP_BROADCUST);
            byte[] sendData;
            sendData = Files.readAllBytes(fileSend.toPath());
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress,PORT);
            taMain.append("You sendet " + fileSend.getName() + "\r\n");
            GetDialog();
            sendSocket.send(sendPacket);
        }
        else{
            taMain.append("Sorry. This file more than 5KB\r\n");
        }
    }
}

private void framDraw(JFrame frame){
tfMsg = new JTextField();
taMain = new JTextArea(FRM_HEIGHT/19, 50);
//ipFor = new JTextField();
JScrollPane spMain = new JScrollPane(taMain);
spMain.setLocation(0,0);
taMain.setLineWrap(true);
taMain.setEditable(false);

JButton btnSend = new JButton();
btnSend.setText("Send");
btnSend.setToolTipText("Broadcast a message");
btnSend.addActionListener(e -> {
try {
    btnSend_Handler();
} catch(Exception ex) {
    ex.printStackTrace();
}
});

JButton button = new JButton("Send file");
button.setToolTipText("Broadcast a file");
button.addActionListener(e ->{
    try {
        btnSendFile_Handler();
    } catch(Exception ex) {
        ex.printStackTrace();
    }
});

frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
frame.setTitle(FRM_TITLE);
frame.setLocation(FRM_LOC_X, FRM_LOC_Y);
frame.setSize(FRM_WIDTH, FRM_HEIGHT);
frame.setResizable(false);
frame.getContentPane().add(BorderLayout.NORTH, spMain);
frame.getContentPane().add(BorderLayout.CENTER, tfMsg);
frame.getContentPane().add(BorderLayout.EAST, btnSend);
frame.getContentPane().add(BorderLayout.WEST, button);
frame.setVisible(true);
}

private void antistatic(){
    Scanner in = new Scanner(System.in);
    System.out.print("Input IP: ");
    IP_BROADCUST = in.nextLine();
    framDraw(new App());
    new theReceiver().start();
}

public void GetDialog(){
    try{
        file = new File("Dialog.txt");
        fw = new FileWriter(file);
        fw.write(dialog);
        fw.close();
    } catch(Exception exept){
        exept.printStackTrace();
    }
}

public static void main( String[] args )
{
    //new App().antistatic();
    App app = new App();
    app.antistatic();
}
}