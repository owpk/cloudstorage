import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.owpk.app.Config;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
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
    Path source = Paths.get("C:\\Test\\1");
    Path target = Paths.get("C:\\Test\\2");
      new Thread(() -> {
        try {
          move(source, target);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }).start();
  }

  private static void move(File sourceFile, File destFile) throws IOException {
    if (sourceFile.isDirectory()) {
      String targetDirName = sourceFile.getName();
      Files.createDirectory(Paths.get(destFile.getPath(), targetDirName));
      File[] files = sourceFile.listFiles();
      if (files == null || files.length == 0) {
        Files.delete(sourceFile.toPath());
      } else {
        for (File f : files)
        move(f, Paths.get(destFile.getAbsolutePath(), targetDirName).toFile());
      }
    } else Files.move(sourceFile.toPath(),Paths.get(destFile.getAbsolutePath(), sourceFile.getName()), REPLACE_EXISTING);
  }

  private static void move(Path sourceFile, Path destFile) throws IOException {
    if (Files.isDirectory(sourceFile)) {
      String targetDirName = sourceFile.getFileName().toString();
      Files.createDirectory(Paths.get(destFile.toString(), targetDirName));
      File[] files = sourceFile.toFile().listFiles();
      if (files == null || files.length == 0) {
        Files.delete(sourceFile);
      } else {
        for (File f : files)
          move(f.toPath(), Paths.get(destFile.toString(), targetDirName));
      }
    } else Files.move(sourceFile,Paths.get(destFile.toString(), sourceFile.getFileName().toString()), REPLACE_EXISTING);
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
