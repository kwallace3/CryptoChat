import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.sql.*;
import java.time.*;

class Database {
    String dbUrl;
    Connection connection;

    public Database(String dbUrl, Connection connection){
        this.dbUrl = dbUrl;
        this.connection = connection;
    }

    public void insertUserInfo(String username, String email, String password) {
        try {

            //create and execute statement to add data to user info table
            Statement statement = this.connection.createStatement();
            statement.executeUpdate(String.format("INSERT INTO users VALUES ('%s', '%s', '%s')", username, email, password));
        }
        catch (Exception exception) {
            System.out.println(exception);
        }
    }

    public void insertMessageInfo(String message, String receiver, String sender) {
        try {

            //establish time and date when message is sent
            LocalDate date = LocalDate.now();
            LocalTime time = LocalTime.now();

            //create and execute statement to add data to message info table
            Statement statement = connection.createStatement();
            statement.executeUpdate(String.format("INSERT INTO messages (mText, reciever, sendD, sendT, sender) VALUES ('%s', '%s', '%s', '%s', '%s')", message, receiver, date, time, sender));
        }
        catch (Exception exception) {
            System.out.println(exception);

        }
    }

    public void getMessage(String sender) {
        try {

            //connect to database
            Statement statement = connection.createStatement();

            //qeury
            ResultSet result = statement.executeQuery(String.format("SELECT mText FROM messages WHERE sender = '%s';", sender));
            ResultSetMetaData mdata = result.getMetaData();

            //format and print results
            int columnsNumber = mdata.getColumnCount();
            while (result.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(result.getString(i));
                    if (i > 1) System.out.print(" | ");
                }
            }
        }
        catch (Exception exception) {
            System.out.println(exception);

        }
    }

    //get date sent from message text
    public void getDateSent(String message) {
        try {
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(String.format("SELECT sendD FROM messages WHERE mText = '%s';", message));
            ResultSetMetaData mdata = result.getMetaData();

            int columnsNumber = mdata.getColumnCount();
            while (result.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(result.getString(i));
                    if (i > 1) System.out.print(" | ");
                }
            }

        }
        catch (Exception exception) {
            System.out.println(exception);

        }
    }

    //get time sent from message text
    public void getTimeSent(String message) {
        try {
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(String.format("SELECT sendT FROM messages WHERE mText = '%s';", message));
            ResultSetMetaData mdata = result.getMetaData();

            int columnsNumber = mdata.getColumnCount();
            while (result.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(result.getString(i));
                    if (i > 1) System.out.print(" | ");
                }
            }

        }
        catch (Exception exception) {
            System.out.println(exception);

        }
    }

    //get email from username
    public void getEmail(String user) {
        try {
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(String.format("SELECT email FROM users WHERE username = '%s';", user));
            ResultSetMetaData mdata = result.getMetaData();

            int columnsNumber = mdata.getColumnCount();
            while (result.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(result.getString(i));
                    if (i > 1) System.out.print(" | ");
                }
            }
        }
        catch (Exception exception) {
            System.out.println(exception);

        }
    }

    //get username from email
    public void getUsername(String email) {
        try {
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(String.format("SELECT username FROM users WHERE email = '%s';", email));
            ResultSetMetaData mdata = result.getMetaData();
            int columnsNumber = mdata.getColumnCount();

            while (result.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(result.getString(i));
                    if (i > 1) System.out.print(" | ");
                }

            }

        }
        catch (Exception exception) {
            System.out.println(exception);

        }
    }

    //get password from username
    public void getPassword(String username) {
        try {
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(String.format("SELECT password FROM users WHERE username = '%s';", username));
            ResultSetMetaData mdata = result.getMetaData();

            int columnsNumber = mdata.getColumnCount();
            while (result.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(result.getString(i));
                    if (i > 1) System.out.print(" | ");
                }
            }
        }
        catch (Exception exception) {
            System.out.println(exception);

        }
    }

    public void close() throws SQLException {
        connection.close();
    }
}

// Account Class: Handles accounts and all information in accounts
class Account {
    private static ReadWriteLock account_lock = new ReentrantReadWriteLock();
    private static ArrayList<Account> account_list = new ArrayList<>();
    private ReadWriteLock channel_lock = new ReentrantReadWriteLock();
    private ArrayList<Channel> channel_list = new ArrayList<>();
    private ReadWriteLock admin_lock = new ReentrantReadWriteLock();
    private ArrayList<Channel> admin_list = new ArrayList<>();
    private ReadWriteLock block_lock = new ReentrantReadWriteLock();
    private ArrayList<Account> blocked_users = new ArrayList<>();
    private Database database;

    String username;
    private String email;
    private String password;

    private Account(String username, String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;
        account_lock.writeLock().lock();
        try {
            account_list.add(this);
        } finally {
            account_lock.writeLock().unlock();
        }
    }

    // Return account with matching username
    // Return null if account does not exist
    public static Account get_account(String username){
        account_lock.readLock().lock();
        try {
            for (Account account : account_list) {
                if (account.username.equals(username))
                    return account;
            }
        } finally {
            account_lock.readLock().unlock();
        }
        return null;
    }

    // Check is username is in use by an existing account
    public static boolean username_exists(String username){
        return get_account(username) != null;
    }

    //Check is username is in use by an existing account
    public static boolean email_exists(String email){
        account_lock.readLock().lock();
        try {
            for (Account account : account_list) {
                if (account.email.equals(email))
                    return true;
            }
        } finally {
            account_lock.readLock().unlock();
        }
        return false;
    }

    //First checks if username or email is in use then makes account
    public static String create_account(String username, String email, String password){
        if (username_exists(username)){
            return(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"createaccount", "failure", username, username + " in use"}));
        }
        if (email_exists(email)){
            return(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"createaccount", "failure", username, email + " in use"}));
        }

        new Account(username, email, password);

        return(JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"createaccount", "success", username}));

    }

    //Logs connection into account username and password if both username and password match
    public static String login(String username, String password) {
        account_lock.readLock().lock();
        try {
            for (int i = 0; i < account_list.size(); i++) {
                if (account_list.get(i).username.equals(username) && Account.account_list.get(i).password.equals(password)) {
                    if (Account.account_list.get(i).password.equals(password))
                        return (JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"login", "success", username}));
                    else
                        return (JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"login", "failure", username}));
                }
            }
        } finally {
            account_lock.readLock().unlock();
        }
        return (JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"login", "failure", username}));
    }

    //adds other_user to the list of blocked_users
    //if other_user is already blocked or does not exist sends connection an error message
    public String block_user(String other_user){
        Account other_account = get_account(other_user);
        if (other_account == null)
            return JavaServer.format_message(new int[]{0, 1, 2, 2, 2}, new String[]{"block", "failure", this.username,  other_user, other_user + " does not exist"});
        if (this.add_block_user(other_account))
            return JavaServer.format_message(new int[]{0, 1, 2, 2, 2}, new String[]{"block", "failure", this.username,  other_user, other_user + " is already blocked"});
        other_account.add_block_user(this);
        return JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"block", "success", this.username,  other_user});
    }

    //adds other user to blocked_user list
    public Boolean add_block_user(Account other_account){
        if (this.is_user_blocked(other_account))
            return false;
        block_lock.writeLock().lock();
        try {
            blocked_users.add(other_account);
        } finally {
            account_lock.writeLock().unlock();
        }
            return true;
    }

    //returns true if other_account is in blocked_users
    public boolean is_user_blocked(Account other_account){
        return this.blocked_users.contains(other_account);
    }

    //checks that user is in the channel then the channel is added to admin_list
    public boolean make_user_admin(Channel channel){
        if(!this.is_in_channel(channel) || channel.is_admin(this))
            return false;
        admin_lock.writeLock().lock();
        try {
            admin_list.add(channel);
        } finally {
            admin_lock.writeLock().unlock();
        }
        return true;
    }

    //Adds user to channel if channel_name and password match (null means no password)
    public String join_channel(String channel_name){
        return (this.join_channel(channel_name, null));
    }
    public String join_channel(String channel_name, String password){
        if (this.is_in_channel(channel_name))
            return (JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"joinchannel", "failure", channel_name, "You are already in " + channel_name}));
        Channel channel = Channel.get_channel(channel_name);
        if (channel == null)
            return (JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"joinchannel", "failure", channel_name, channel_name + " does not exist"}));
        if (!channel.password.equals(password))
            return (JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"joinchannel", "failure", channel_name, channel_name + "'s password does not match"}));
        channel_lock.writeLock().lock();
        try {
            channel_list.add(channel);
        } finally {
            channel_lock.writeLock().unlock();
        }
        channel_lock.writeLock().lock();
        channel.add_account(this);
        return (JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"joinchannel", "success", channel_name}));
    }

    //returns true if user is in channel otherwise false
    public boolean is_in_channel(Channel channel){
        channel_lock.readLock().lock();
        if(channel == null)
            return false;
        try {
            for (Channel c : channel_list) {
                if (c.channel_name.equals(channel.channel_name))
                    return true;
            }
        } finally {
            channel_lock.readLock().unlock();
        }
        return false;
    }
    public boolean is_in_channel(String channel_name){
        return (this.is_in_channel(Channel.get_channel(channel_name)));
    }
}

class Channel {
    private static ReadWriteLock channel_lock = new ReentrantReadWriteLock();
    private static ArrayList<Channel> channel_list = new ArrayList<>();
    private ReadWriteLock account_lock = new ReentrantReadWriteLock();
    private ArrayList<Account> account_list = new ArrayList<>();
    private ReadWriteLock admin_lock = new ReentrantReadWriteLock();
    private ArrayList<Account> admin_list = new ArrayList<>();
    String channel_name;
    String password;

    private Channel (Account admin, String channel_name, String password){
        this.channel_name = channel_name;
        this.password = password;

        channel_lock.writeLock().lock();
        try {
            channel_list.add(this);
        } finally {
            channel_lock.writeLock().unlock();
        }

        this.admin_lock.writeLock().lock();
        try {
            this.admin_list.add(admin);
        } finally {
            this.admin_lock.writeLock().unlock();
        }
    }

    // Return account with matching channel_name
    // Return null if channel does not exist
    public static Channel get_channel(String channel_name){
        channel_lock.readLock().lock();
        try {
            for (Channel channel : channel_list) {
                if (channel.channel_name.equals(channel_name))
                    return channel;
            }
        } finally {
            channel_lock.readLock().unlock();
        }
        return null;
    }

    // Create's a new channel if the channel_name us not in use
    // admin is added as admin of the channel
    public static String create_channel(Account admin, String channel_name){
        return create_channel(admin, channel_name, null);
    }
    public static String create_channel(Account admin, String channel_name, String password){
        if (get_channel(channel_name) != null){
            return(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"createchannel", "failure", channel_name, channel_name + " in use"}));
        }
        new Channel(admin, channel_name, password);
        return(JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"createchannel", "success", channel_name}));
    }

    //Adds account to channel
    public void add_account(Account account){
        account_lock.writeLock().lock();
        try {
            account_list.add(account);
        } finally {
            account_lock.writeLock().unlock();
        }
    }

    private boolean add_admin (Account account){
        if (this.is_admin(account) || !this.is_in_channel(account))
            return false;
        account_lock.writeLock().lock();
        try {
            if (account.make_user_admin(this)) {
                account_list.add(account);
                return true;
            }
            return false;
        }
        finally {
            account_lock.writeLock().unlock();
        }
    }

    public boolean is_admin(Account account){
        admin_lock.readLock().lock();
        try {
             return (admin_list.contains(account));
        }
        finally {
            admin_lock.readLock().unlock();
        }
    }

    public boolean is_in_channel(Account account){
        account_lock.readLock().lock();
        try {
            return (account_list.contains(account));
        }
        finally {
            account_lock.readLock().unlock();
        }
    }

    // Promote account to admin
    public String promote_user(Account account){
        if (!this.is_in_channel(account))
            return (JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"promoteuser", "failure", this.channel_name, account.username + " is not in " + this.channel_name}));
        if (this.is_admin(account))
            return (JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"promoteuser", "failure", this.channel_name, account.username + " is already am admin of " + this.channel_name}));
        this.add_admin(account);
        Client.send_message(JavaServer.format_message(new int[]{0, 2}, new String[]{"recievedpromoteuser", this.channel_name}), account.username);
        return (JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"promoteuser", "success", this.channel_name, account.username}));
    }
}

// ClientHandler class
class Client extends Thread {
    static ReadWriteLock lock = new ReentrantReadWriteLock();
    static ArrayList<Client> client_list = new ArrayList<>();
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    boolean closed;
    Account account;

    // Constructor
    public Client(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.account = null;
        this.closed = false;
        lock.writeLock().lock();
        try {
            client_list.add(this);
        } finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public void run() {
        String data;
        while (!this.closed) {

            // receive the answer from client
            data = receive_message();
            if (data == null){
                this.closed = true;
            }
            else {
                System.out.println(data);
                handle_message(data);
            }
        }
        try {
            this.dos.close();
            this.dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lock.writeLock().lock();
        try {
            client_list.remove(this);
        } finally {
            lock.writeLock().unlock();
        }
        System.out.println("Client Disconnected " + s);
    }

    // reads the message and calls the corresponding method with relevant information
    public void handle_message(String message) {
        String[] split = JavaServer.split_message(message);
        if (split.length < 2)
            send_message(JavaServer.format_message(new int[]{0, 2}, new String[]{"error", "Incorrect Message Format"}));
        else if (split[0].equals("createaccount") && split.length > 3)
            send_message(Account.create_account(split[1], split[2], split[3]));
        else if (split[0].equals("login") && split.length > 2) {
            String result = Account.login(split[1], split[2]);
            if (result.substring(7, 14).equals("success"))
                account = Account.get_account(split[1]);
            send_message(result);
        }
        else if (split[0].equals("logout")){
            if (account == null){
                send_message(JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"logout", "failure", "You are not logged into " + split[1]}));
            }
            else if (account.username.equals(split[1])){
                account = null;
                send_message(JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"logout", "success", split[1]}));
            }
            else
                send_message(JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"logout", "failure", "You are not logged into " + split[1]}));
        }
        else if (split[0].equals("senddirectmessage") && split.length > 2 && account != null){
            if(send_message(JavaServer.format_message(new int[]{0, 2, 2, 3}, new String[]{"receiveddirectmessage", account.username, split[2], split[3]}), split[1])){
                send_message(JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"senddirectmessage", "success", split[1]}));
            }
            else
                send_message(JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"senddirectmessage", "failure", split[1], "Unable to send message to " + split[1]}));
        }
        else if (split[0].equals("block")){
            if (account != null){
                send_message(JavaServer.format_message(new int[]{0, 1, 2}, new String[]{"block", "failure", split[1], split[2], "You must be logged in"}));
            }
            else {
                send_message(account.block_user(split[2]));
            }
        }
        else if (split[0].equals("createchannel")){
            if (account == null){
                send_message(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"createchannel", "failure", split[2], "You must be logged in"}));
            }
            if (split.length == 2)
                send_message(Channel.create_channel(account, split[1]));
            else
                send_message(Channel.create_channel(account, split[1], split[2]));
        }
        else if (split[0].equals("joinchannel")){
            if (account == null){
                send_message(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"joinchannel", "failure", split[2], "You must be logged in"}));
            }
            if (split.length == 2)
                send_message(account.join_channel(split[1]));
            else
                send_message(account.join_channel(split[1], split[2]));
        }
        else if (split[0].equals("promoteuser") && split.length > 1){
            Channel channel = Channel.get_channel(split[1]);
            Account other_user = Account.get_account(split[2]);
            if (account == null){
                this.send_message(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"promoteuser", "failure", split[1], "You are not logged in"}));
            }
            if (other_user == null){
                this.send_message(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"promoteuser", "failure", split[1], split[2] + " is not a user"}));
            }
            else if (channel == null){
                this.send_message(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"promoteuser", "failure", split[1], split[1], " is not a channel"}));
            }
            else if (!channel.is_admin(account)){
                this.send_message(JavaServer.format_message(new int[]{0, 1, 2, 2}, new String[]{"promoteuser", "failure", split[1], "You must be an admin of " + split[1]}));
            }
            else {
                this.send_message(channel.promote_user(other_user));
            }
        }
        else
            send_message(JavaServer.format_message(new int[]{0, 2}, new String[]{"error", "Incorrect Message Format"}));
    }

    //Sends a message to the Client
    public void send_message(String message) {
        try {
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException ignored){

        }
    }

    //Sends a message to another Client with username
    public static boolean send_message(String message, String username){
        lock.readLock().lock();
        try {
            for (Client client : client_list) {
                if (client.account != null) {
                    if (client.account.username.equals(username)) {
                        client.send_message(message);
                        return true;
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    public String receive_message() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            return null;
        }
    }

}

// Server class
public class JavaServer {
    public static void main(String[] args) throws IOException, SQLException {
        // server is listening on port 4995
        ServerSocket ss = new ServerSocket(4995);
        String dbUrl = "jdbc:mysql://104.154.41.246:3306/CRYPTOCHAT";
        Database database = new Database(dbUrl, DriverManager.getConnection(dbUrl, "root", "CryptoChat"));
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
                Thread t = new Client(s, dis, dos);

                // Invoking the start() method
                t.start();

            } catch (Exception e) {
                if (s != null) {
                    s.close();
                }
                e.printStackTrace();

            }
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
        StringBuilder message = new StringBuilder(data[0]);
        for (int i = 1; i < data.length; i++) {
            message.append('%');
            int length = data[i].length();
            int digits = (int) Math.log10(length) + 1;
            while (digits < size[i]) {
                digits += 1;
                message.append('0');
            }
            message.append(length).append(data[i]);
        }
        return message.toString();
    }

    public static String[] ListToArray(ArrayList<String> list) {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
            arr[i] = list.get(i);
        return arr;
    }
}