import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class WritePropertyTest {

  @Test
  public void shouldWritePropertyToFile() throws FileNotFoundException, IOException {
    File file = null;
    Properties props = new Properties();
    props.setProperty("Hello", "World");
    URL url = Thread.currentThread().getContextClassLoader()
        .getResource("test.properties");
    try {
      file = new File(url.toURI().getPath());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    props.store(new FileOutputStream(file, true), "OMG, It werks!");
    props.load(new FileInputStream(file));
    Assert.assertEquals("World", props.getProperty("Hello"));
  }

}
