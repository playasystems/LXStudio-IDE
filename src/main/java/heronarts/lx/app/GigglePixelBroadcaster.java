package heronarts.lx.app;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.color.LXColor;
import playasystems.gigglepixel.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static java.lang.Math.min;

public class GigglePixelBroadcaster implements LXLoopTask {
  public static final int BROADCAST_PERIOD_MSEC = 5000;

  private final LX lx;
  private final GPBroadcaster gp;
  private final String name;
  private List<Integer> colors;
  private double msecSinceLastBroadcast;
  public boolean enabled;

  public GigglePixelBroadcaster(LX lx, String destIP, String myName, int myID) throws IOException {
    this.lx = lx;
    InetAddress destAddr = InetAddress.getByName(destIP);
    this.gp = new GPBroadcaster(destAddr);
    this.gp.sourceOverride = myID;
    this.name = myName;
    this.msecSinceLastBroadcast = 0.0;
    this.enabled = false;
  }

  public void setColors(List<Integer> colors) {
    this.colors = colors;
  }

  public void sendImmediately() {
    this.msecSinceLastBroadcast = BROADCAST_PERIOD_MSEC;
  }

  @Override
  public void loop(double deltaMs) {
    this.msecSinceLastBroadcast += deltaMs;
    if (!this.enabled) return;

    if (this.msecSinceLastBroadcast < BROADCAST_PERIOD_MSEC) return;
    this.msecSinceLastBroadcast = 0.0;

    GPIdentificationPacket idPacket = new GPIdentificationPacket(this.name);
    try {
      this.gp.send(idPacket);
    } catch (IOException e) {
      LX.log("Error broadcasting GigglePixel ID: " + e.getMessage());
      return;
    }

    int numColors = this.colors.size();
    if (numColors < 1) return;
    List<GPColor> entries = new ArrayList<>();
    for (Integer color : this.colors) {
      int r = (256 + LXColor.red(color)) % 256;
      int g = (256 + LXColor.green(color)) % 256;
      int b = (256 + LXColor.blue(color)) % 256;
      int frac = min(255, 256 / numColors);
      entries.add(new GPColor(r,g,b,frac));
    }
    GPPalettePacket palettePacket = new GPPalettePacket(entries);
    try {
      this.gp.send(palettePacket);
    } catch (IOException e) {
      LX.log("Error broadcasting GigglePixel palette: " + e.getMessage());
    }
  }
}
