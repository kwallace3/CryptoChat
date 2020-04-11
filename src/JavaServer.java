import java.io.*;
import java.util.*;
import java.net.*;


// ClientHandler class
class ClientHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;


    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {
        String data;
        while (true) {
            send_message("Connected");

            // receive the answer from client
            data = receive_message();

            System.out.println(data);
            String[] split = split_message(data);
            System.out.println(split);
            handle_message(data);

        }
        /*try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }*/
    }

    public void handle_message(String message) {
        String[] split = split_message(message);
        if (split.length == 0)
            send_message(format_message(new int[]{0, 2}, new String[]{"error", "Incorrect Message Format"}));
        return;
    }

    public void send_message(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
        }
    }

    public String receive_message() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            return "";
        }
    }

    public static String[] split_message(String message) {
        ArrayList<String> split = new ArrayList<>();
        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) == '%') {
                split.add(message.substring(0, i));
                message = message.substring(i + 1);
                break;
            }
        }
        if (split.isEmpty())
            return ListToArray(split);

        int x = 1;
        int size;
        while (x > 0) {
            size = 2;
            //Checks to see if we are on the 3rd data of the senddirectmessage header
            if (split.get(0).equals("senddirectmessage") && x == 3) {
                size = 3;
            }
            int length = Integer.parseInt(message.substring(0, size));
            split.add(message.substring(size, size + length));

            x += 1;
            // breaks out of loop if next character is not '%' or we are at the end of the message
            if (length + size == message.length() || message.charAt(length + size) != '%') {
                break;
            }
            message = message.substring(length + size + 1);
        }

        return ListToArray(split);
    }

    public static String format_message(int[] size, String[] data) {
        String message = data[0];
        for (int i = 1; i < data.length; i++) {
            message += '%';
            int length = data[i].length();
            int digits = (int) Math.log10(length) + 1;
            while (digits < size[i]) {
                digits += 1;
                message += '0';
            }
            message += length + data[i];
        }
        return message;
    }

    public static String[] ListToArray(ArrayList<String> list) {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
            arr[i] = list.get(i);
        return arr;
    }
}

// Server class
public class JavaServer {
    public static void main(String[] args) throws IOException {
        // server is listening on port 4995
        ServerSocket ss = new ServerSocket(4995);

        // running infinite loop for getting
        // client request
        while (true) {
            Socket s = null;

            try {
                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                // create a new thread object
                Thread t = new ClientHandler(s, dis, dos);

                // Invoking the start() method
                t.start();

            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }
        }
    }
}