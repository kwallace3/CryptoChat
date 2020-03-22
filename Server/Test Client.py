import socket

# For testing a simple TCP server
# Not for final use

# serverName = "cryptochatwit.duckdns.org"
serverName = "localhost"
serverPort = 4995
clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
clientSocket.connect((serverName, serverPort))
message = input('Input Message:')
while message != 'exit':
    clientSocket.send(message.encode())
    modifiedSentence = clientSocket.recv(1024)
    print('From Server:', modifiedSentence.decode())
    message = input('Input Message:')
clientSocket.close()
