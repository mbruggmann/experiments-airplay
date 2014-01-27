package ch.mbruggmann.airplay.discovery;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

/**
 * Scans for airplay devices on the local network.
 */
public class Discovery implements Closeable {
  public static final String SERVICE_TYPE_AIRPLAY = "_airplay._tcp.local.";
  private final Set<Device> devices = Sets.newHashSet();
  private JmDNS jmdns;

  /**
   * Start scanning for airplay devices.
   */
  public void start() {
    try {
      JmDNS jmdns = JmDNS.create();
      jmdns.addServiceListener(SERVICE_TYPE_AIRPLAY, serviceListener);
      this.jmdns = jmdns;
    } catch (IOException e) {
      throw new RuntimeException("can't create jmdns", e);
    }
  }

  /**
   * Stop scanning for airplay devices.
   */
  public void close() {
    checkState(jmdns != null, "not started");
    try {
      jmdns.removeServiceListener(SERVICE_TYPE_AIRPLAY, serviceListener);
      jmdns.close();
    } catch (IOException e) {
      // ignore
    }
    jmdns = null;
  }

  /**
   * Get a snapshot of all currently known devices.
   * @return all known devices as of now.
   */
  public Set<Device> getDevices() {
    return ImmutableSet.copyOf(devices);
  }

  private final ServiceListener serviceListener = new ServiceListener() {
    @Override
    public void serviceAdded(ServiceEvent event) {
      ServiceInfo serviceInfo = event.getDNS().getServiceInfo(event.getType(), event.getName());
      Device device = Device.fromServiceInfo(serviceInfo);
      devices.add(device);
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
      ServiceInfo serviceInfo = jmdns.getServiceInfo(SERVICE_TYPE_AIRPLAY, event.getName());
      Device device = Device.fromServiceInfo(serviceInfo);
      devices.remove(device);
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
    }
  };

}
