package ch.mbruggmann.airplay;

import ch.mbruggmann.airplay.command.*;
import ch.mbruggmann.airplay.discovery.Device;
import ch.mbruggmann.airplay.discovery.Discovery;
import ch.mbruggmann.airplay.reverse.ReverseConnection;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Set;

public class Main {
  private static final int DISCOVERY_TIMEOUT_MS = 30*1000;

  public static void main(String... args) throws IOException, InterruptedException {
    Discovery discovery = new Discovery();
    discovery.start();

    // wait for at least one device to show up
    Set<Device> devices = Sets.newHashSet();
    long start = System.currentTimeMillis();
    while (devices.size() == 0 && System.currentTimeMillis() < start + DISCOVERY_TIMEOUT_MS) {
      devices = discovery.getDevices();
      Thread.sleep(1000);
    }

    // exit early if we can't find any devices
    discovery.close();
    if (devices.size() == 0) {
      System.out.println("no devices found");
      return;
    }

    // for simplicity, just pick the first available device
    final Device airplayServer = devices.iterator().next();
    System.out.println("connecting to " + airplayServer);

    Reply serverInfo = new ServerInfoCommand(airplayServer).doRequest();
    System.out.println(serverInfo.getBody());

    final ReverseConnection reverse = new ReverseConnection(airplayServer);
    reverse.start();

    // play the rush trailer
    final String contentUrl = "http://movietrailers.apple.com/movies/universal/rush/rush-tlr3_480p.mov?width=848&height=352";
    PlayCommand playCommand = new PlayCommand(airplayServer, contentUrl);
    Reply reply = playCommand.doRequest();
    System.out.println("play response: " + reply.getStatusCode());

    // monitor progress
    ProgressCommand.ProgressReply progressReply = new ProgressCommand(airplayServer).doRequest();
    boolean started = false;
    while (!started || progressReply.getRemaining() > 0) {
      try {
        progressReply = new ProgressCommand(airplayServer).doRequest();
        System.out.println(String.format("dur %f, pos %f", progressReply.getDuration(), progressReply.getPosition()));

        started = started || progressReply.getPosition() > 0;
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        Thread.sleep(1500);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // stop and exit
    new StopCommand(airplayServer).doRequest();
    reverse.close();
  }

}
