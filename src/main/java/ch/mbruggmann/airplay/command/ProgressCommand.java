package ch.mbruggmann.airplay.command;

import ch.mbruggmann.airplay.discovery.Device;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProgressCommand extends AbstractCommand {

  public ProgressCommand(Device device) {
    super(device, "/scrub", HttpRequest.METHOD_GET);
  }

  @Override
  public ProgressReply doRequest() {
    Reply reply = super.doRequest();
    return new ProgressReply(reply.getStatusCode(), reply.getBody());
  }

  public static class ProgressReply extends Reply {
    private static final Pattern FLOAT_PATTERN = Pattern.compile("([+-]?\\d*\\.\\d+)(?![-+0-9\\.])");
    private final double duration;
    private final double position;

    public ProgressReply(int code, Optional<String> body) {
      super(code, body);

      if (body.isPresent()) {
        Matcher m = FLOAT_PATTERN.matcher(body.get());
        duration = m.find() ? Double.valueOf(m.group()) : -1;
        position = m.find() ? Double.valueOf(m.group()) : -1;
      } else {
        duration = -1;
        position = -1;
      }
    }

    public double getDuration() {
      return duration;
    }

    public double getPosition() {
      return position;
    }

    public double getRemaining() {
      return getDuration() - getPosition();
    }
  }

}
