import java.io.*;
import java.net.*;
import java.util.Random;

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

  private Random random;

  // constructor
  public ClientHandler(Socket socket) {
    try {
      this.socket = socket;
      this.sender = new PrintWriter(socket.getOutputStream(), true);
      this.reciever =
          new BufferedReader(new InputStreamReader(socket.getInputStream()));

      this.random = new Random();
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

  private String calculateQuadratic(double a, double b, double c) {
    double result = (4 * a * a) + (2 * b) + c;

    return String.format("%.4f", result);
  }

  // thread
  @Override
  public void run() {
    while (socket.isConnected()) {
      try {
        String recieve = reciever.readLine();

        if (recieve != null) {
          int clientID = Integer.parseInt(recieve);
          System.out.printf("Client %s connected!\n", clientID);

          String response = "";
          String message = reciever.readLine();

          while (message == null) {
            continue;
          }

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

            double a = Double.parseDouble(strings[0]);
            double b = Double.parseDouble(strings[1]);
            double c = Double.parseDouble(strings[2]);
            response = calculateQuadratic(a, b, c);

            break;
          default:
            response = "Invalid Client type!";
          }

          sendMessage(response);
        }
      } catch (IOException e) {
        close(socket, sender, reciever);

        break;
      }
    }
  }
}
