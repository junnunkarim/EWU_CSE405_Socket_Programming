import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements Runnable {
  private static final int NUM_CLIENTS = 3;

  private Socket socket;
  // for sending output stream to server
  private PrintWriter sender;
  // for getting input stream from server
  private BufferedReader reciever;
  private int clientID = 0;

  private Random random;

  // constructor
  public Client(int clientID, String address, int port) {
    try {
      // connect to server
      socket = new Socket(address, port);
      this.socket = socket;

      // this.sender =
      this.sender = new PrintWriter(socket.getOutputStream(), true);
      this.reciever =
          new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.clientID = clientID;

      this.random = new Random();
    } catch (IOException e) {
      e.printStackTrace();
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
      e.printStackTrace();
      close(socket, sender, reciever);
    }
  }

  private String generateRandomString(int length) {
    StringBuilder str = new StringBuilder();

    for (int i = 0; i < length; i++) {
      str.append((char)(random.nextInt(26) + 'A')); // random uppercase letter
    }

    return str.toString();
  }

  // thread
  @Override
  public void run() {
    try {
      System.out.printf("Client %d connected to Server!\n", clientID);

      String response = " ";
      String message = " ";

      // loop to send random messages
      while (true) {
        // send clientID to server
        sendMessage(String.valueOf(clientID));

        switch (clientID) {
        case 0:
          // send random string
          message = generateRandomString(10);

          System.out.printf("Client %d sent string: %s\n", clientID, message);
          break;
        case 1:
          // send random number
          message = String.valueOf(random.nextInt(20)); // range 0 to 20

          System.out.printf("Client %d sent number: %s\n", clientID, message);
          break;
        case 2:
          // send 3 random numbers
          double a = random.nextDouble();
          double b = random.nextDouble();
          double c = random.nextDouble();

          message = String.format("%.4f %.4f %.4f", a, b, c);

          System.out.printf("Client %d sent coefficients: %.2f %.2f %.2f\n",
                            clientID, a, b, c);
          break;
        default:
          message = "Invalid Client type!";
        }

        sendMessage(message);

        response = reciever.readLine();

        if (response != null) {
          System.out.printf("Client %d received response: %s\n", clientID,
                            response);
        } else {
          System.out.printf("Server is disconnected! Client %d exited!\n\n",
                            clientID);

          close(socket, sender, reciever);
          break;
        }

        // simulate n seconds delay between messages
        int n = 2;

        try {
          Thread.sleep(n * 1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();

          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      close(socket, sender, reciever);
    }
  }

  // main
  public static void main(String[] args) throws IOException {
    String address = "127.0.0.1";
    int port = 5678;

    // create and start threads for each client
    Thread[] threads = new Thread[NUM_CLIENTS];

    for (int ID = 0; ID < NUM_CLIENTS; ID++) {
      threads[ID] = new Thread(new Client(ID, address, port));
      threads[ID].start();
    }
  }
}
