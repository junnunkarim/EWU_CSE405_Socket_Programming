import java.io.*;
import java.net.*;

public class Server {
  private ServerSocket serverSocket;

  // constructor
  public Server(ServerSocket serverSocket) { this.serverSocket = serverSocket; }

  // methods
  public void startServer() {
    try {
      while (!serverSocket.isClosed()) {
        // wait for client connection
        Socket socket = serverSocket.accept();

        // create a new thread to handle the client communication
        ClientHandler clientHandler = new ClientHandler(socket);

        Thread thread = new Thread(clientHandler);
        thread.start();
      }
    } catch (IOException e) {
      close();
    }
  }

  public void close() {
    try {
      if (serverSocket != null) {
        serverSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // main
  public static void main(String[] args) throws IOException {
    int port = 5678;

    ServerSocket serverSocket = new ServerSocket(port);
    Server server = new Server(serverSocket);

    System.out.printf("Server started on port %d\n", port);
    System.out.println("Waiting for clients...\n");

    server.startServer();
  }
}

// class
class ClientHandler implements Runnable {
  private Socket socket;
  // for sending output stream to client
  private PrintWriter sender;
  // for getting input stream from client
  private BufferedReader reciever;

  // constructor
  public ClientHandler(Socket socket) {
    try {
      this.socket = socket;
      this.sender = new PrintWriter(socket.getOutputStream(), true);
      this.reciever =
          new BufferedReader(new InputStreamReader(socket.getInputStream()));

    } catch (IOException e) {
      close(socket, sender, reciever);
    }
  }

  // methods
  public void close(Socket socket, PrintWriter sender,
                    BufferedReader reciever) {
    try {
      if (reciever != null) {
        reciever.close();
      }
      if (sender != null) {
        sender.close();
      }
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendMessage(String message) {
    try {
      sender.println(message);
    } catch (Exception e) {
      close(socket, sender, reciever);
    }
  }

  private String calculateQuadratic(int a, int b, int c) {
    int discriminant = (b * b) - (4 * a * c);
    String response = " ";

    if (discriminant > 0) {
      double root1 = (-b + Math.sqrt(discriminant)) / (2 * a);
      double root2 = (-b - Math.sqrt(discriminant)) / (2 * a);

      response = String.format("x1 = %.4f, x2 = %.4f", root1, root2);
    } else if (discriminant == 0) {
      double root = (-b) / (2 * a);

      response = String.format("x1 = %.4f", root);
    } else {
      response = "No Real solution!";
    }

    return response;
  }

  // thread
  @Override
  public void run() {
    try {
      int clientID = 0;

      String recieve = " ";
      String response = " ";
      String message = " ";

      while (socket.isConnected()) {
        recieve = reciever.readLine();

        if (recieve != null) {
          clientID = Integer.parseInt(recieve);
          System.out.printf("Client %s connected!\n", clientID);

          message = reciever.readLine();

          System.out.printf("Client %s Sent: %s!\n", clientID, message);

          switch (clientID) {
          case 0:
            // receive string, convert to lowercase, and send back
            response = message.toLowerCase();

            break;
          case 1:
            // ceceive number, square it, and send back
            int number = Integer.parseInt(message);
            response = String.valueOf(number * number);

            break;
          case 2:
            // receive 3 numbers, calculate quadratic formula, and send back
            String[] strings = message.split(" ");

            int a = Integer.parseInt(strings[0]);
            int b = Integer.parseInt(strings[1]);
            int c = Integer.parseInt(strings[2]);
            response = calculateQuadratic(a, b, c);

            break;
          default:
            response = "Invalid Client type!";
          }

          sendMessage(response);
        }
      }
    } catch (IOException e) {
      close(socket, sender, reciever);
    }
  }
}
