package ch.mbruggmann.airplay.command;

import ch.mbruggmann.airplay.discovery.Device;
import com.github.kevinsawicki.http.HttpRequest;

public class StopCommand extends AbstractCommand {

  public StopCommand(Device device) {
    super(device, "/stop", HttpRequest.METHOD_POST);
  }

}
