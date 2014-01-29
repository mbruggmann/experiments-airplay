package ch.mbruggmann.airplay.player;

import ch.mbruggmann.airplay.discovery.Device;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * A reverse TCP connection to receive events from an airplay device.
 */
public class ReverseConnection implements Closeable {
  private final Device device;
  private final HLSPlayer player;
  boolean stopped = false;

  public ReverseConnection(Device device, HLSPlayer player) {
    this.device = device;
    this.player = player;
  }

  /**
   * Set up a reverse connection to the airplay device.
   */
  public void start() {
    reverseConnectionThread.start();
  }

  /**
   * Stop the reverse connection to the airplay device.
   */
  public void close() {
    stopped = true;
    reverseConnectionThread.interrupt();
  }

  /**
   * Interpret an event coming in from the airplay device.
   * @param headers the event headers.
   * @param body the event body.
   */
  protected void handleReverseEvent(String headers, String body) {
    if (body.contains(">loading<"))
      player.setState(HLSPlayerState.LOADING);
    else if (body.contains(">playing<"))
      player.setState(HLSPlayerState.PLAYING);
    else if (body.contains(">paused<"))
      player.setState(HLSPlayerState.PAUSED);
    else if (body.contains(">itemPlayedToEnd<"))
      player.setState(HLSPlayerState.STOPPED);
    else if (body.contains(">stopped<"))
      player.setState(HLSPlayerState.STOPPED);
    else if (body.contains(">accessLogChanged<")) {
      // ignored
    } else {
      System.out.println("unhandled reverse event");
      System.out.println(headers);
      System.out.println(body);
    }
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
        String reverseResponse = readNextSegment(in);
        if (!reverseResponse.trim().startsWith("HTTP/1.1 101 Switching Protocols")) {
          throw new IOException("can't setup reverse connection");
        }

        while (!stopped) {
          String eventHeaders = readNextSegment(in);
          String eventData = readNextSegment(in);

          handleReverseEvent(eventHeaders, eventData);

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

    private String readNextSegment(final BufferedReader reader) throws IOException {
      String line = reader.readLine();
      List<String> result = Lists.newArrayList(line);
      while (line != null && !line.trim().isEmpty() && !"</plist>".equals(line.trim())) {
        line = reader.readLine();
        result.add(line);
      }
      return Joiner.on('\n').join(result);
    }

  });

}
