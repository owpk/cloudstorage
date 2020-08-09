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
    Path source = Paths.get("C:\\Test\\1");
    Path target = Paths.get("C:\\Test\\2\\folder");
      new Thread(() -> {
        try {
          move(source, target);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }).start();
  }

  private static void move(Path sourceFile, Path destFile) throws IOException {
    if (Files.isDirectory(sourceFile)) {
      String targetDirName = sourceFile.getFileName().toString();
      Files.createDirectory(Paths.get(destFile.toString(), targetDirName));
      File[] files = sourceFile.toFile().listFiles();
      if (files != null && files.length != 0) {
        for (File f : files)
          move(f.toPath(), Paths.get(destFile.toString(), targetDirName));
      }
        Files.deleteIfExists(sourceFile);
      } else
        Files.move(sourceFile, Paths.get(destFile.toString(), sourceFile.getFileName().toString()), REPLACE_EXISTING);
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
