package edu.wit.login;

import android.annotation.SuppressLint;
import android.renderscript.ScriptGroup;

import androidx.appcompat.app.AppCompatActivity;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// reference:https://www.tutorialspoint.com/sending-and-receiving-data-with-sockets-in-android
@SuppressLint("SetTextI18n")
public class NetworkOPS extends AppCompatActivity {

    public Socket execute() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                InetAddress address = InetAddress.getByName("cryptochatwit.duckdns.org");
                Socket socket = new Socket(address, 4995);
                InputStream hello = socket.getInputStream();
                OutputStream message = socket.getOutputStream();
                System.out.print(hello + " " +message);

                return socket;
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        });
        return null;
    }
    //gets user data
    public String[] retrieve() throws IOException {
       Socket connect = execute();
        String[] data = new String[10];
        InputStream is = connect.getInputStream();
        InputStreamReader message = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(message);
        for(int i = 0; i<data.length;i++){
            data[i] = br.readLine();
        }
        return data;
    }
    // gets the username and password from the server
    public String retrieve(String user, String pass) throws Exception{
        Socket connect = execute();
        InputStream is = connect.getInputStream();
        InputStreamReader message = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(message);
        System.out.println(br);
        String response = br.readLine();

        if (response.contains(user) && response.contains(pass) ){
            connect.close();
            return "login%7success%##username";
        }
        else
            connect.close();
        return br.toString();

    }
    //writes to server/database
    public void write(String username, String Password, String Email) throws Exception{
        Socket connect = execute();
        // Get a reference to the socket's output stream.
        OutputStream os = connect.getOutputStream();
        //Send data to server
         username = findViewById(R.id.newname).toString();
        Password = findViewById(R.id.txtPassword).toString();
        Email = findViewById(R.id.txtEmail).toString();

        os.write(username.getBytes());
        os.write(Password.getBytes());
        os.write(Email.getBytes());

        connect.close();
    }


}