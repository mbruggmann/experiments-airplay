package ch.mbruggmann.airplay.reverse;

import ch.mbruggmann.airplay.discovery.Device;

import java.io.*;
import java.net.Socket;

public class ReverseConnection implements Closeable {
  private final Device device;
  boolean stopped = false;

  public ReverseConnection(Device device) {
    this.device = device;
  }

  /**
   * Set up a reverse connection to the airplay device.
   */
  public void start() {
    reverseConnectionThread.start();
  }

  protected String readUntilEmptyLine(final BufferedReader reader) throws IOException {
    String line = reader.readLine();
    String result = line;
    while (line != null && !line.trim().isEmpty()) {
      line = reader.readLine();
      System.out.println(line);
      result += line;
    }
    return result;
  }

  /**
   * Stop the reverse connection to the airplay device.
   */
  public void close() {
    stopped = true;
    reverseConnectionThread.interrupt();
  }

  private final Thread reverseConnectionThread = new Thread(new Runnable() {

    @Override
    public void run() {
      final String request = "POST /reverse HTTP/1.1\r\n" +
          "Upgrade: PTTH/1.0\r\n" +
          "Connection: Upgrade\r\n" +
          "X-Apple-Purpose: event\r\n" +
          "Content-Length: 0\r\n" +
          "User-Agent: MediaControl/1.0\r\n" +
          "X-Apple-Session-ID: " + device.getSessionId() + "\r\n" +
          "\r\n";

      Socket socket = null;
      try {
        socket = new Socket(device.getAddress().getHostName(), device.getPort());

        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // send the reverse http request
        out.write(request);
        out.flush();

        // read the reverse http response
        String reverseResponse = readUntilEmptyLine(in);
        if (!reverseResponse.trim().startsWith("HTTP/1.1 101 Switching Protocols")) {
          throw new IOException("can't setup reverse connection");
        }

        while (!stopped) {
          String eventHeaders = readUntilEmptyLine(in);
          String eventData = readUntilEmptyLine(in);

          out.write("HTTP/1.1 200 OK\r\n" +
              "Content- Length: 0\r\n\" + " +
              "X-Apple-Session-ID: " + device.getSessionId() + "\r\n" +
              "\r\n");
          out.flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (socket != null) try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  });

}
