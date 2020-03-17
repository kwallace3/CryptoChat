import _thread
import socket

myHost = 'localhost'
myPort = 4995

# Creates a TCP Server with Port# 6667
sockobj = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sockobj.bind((myHost, myPort))
sockobj.listen(16)


def splitmessage(message):
    split = [""]
    # Remove the message before the first '%' and save it as split[0]. This is the message header
    for i in range(len(message)):
        if message[i] == '%':
            split[0] = message[:i]
            message = message[i + 1:]
            break

    # Checks the header was successfully taken off, if not return empty split
    if split == [""]:
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


class Account:
    account_list = []

    def __init__(self, login, password):
        pass

    # Check is username is in use by an existing account
    @staticmethod
    def account_exists(username):
        pass

    # First checks if username is in use then makes account
    @staticmethod
    def create_account(connection, username, email, password):
        pass

    @staticmethod
    def login(connection, username, password):
        pass

    def logout(self, connection, username):
        pass

    def block_user(self, other_user):
        pass


def send_message(sender, receiver, message):
    pass


# Finds action message wants to takes and evokes method
def handle_message(connection, message):
    split = splitmessage(message)
    print(split)
    if len(split) == 1:
        connection.sendall("error%24Incorrect Message Format".encode())
        return
    if split[0] == "createaccount" and len(split) >= 4:
        Account.create_account(connection, split[1], split[2], split[3])
    elif split[0] == "login" and len(split) >= 3:
        Account.create_login(connection, split[1], split[2])
    elif split[0] == "logout" and len(split) >= 2:
        Account.create_logout(connection, split[1])
    else:
        connection.sendall("error%24Incorrect Message Format".encode())


def handle_client(connection):
    data = connection.recv(1024).decode()
    print("Received from new connection:" + data)
    handle_message(connection, data)
    connection.sendall(data.encode())
    connection.close()


# listen until process killed
def dispatcher():
    while True:  # wait for next connection
        connection, address = sockobj.accept()  # pass to thread for service
        _thread.start_new(handle_client, (connection,))


dispatcher()
