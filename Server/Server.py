import _thread
import socket

myHost = 'localhost'
myPort = 6667

# Creates a TCP Server with Port# 6667
sockobj = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sockobj.bind((myHost, myPort))
sockobj.listen(16)


class Account:
    account_list = []

    def __init(self, login, password):
        pass

    # Check is username is in use by an existing account
    @staticmethod
    def account_exists(username):
        pass

    # First checks if username is in use then makes account
    @staticmethod
    def create_account(connection, username, password):
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
    pass


def handle_client(connection):
    data = connection.recv(1024).decode()
    print("Received from new connection:" + data)

    connection.sendall(data.encode())
    connection.close()


# listen until process killed
def dispatcher():
    while True:  # wait for next connection
        connection, address = sockobj.accept()  # pass to thread for service
        _thread.start_new(handle_client, (connection,))


dispatcher()
