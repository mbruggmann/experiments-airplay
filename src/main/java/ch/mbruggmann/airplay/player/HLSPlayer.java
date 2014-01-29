package ch.mbruggmann.airplay.player;

import ch.mbruggmann.airplay.command.*;
import ch.mbruggmann.airplay.discovery.Device;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HLSPlayer implements AutoCloseable {
  public static final long PROGRESS_UPDATE_DELAY = 1500;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final Device device;

  private ReverseConnection reverseConnection;
  private ScheduledFuture<?> progressUpdateFuture;
  private HLSPlayerState state = HLSPlayerState.CONNECTING;
  private double duration = 0;
  private double position = 0;

  public HLSPlayer(Device device) {
    this.device = device;
    this.reverseConnection = new ReverseConnection(device, this);
  }

  public void connect() throws HLSPlayerException {
    Reply serverInfo = new ServerInfoCommand(device).doRequest();
    if (serverInfo.getStatusCode() != 200) {
      setState(HLSPlayerState.ERROR);
      throw new HLSPlayerException("can't fetch server info");
    }
    reverseConnection.start();
  }

  public void play(String contentUrl) throws HLSPlayerException {
    // if we are already playing (or paused), stop
    if (getState() == HLSPlayerState.LOADING || getState() == HLSPlayerState.PLAYING || getState() == HLSPlayerState.PAUSED) {
      stop();
    }

    // if we haven't connected yet, do that now
    if (getState() == HLSPlayerState.CONNECTING) {
      connect();
    }

    // make sure we are not in error state
    if (getState() == HLSPlayerState.ERROR) {
      throw new HLSPlayerException("can't start playback from error state.");
    }

    setState(HLSPlayerState.LOADING);
    PlayCommand playCommand = new PlayCommand(device, contentUrl);
    Reply reply = playCommand.doRequest();
    if (reply.getStatusCode() != 200) {
      setState(HLSPlayerState.ERROR);
      throw new HLSPlayerException("can't start playback, status code " + reply.getStatusCode());
    }
    progressUpdateFuture = executor.scheduleAtFixedRate(progressUpdater, 0, PROGRESS_UPDATE_DELAY, TimeUnit.MILLISECONDS);
  }

  public void stop() throws HLSPlayerException {
    Reply reply = new StopCommand(device).doRequest();
    if (reply.getStatusCode() != 200) {
      setState(HLSPlayerState.ERROR);
      throw new HLSPlayerException("can't stop playback, status code " + reply.getStatusCode());
    }
    setState(HLSPlayerState.STOPPED);
  }

  public HLSPlayerState getState() {
    return state;
  }

  public double getDuration() {
    return duration;
  }

  public double getPosition() {
    return position;
  }

  @Override
  public void close() {
    if (progressUpdateFuture != null) {
      progressUpdateFuture.cancel(true);
      progressUpdateFuture = null;
    }
    reverseConnection.close();
    executor.shutdown();
  }

  void setState(HLSPlayerState state) {
    if (state != this.state) {
      System.out.println("new player state " + state);
      this.state = state;
    }
  }

  public final Runnable progressUpdater = new Runnable() {
    public static final int ZERO_DURATION_RUNS_FOR_ERROR = 3;
    private int zeroDurationRuns = 0;

    @Override
    public void run() {
      try {
        ProgressCommand.ProgressReply progressReply = new ProgressCommand(device).doRequest();
        if (progressReply.getStatusCode() != 200) {
          return;
        }

        duration = progressReply.getDuration();
        position = progressReply.getPosition();

        //HACK detect some error conditions that are not reported through the reverse connection
        if (duration == 0 && getState() == HLSPlayerState.PLAYING) {
          zeroDurationRuns++;
          if (zeroDurationRuns >= ZERO_DURATION_RUNS_FOR_ERROR) {
            zeroDurationRuns = 0;
            setState(HLSPlayerState.ERROR);
          }
        }
      } catch (Exception e) {
        //ignored
      }
    }
  };


}
