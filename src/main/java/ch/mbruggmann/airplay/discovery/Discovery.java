package ch.mbruggmann.airplay.discovery;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkState;

/**
 * Scans for airplay devices on the local network.
 */
public class Discovery implements Closeable {
  public static final String SERVICE_TYPE_AIRPLAY = "_airplay._tcp.local.";
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Set<Device> devices = Sets.newHashSet();
  private JmDNS jmdns;
  private boolean stopping = false;

  /**
   * Start scanning for airplay devices.
   */
  public void start() {
    try {
      this.jmdns = JmDNS.create();
    } catch (IOException e) {
      throw new RuntimeException("can't create jmdns", e);
    }
    stopping = false;
    executor.submit(periodicUpdater);
  }

  /**
   * Stop scanning for airplay devices.
   */
  public void close() {
    checkState(jmdns != null, "not started");
    try {
      jmdns.close();
    } catch (IOException e) {
      // ignore
    }
    stopping = true;
    executor.shutdownNow();
    jmdns = null;
  }

  /**
   * Get a snapshot of all currently known devices.
   * @return all known devices as of now.
   */
  public synchronized Set<Device> getDevices() {
    return ImmutableSet.copyOf(devices);
  }

  private synchronized void addDeviceForServiceInfo(ServiceInfo info) {
    Device device = Device.fromServiceInfo(info);
    devices.add(device);
  }

  //TODO implement this
  private synchronized void removeDeviceForServiceInfo(ServiceInfo info) {
    Device device = Device.fromServiceInfo(info);
    devices.remove(device);
  }

  private final Runnable periodicUpdater = new Runnable() {
    private final long[] SCAN_TIMES = new long[]{
        4000, 6000, 8000, 16000, 8000, 6000, 4000
    };
    private int currentScanTime = 0;

    @Override
    public void run() {
      while (!stopping) {
        long scanTime = SCAN_TIMES[currentScanTime++ % SCAN_TIMES.length];
        ServiceInfo[] services = jmdns.list(SERVICE_TYPE_AIRPLAY, scanTime);
        for (ServiceInfo service: services) {
          addDeviceForServiceInfo(service);
        }
      }
    }
  };

}
