package ch.mbruggmann.airplay.command;

import ch.mbruggmann.airplay.discovery.Device;
import com.github.kevinsawicki.http.HttpRequest;

public class PlayCommand extends AbstractCommand {

  public PlayCommand(Device device, String contentUrl) {
    this(device, contentUrl, 0.0);
  }

  public PlayCommand(Device device, String contentUrl, double position) {
    super(device, "/play", HttpRequest.METHOD_POST);
    setRequestBody(String.format("Content-Location: %s\nStart-Position: %.2f\n", contentUrl, position));
  }

}
