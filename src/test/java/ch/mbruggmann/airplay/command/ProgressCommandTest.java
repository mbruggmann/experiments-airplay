package ch.mbruggmann.airplay.command;

import com.google.common.base.Optional;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class ProgressCommandTest {

  @Test
  public void testProgressReply() {
    double duration = 7.86;
    double position = 4.21;
    String body = String.format("duration: %f\nposition:%f", duration, position);
    ProgressCommand.ProgressReply reply = new ProgressCommand.ProgressReply(200, Optional.of(body));
    assertEquals(duration, reply.getDuration());
    assertEquals(position, reply.getPosition());
  }

}
