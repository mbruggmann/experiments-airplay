package ch.mbruggmann.airplay.command;

import ch.mbruggmann.airplay.discovery.Device;
import com.github.kevinsawicki.http.HttpRequest;

public class ServerInfoCommand extends AbstractCommand {

  public ServerInfoCommand(Device device) {
    super(device, "/server-info", HttpRequest.METHOD_GET);
  }

}
