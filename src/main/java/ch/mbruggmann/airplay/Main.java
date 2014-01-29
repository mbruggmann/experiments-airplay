package ch.mbruggmann.airplay;

import ch.mbruggmann.airplay.discovery.Device;
import ch.mbruggmann.airplay.discovery.Discovery;
import ch.mbruggmann.airplay.player.HLSPlayer;
import ch.mbruggmann.airplay.player.HLSPlayerException;
import ch.mbruggmann.airplay.player.HLSPlayerState;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Set;

public class Main {
  private static final int DISCOVERY_TIMEOUT_MS = 30*1000;

  public static void main(String... args) throws IOException, InterruptedException, HLSPlayerException {
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

    final String contentUrl = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
    HLSPlayer player = new HLSPlayer(airplayServer);
    player.play(contentUrl);

    Thread.sleep(10000);

    final String newContentUrl = "http://movietrailers.apple.com/movies/universal/rush/rush-tlr3_480p.mov?width=848&height=352";
    player.play(newContentUrl);

    ImmutableSet<HLSPlayerState> stopStates = ImmutableSet.of(HLSPlayerState.STOPPED, HLSPlayerState.ERROR);
    while (! stopStates.contains(player.getState())) {
      Thread.sleep(1000);
    }

    System.out.println("player stopped in state " + player.getState());
    player.close();
  }

}
