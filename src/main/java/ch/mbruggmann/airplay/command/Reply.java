package ch.mbruggmann.airplay.command;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;

public class Reply {
  private final int code;
  private final Optional<String> body;

  public static Reply fromRequest(HttpRequest request) {
    return new Reply(request.code(), Optional.fromNullable(request.body()));
  }

  public Reply(int code, Optional<String> body) {
    this.code = code;
    this.body = body;
  }

  public int getStatusCode() {
    return code;
  }

  public Optional<String> getBody() {
    return body;
  }
}
