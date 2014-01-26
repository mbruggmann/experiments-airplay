package ch.mbruggmann.airplay.discovery;

import javax.jmdns.ServiceInfo;
import java.net.InetAddress;

/**
 * An airplay-capable device on the local network.
 */
public class Device {
  private final String name;
  private final InetAddress address;
  private final int port;

  static Device fromServiceInfo(ServiceInfo serviceInfo) {
    return new Device(serviceInfo.getName(), serviceInfo.getInet4Addresses()[0], serviceInfo.getPort());
  }

  Device(final String name, final InetAddress address, final int port) {
    this.name = name;
    this.address = address;
    this.port = port;
  }

  /**
   * Get the name of the device.
   * @return the device name.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the address for this device.
   * @return the device address.
   */
  public InetAddress getAddress() {
    return address;
  }

  /**
   * Get the port for this device.
   * @return the device port.
   */
  public int getPort() {
    return port;
  }

  /**
   * Get the HTTP endpoint for this device, e.g. "http://192.168.1.42:7000"
   * @return the http endpoint for this device.
   */
  public String getHttpEndpoint() {
    return String.format("http://%s:%d", getAddress().getHostAddress(), getPort());
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Device)
      return getHttpEndpoint().equals(((Device) other).getHttpEndpoint());
    return false;
  }

  @Override
  public String toString() {
    return String.format("Device{%s - %s}", getName(), getHttpEndpoint());
  }

}

