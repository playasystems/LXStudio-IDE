/**
 * Copyright 2020- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.app;

import java.io.File;
import java.io.IOException;

import heronarts.lx.LX;
import heronarts.lx.LXPlugin;
import heronarts.lx.app.pattern.RGBStripesPattern;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.StripModel;
import heronarts.lx.studio.LXStudio;
import processing.core.PApplet;

/**
 * This is an example top-level class to build and run an LX Studio
 * application via an IDE. The main() method of this class can be
 * invoked with arguments to either run with a full Processing 3 UI
 * or as a headless command-line only engine.
 */
public class LXStudioApp extends PApplet implements LXPlugin {

  private static final String WINDOW_TITLE = "GigglePixel Example";

  private static int WIDTH = 1280;
  private static int HEIGHT = 800;
  private static boolean FULLSCREEN = false;

  private static int WINDOW_X = 0;
  private static int WINDOW_Y = 0;

  private static boolean HAS_WINDOW_POSITION = false;

  private GigglePixelListener gpListener;
  private GigglePixelBroadcaster gpBroadcaster;
  private GigglePixelOutput gpOutput;

  @Override
  public void settings() {
    if (FULLSCREEN) {
      fullScreen(PApplet.P3D);
    } else {
      size(WIDTH, HEIGHT, PApplet.P3D);
    }
    pixelDensity(displayDensity());
  }

  @Override
  public void setup() {
    LXStudio.Flags flags = new LXStudio.Flags(this);
    flags.resizable = true;
    flags.useGLPointCloud = false;
    flags.startMultiThreaded = true;

    LXModel model = new StripModel(25);

    new LXStudio(this, flags, model);
    this.surface.setTitle(WINDOW_TITLE);
    if (!FULLSCREEN && HAS_WINDOW_POSITION) {
      this.surface.setLocation(WINDOW_X, WINDOW_Y);
    }

  }

  @Override
  public void initialize(LX lx) {
    // Here is where you should register any custom components or make modifications
    // to the LX engine or hierarchy. This is also used in headless mode, so note that
    // you cannot assume you are working with an LXStudio class or that any UI will be
    // available.

    // Register custom pattern and effect types
    lx.registry.addPattern(RGBStripesPattern.class);
    lx.registry.addEffect(heronarts.lx.app.effect.AppEffect.class);

    String myGigglePixelName = "Aztec Spleen";
    int myGigglePixelID = 0; // Visit https://tinyurl.com/gpsources to be assigned one!

    String listenIp = "0.0.0.0";
    try {
      this.gpListener = new GigglePixelListener(lx, listenIp, myGigglePixelID);
      lx.engine.addLoopTask(this.gpListener);
      LX.log("GigglePixel listener created");
    } catch (IOException e) {
      LX.log("Failed to create GigglePixel listener: " + e.getMessage());
    }

    try {
      this.gpBroadcaster = new GigglePixelBroadcaster(
              lx, "127.0.0.1", myGigglePixelName, myGigglePixelID);
      lx.engine.addLoopTask(this.gpBroadcaster);
      LX.log("GigglePixel broadcaster created");
    } catch (IOException e) {
      LX.log("Failed to create GigglePixel broadcaster: " + e.getMessage());
    }

    this.gpOutput = new GigglePixelOutput(lx, this.gpBroadcaster);
    lx.addOutput(this.gpOutput);
  }

  public void initializeUI(LXStudio lx, LXStudio.UI ui) {
    // Here is where you may modify the initial settings of the UI before it is fully
    // built. Note that this will not be called in headless mode. Anything required
    // for headless mode should go in the raw initialize method above.
  }

  public void onUIReady(LXStudio lx, LXStudio.UI ui) {
    // At this point, the LX Studio application UI has been built. You may now add
    // additional views and components to the Ui hierarchy.
    GigglePixelUI gpui = new GigglePixelUI(ui, ui.leftPane.global.getContentWidth(),
            this.gpOutput, this.gpListener, this.gpBroadcaster);
    gpui.addToContainer(ui.leftPane.global);
  }

  @Override
  public void draw() {
    // All handled by core LX engine, do not modify, method exists only so that Processing
    // will run a draw-loop.
  }

  /**
   * Main interface into the program. Two modes are supported, if the --headless
   * flag is supplied then a raw CLI version of LX is used. If not, then we embed
   * in a Processing 4 applet and run as such.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    LX.log("Initializing LX version " + LXStudio.VERSION);
    boolean headless = false;
    File projectFile = null;
    for (int i = 0; i < args.length; ++i) {
      if ("--help".equals(args[i])) {
      } else if ("--headless".equals(args[i])) {
        headless = true;
      } else if ("--fullscreen".equals(args[i]) || "-f".equals(args[i])) {
        FULLSCREEN = true;
      } else if ("--width".equals(args[i]) || "-w".equals(args[i])) {
        try {
          WIDTH = Integer.parseInt(args[++i]);
        } catch (Exception x ) {
          LX.error("Width command-line argument must be followed by integer");
        }
      } else if ("--height".equals(args[i]) || "-h".equals(args[i])) {
        try {
          HEIGHT = Integer.parseInt(args[++i]);
        } catch (Exception x ) {
          LX.error("Height command-line argument must be followed by integer");
        }
      } else if ("--windowx".equals(args[i]) || "-x".equals(args[i])) {
        try {
          WINDOW_X = Integer.parseInt(args[++i]);
          HAS_WINDOW_POSITION = true;
        } catch (Exception x ) {
          LX.error("Window X command-line argument must be followed by integer");
        }
      } else if ("--windowy".equals(args[i]) || "-y".equals(args[i])) {
        try {
          WINDOW_Y = Integer.parseInt(args[++i]);
          HAS_WINDOW_POSITION = true;
        } catch (Exception x ) {
          LX.error("Window Y command-line argument must be followed by integer");
        }
      } else if (args[i].endsWith(".lxp")) {
        try {
          projectFile = new File(args[i]);
        } catch (Exception x) {
          LX.error(x, "Command-line project file path invalid: " + args[i]);
        }
      }
    }
    if (headless) {
      // We're not actually going to run this as a PApplet, but we need to explicitly
      // construct and set the initialize callback so that any custom components
      // will be run
      LX.Flags flags = new LX.Flags();
      flags.initialize = new LXStudioApp();
      if (projectFile == null) {
        LX.log("WARNING: No project filename was specified for headless mode!");
      }
      LX.headless(flags, projectFile);
    } else {
      PApplet.main("heronarts.lx.app.LXStudioApp", args);
    }
  }

}
