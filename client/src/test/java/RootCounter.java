import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.junit.Test;
import org.owpk.controller.TreeViewFiller;

import java.io.File;
import java.lang.reflect.Method;

public class RootCounter {
  @Test
  public void shouldCountRoots() throws NoSuchMethodException {

    File[] f = new File("C:\\Test\\1\\2\\3\\4 shouldn be").listFiles();
    System.out.println(f);
    System.out.println(f.length);
//    Class<?> c = TreeViewFiller.class;
//    Method m = c.getDeclaredMethod("srootCounter", TreeItem.class);
//    Method m2 = c.getDeclaredMethod("rootCounter", TreeItem.class);
//    m.invoke()
  }
}
