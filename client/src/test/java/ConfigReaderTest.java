import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.owpk.app.Config;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


public class ConfigReaderTest {
  private static String source;
  private static String port;
  private static String connect_on_startup;
  private static Properties properties;

  @BeforeClass
  public static void readProperty() {
    properties = new Properties();
    try (InputStream in =
             Config.class.getClassLoader()
                 .getResourceAsStream("client.properties")) {
      properties.load(in);
      source = properties.getProperty("root_directory");
      System.out.println(source);
      port = properties.getProperty("port");
      System.out.println(port);
      connect_on_startup = properties.getProperty("connect_on_startup");
      System.out.println(connect_on_startup);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    showDisk();
  }

  public static void showDisk() throws IOException {
    File[] drives = File.listRoots();
    if (drives != null && drives.length > 0) {
      for (File aDrive : drives) {
        System.out.println(aDrive);
      }
    }
  }

  @Test
  public void shouldSwitchConnectOnStartupArgument() {
    String cos = properties.getProperty("connect_on_startup");
    System.out.println("------------");
    System.out.println(cos + " <- before prop");
    String arg = "";
    if (cos.equals("true"))
      arg = "false";
    else if (cos.equals("false"))
      arg = "true";
    properties.setProperty("connect_on_startup", arg);
    System.out.println(properties.getProperty("connect_on_startup") + " <- new prop");
    Assert.assertEquals(arg, properties.getProperty("connect_on_startup"));
  }
  @Test
  public void getDiskName() {

  }
}
