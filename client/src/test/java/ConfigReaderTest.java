import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.owpk.app.ClientConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class ConfigReaderTest {
  private static String source;
  private static String port;
  private static String connect_on_startup;
  private static Properties properties;

  @BeforeClass
  public static void readProperty() {
    properties = new Properties();
    try (InputStream in =
             ClientConfig.class.getClassLoader()
                 .getResourceAsStream("test.properties")) {
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
}
