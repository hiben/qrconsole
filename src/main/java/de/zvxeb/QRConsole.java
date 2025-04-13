package de.zvxeb;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import io.nayuki.qrcodegen.QrCode;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class QRConsole {

  public static void main(String...args) throws InterruptedException, InvocationTargetException {
    Options options = new Options();
    var jcommander = JCommander.newBuilder().addObject(options).programName("QRConsole").build();
    jcommander.parse(args);
    if(options.help) {
      jcommander.usage();
      return;
    }
    final boolean invert = options.invert;
    final int pad = options.pad;
    String data = options.data;
    if(options.fromClipboard) {
      var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      try {
        Object clipboardData = clipboard.getData(DataFlavor.stringFlavor);
        if(clipboardData!=null) {
          data = String.valueOf(clipboardData);
          System.out.format("Encoding from clipboard: '%s'\n", data);
        }
      } catch (IOException e) {
      } catch (UnsupportedFlavorException e) {
      }
    }
    if(data == null || data.length()==0) {
      System.err.println("No data to encode...");
      return;
    }
    QrCode qrCode = QrCode.encodeText(data, QrCode.Ecc.LOW);
    StringBuilder sb = new StringBuilder();
    int size = qrCode.size + 2 * pad;
    for (int line = 0; line < size; line++) {
      for(int column = 0; column < size; column++) {
        boolean isPad = line < pad || line > (size - pad) || column < pad || column > (size - pad);
        boolean active = isPad ? false : qrCode.getModule(column-pad, line-pad);
        sb.append((active ^ invert) ? "  " : "██");
      }
      sb.append("\n");
    }
    System.out.print(sb);
    if(options.toClipboard) {
      StringSelection stringSelection = new StringSelection(sb.toString());
      // set clipboard on AWT thread to avoid exiting too early
      SwingUtilities.invokeAndWait(() ->
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null)
      );
    }
  }

  public static class Options {
    @Parameter(names = {"-h", "--help"}, help = true, description = "show help")
    private boolean help;
    @Parameter(names = {"-i", "--invert"}, description = "invert output")
    private boolean invert = false;
    @Parameter(names = {"-p", "--pad"}, validateWith = PositiveInteger.class, description = "padding")
    private int pad = 4;
    @Parameter(names = {"-t", "--to-clipboard"}, description = "send output to clipboard")
    private boolean toClipboard = false;
    @Parameter(names = {"-c", "--clipboard"}, description = "get input from clipboard")
    private boolean fromClipboard = false;
    @Parameter(description = "<data to encode>")
    private String data;
  }

}
