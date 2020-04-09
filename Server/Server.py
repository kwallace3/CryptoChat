import _thread
import socket
import math

# myhost 10.150.0.2 is for use on the server while localhost is for testing. Comment out the one you are not using
# myHost = "10.150.0.2"
myHost = "localhost"
myPort = 4995

# Creates a TCP Server with Port# 6667
sockobj = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sockobj.bind((myHost, myPort))
sockobj.listen(64)


# Connections connected to the client

# Splits message into a list with all parts separated
# Message is received in format header%##data%##data...%##data
# All messages are in that format besides for for the 3rd data in senddirectmessage
def split_message(message):
    split = []
    # Remove the message before the first '%' and save it as split[0]. This is the message header
    for i in range(len(message)):
        if message[i] == '%':
            split.append(message[:i])
            message = message[i + 1:]
            break

    # Checks the header was successfully taken off, if not return empty split
    if len(split) == 0:
        return split

    x = 1
    while x > 0:
        # Checks to see if we are on the 3rd data of the senddirectmessage header
        if split[0] == 'senddirectmessage' and x == 3:
            size = 3
        else:
            size = 2
        length = int(message[:size])
        split.append(message[size:length + size])

        x += 1
        # breaks out of loop if next character is not '%' or we are at the end of the message
        if message[length + size:] == '':
            break
        if message[length + size] != '%':
            break
        message = message[length + size + 1:]

    return split


# formats list size and list data into a string in the format of header%##data%##data...%##data
# %## is the number of characters in the data with the number of # being in list size
# example size=[0, 2] and data=[header, testdata] returns header%08testdata
def format_message(size, data):
    message = data[0]
    for i in range(1, len(data)):
        message += '%'
        length = len(data[i])
        digits = int(math.log10(length)) + 1
        while digits < size[i]:
            digits += 1
            message += '0'
        message += str(length) + data[i]
    return message


# Handles connections from clients
class Client:
    client_list = []

    def __init__(self, connection):
        self.account = None
        self.client_list.append(self)
        self.connection = connection

    @staticmethod
    def get_client(connection):
        for i in range(len(Client.client_list)):
            if Client.client_list[i].connection == connection:
                return Client.client_list[i]
        return None

    @staticmethod
    def remove_client(connection):
        for i in range(len(Client.client_list)):
            if Client.client_list[i].connection == connection:
                if Client.client_list[i].account is not None:
                    Client.client_list[i].account.loggedin = False
                    Client.client_list[i].account.connection = None
                Client.client_list.pop(i)


# Handles accounts and all information in accounts
class Account:
    account_list = []

    def __init__(self, username, email, password):
        self.account_list.append(self)
        self.username = username
        self.email = email
        self.password = password
        self.loggedin = False
        self.connection = None
        self.blocked_users = []

        self.admin_channel = []

    # Check is username is in use by an existing account
    @staticmethod
    def username_exists(username):
        for i in range(len(Account.account_list)):
            if Account.account_list[i].username == username:
                return True
        return False

    # Check is username is in use by an existing account
    @staticmethod
    def email_exists(email):
        for i in range(len(Account.account_list)):
            if Account.account_list[i].email == email:
                return True
        return False

    # First checks if username is in use then makes account
    @staticmethod
    def create_account(connection, username, email, password):
        if Account.username_exists(username):
            connection.sendall(
                format_message([0, 1, 2, 2], ["createaccount", "failure", username, username + " in use"]).encode())
            return
        if Account.email_exists(email):
            connection.sendall(
                format_message([0, 1, 2, 2], ["createaccount", "failure", username, email + " in use"]).encode())
            return

        Account(username, email, password)

        connection.sendall(format_message([0, 1, 2], ["createaccount", "success", username]).encode())

    # Logs connection into account username and password if both username and password match
    @staticmethod
    def login(connection, username, password):
        for i in range(len(Account.account_list)):
            if Account.account_list[i].username == username:
                if Account.account_list[i].password == password:
                    Client.get_client(connection).account = Account.account_list[i]
                    Account.account_list[i].loggedin = True
                    Account.account_list[i].connection = connection
                    print(str(Account.account_list[i].connection))
                    connection.sendall(format_message([0, 1, 2], ["login", "success", username]).encode())
                else:
                    connection.sendall(format_message([0, 1, 2], ["login", "failure", username]).encode())

    # Logs connection out of account
    def logout(self, connection, username):
        Client.get_client(connection).account = None
        self.loggedin = False
        connection.sendall(format_message([0, 1, 2], ["logout", "success", username]).encode())

    # Returns True if self has user blocked otherwise return False
    def is_user_blocked(self, user):
        for i in range(len(self.blocked_users)):
            if self.blocked_users[i] == user:
                return True
        return False

    # self adds other_user to the list of blocked_users
    # if other_user is already blocked or does not exist sends connection an error message
    def block_user(self, connection, other_user):
        if self.is_user_blocked(other_user):
            connection.sendall(format_message([0, 1, 2, 2], ["block", "failure", self.username, other_user,
                                                             "User already blocked"]).encode())
            return False
        if Account.username_exists(other_user):
            self.blocked_users.append(other_user)
            connection.sendall(format_message([0, 1, 2, 2], ["block", "success", self.username, other_user]).encode())
            return True
        else:
            connection.sendall(format_message([0, 1, 2, 2], ["block", "failure", self.username, other_user,
                                                             "User not found"]).encode())
            return False

    # returns Account with matching username
    @staticmethod
    def get_account(username):
        for i in range(len(Account.account_list)):
            if Account.account_list[i].username == username:
                return Account.account_list[i]
        return


class Channel:
    channel_list = []

    def __init__(self, account, channel_name, password=None):
        self.channel_list.append(self)
        self.channel_admin_list = []
        self.channel_admin_list.append(account)
        self.channel_name = channel_name
        self.password = password

        account.admin_channel.append(self)

    # Create new channel with channel_name if a channel without that name does not exist
    # If a channel already has that name return an error to the user and exit without creating a channel
    # By default channels have no password unless one was provided
    @staticmethod
    def create_channel(connection, channel_name, password=None):
        for i in range(len(Channel.channel_list)):
            if Channel.channel_list[i].channel_name == channel_name:
                connection.sendall(format_message([0, 1, 2, 2], ["senddirectmessage", "failure", channel_name,
                                                                 channel_name + " already exists"]).encode())
                return
        Channel(connection.account, channel_name, password)


# Send a message between connection and receiver with timestamp and message
# Checks if connection is logged in and if the receiver has an account
def send_message(connection, receiver, timestamp, message):
    account = Client.get_client(connection).account
    if account.is_user_blocked(receiver):
        connection.sendall(format_message([0, 1, 2, 2], ["senddirectmessage", "failure", receiver,
                                                         "You have blocked " + receiver]).encode())
        return
    if account.loggedin is None:
        connection.sendall(
            format_message([0, 1, 2, 2], ["senddirectmessage", "failure", receiver, "Not logged in"]).encode())
        return
    receiver_account = Account.get_account(receiver)
    if receiver_account is None:
        connection.sendall(
            format_message([0, 1, 2, 2], ["senddirectmessage", "failure", receiver, "User not found"]).encode())
        return
    if receiver_account.connection is None or receiver_account.loggedin is False:
        connection.sendall(
            format_message([0, 1, 2, 2], ["senddirectmessage", "failure", receiver, "Reciever not logged in"]).encode())
        return
    if receiver_account.is_user_blocked(account.username):
        connection.sendall(format_message([0, 1, 2, 2], ["senddirectmessage", "failure", receiver,
                                                         "You are blocked by " + receiver]).encode())
        return
    receiver_account.connection.sendall(
        format_message([0, 2, 2, 3], ["receivedirectmessage", account.username, timestamp, message]).encode())

    connection.sendall(format_message([0, 1, 2], ["senddirectmessage", "success", receiver]).encode())


# Finds action message wants to takes and evokes method
def handle_message(connection, message):
    split = split_message(message)
    print(split)
    if len(split) == 0:
        connection.sendall(format_message([0, 2], ["error", "Incorrect Message Format"]).encode())
        return
    if split[0] == "createaccount" and len(split) >= 4:
        Account.create_account(connection, split[1], split[2], split[3])
    elif split[0] == "login" and len(split) >= 3:
        Account.login(connection, split[1], split[2])
    elif split[0] == "logout" and len(split) >= 2:
        Account.get_account(split[1]).logout(connection, split[1])
    elif split[0] == "checkuserexists" and len(split) >= 2:
        if Account.get_account(split[1]):
            connection.sendall(format_message([0, 1, 2], ["checkuserexists", "success", split[1]]).encode())
        else:
            connection.sendall(
                format_message([0, 1, 2, 2], ["checkuserexists", "failure", split[1], "User not found"]).encode())
    elif split[0] == "senddirectmessage" and len(split) >= 3:
        send_message(connection, split[1], split[2], split[3])
    elif split[0] == "block" and len(split) >= 2:
        client = Client.get_client(connection)
        if client is None:
            connection.sendall(format_message([0, 1, 2, 2, 2], ["block", "failure", split[1], split[2],
                                                                "You must be logged in"]).encode())
        else:
            client.account.block_user(connection, split[2])
    elif split[0] == "createchannel" and len(split) >= 2:
        if len(split) == 2:
            Channel.create_channel(connection, split[1])
        else:
            Channel.create_channel(connection, split[1], split[2])
    else:
        connection.sendall(format_message([0, 2], ["error", "Incorrect Message Format"]).encode())


# Is called when a new thread is created to deal with a new connection
def handle_client(connection):
    print("New Connection" + str(connection))
    Client(connection)
    try:
        data = connection.recv(1024).decode()
    except ConnectionResetError or ConnectionAbortedError:
        print("Connection Closed" + str(connection))
        Client.remove_client(connection)
        connection.close()
        return
    print(data)
    handle_message(connection, data)
    while True:
        try:
            data = connection.recv(1024).decode()
        except ConnectionResetError or ConnectionAbortedError:
            break
        print(data)
        handle_message(connection, data)

    Client.remove_client(connection)
    print("Connection Closed" + str(connection))
    connection.close()


# listen until process killed
def dispatcher():
    while True:  # wait for next connection
        connection, address = sockobj.accept()  # pass to thread for service
        _thread.start_new(handle_client, (connection,))


dispatcher()
