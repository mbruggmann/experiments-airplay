package ch.mbruggmann.airplay.command;

import ch.mbruggmann.airplay.discovery.Device;
import com.github.kevinsawicki.http.HttpRequest;

public class ErrorLogCommand extends AbstractCommand {

  public ErrorLogCommand(Device device) {
    super(device, "/getProperty?playbackErrorLog", HttpRequest.METHOD_GET);
  }

}
