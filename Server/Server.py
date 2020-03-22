import _thread
import socket
import math

# myHost = "10.150.0.2"
myHost = "localhost"
myPort = 4995

# Creates a TCP Server with Port# 6667
sockobj = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sockobj.bind((myHost, myPort))
sockobj.listen(64)


# Connections connected to the client


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
        print(message)
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
        return

    @staticmethod
    def remove_client(connection):
        for i in range(len(Client.client_list)):
            if Client.client_list[i].connection == connection:
                if Client.client_list[i].account is not None:
                    Client.client_list[i].account.loggedin = False
                    Client.client_list[i].account.connection = None
                Client.client_list.pop(i)


class Account:
    account_list = []

    def __init__(self, username, email, password):
        self.account_list.append(self)
        self.username = username
        self.email = email
        self.password = password
        self.loggedin = False
        self.connection = None

    # Check is username is in use by an existing account
    @staticmethod
    def username_exists(username):
        for i in range(len(Account.account_list)):
            if Account.account_list[i].username == username:
                return True
        return False

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

    @staticmethod
    def login(connection, username, password):
        for i in range(len(Account.account_list)):
            if Account.account_list[i].username == username:
                if Account.account_list[i].password == password:
                    Client.get_client(connection).account = Account.account_list[i]
                    Account.account_list[i].loggedin = True
                    Account.account_list[i].connection = connection
                    connection.sendall(format_message([0, 1, 2], ["login", "success", username]).encode())
                else:
                    connection.sendall(format_message([0, 1, 2], ["login", "failure", username]).encode())

    def logout(self, connection, username):
        Client.get_client(connection).account = None
        self.loggedin = False
        connection.sendall(format_message([0, 1, 2], ["logout", "success", username]).encode())

    def block_user(self, other_user):
        pass

    @staticmethod
    def get_account(username):
        for i in range(len(Account.account_list)):
            if Account.account_list[i].username == username:
                return Account.account_list[i]
        return


def send_message(connection, receiver, message):
    pass


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
    else:
        connection.sendall(format_message([0, 2], ["error", "Incorrect Message Format"]).encode())


def handle_client(connection):
    print("New Connection" + str(connection))
    try:
        data = connection.recv(1024).decode()
    except ConnectionResetError or ConnectionAbortedError:
        print("Connection Closed" + str(connection))
        return
    print(data)
    client = Client(connection)
    handle_message(connection, data)
    try:
        while True:
            data = connection.recv(1024).decode()
            print(data)
            handle_message(connection, data)
    except ConnectionResetError or ConnectionAbortedError:
        Client.remove_client(connection)
        print("Connection Closed" + str(connection))
        connection.close()


# listen until process killed
def dispatcher():
    while True:  # wait for next connection
        connection, address = sockobj.accept()  # pass to thread for service
        _thread.start_new(handle_client, (connection,))


dispatcher()
