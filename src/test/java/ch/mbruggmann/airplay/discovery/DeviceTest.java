package ch.mbruggmann.airplay.discovery;

import org.junit.Test;

import javax.jmdns.ServiceInfo;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceTest {

  @Test
  public void testFromServiceInfo() throws UnknownHostException {
    String name = "appletv";
    Inet4Address address = (Inet4Address) InetAddress.getByName("192.168.1.4");
    int port = 42;

    ServiceInfo serviceInfo = mock(ServiceInfo.class);
    when(serviceInfo.getName()).thenReturn(name);
    when(serviceInfo.getInet4Addresses()).thenReturn(new Inet4Address[]{address});
    when(serviceInfo.getPort()).thenReturn(port);

    Device device = Device.fromServiceInfo(serviceInfo);
    assertEquals(name, device.getName());
    assertEquals(address, device.getAddress());
    assertEquals(port, device.getPort());
  }

  @Test
  public void testHostname() throws UnknownHostException {
    InetAddress address = InetAddress.getByName("192.168.1.4");
    int port = 42;
    Device device = new Device("appletv", address, port);
    assertEquals("http://192.168.1.4:42", device.getHttpEndpoint());
  }

  @Test
  public void testEquals() throws UnknownHostException {
    Device device1 = new Device("appletv", InetAddress.getByName("192.168.1.4"), 42);
    Device device2 = new Device("appletv", InetAddress.getByName("192.168.1.4"), 42);
    Device device3 = new Device("appletv", InetAddress.getByName("192.168.1.5"), 42);
    Device device4 = new Device("appletv", InetAddress.getByName("192.168.1.4"), 43);

    assertTrue(device1.equals(device2));
    assertTrue(device2.equals(device1));
    assertFalse(device1.equals(device3));
    assertFalse(device1.equals(device4));
  }

}
