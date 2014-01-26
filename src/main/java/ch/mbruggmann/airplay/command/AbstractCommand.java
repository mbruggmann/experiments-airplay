package ch.mbruggmann.airplay.command;

import ch.mbruggmann.airplay.discovery.Device;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;

abstract class AbstractCommand implements Command {
  private final Device device;
  private final String endpoint;
  private final String method;

  private Optional<String> body = Optional.absent();

  public AbstractCommand(Device device, String endpoint, String method) {
    this.device = device;
    this.endpoint = endpoint;
    this.method = method;
  }

  public void setRequestBody(String body) {
    this.body = Optional.fromNullable(body);
  }

  public Reply doRequest() {
    final HttpRequest request = new HttpRequest(device.getHttpEndpoint() + endpoint, method);
    request.readTimeout(1000).connectTimeout(1000);
    request.userAgent("MediaControl/1.0");

    if (this.body.isPresent())
      request.send(this.body.get());

    return Reply.fromRequest(request);
  }
}
