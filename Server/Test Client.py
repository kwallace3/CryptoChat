import socket
import _thread

# For testing a simple TCP server
# Not for final use
# myhost cryptochatwit.duckdns.org is for testing the server while localhost is for testing new builds of
# the server before deployment. Comment out the one you are not using
serverName = "cryptochatwit.duckdns.org"
# serverName = "localhost"
serverPort = 4995
clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
clientSocket.connect((serverName, serverPort))


def handle_message():
    while True:
        message = clientSocket.recv(1024)
        print('From Server:', message.decode())


# listen until process killed
def dispatcher():
    while True:  # wait for next connection
        _thread.start_new(handle_message, ())
        message = input("Message:")
        while message != 'exit':
            clientSocket.send(message.encode())
            message = input()
        clientSocket.close()


dispatcher()