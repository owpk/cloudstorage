import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.junit.Test;
import org.owpk.controller.TreeViewFiller;

import java.lang.reflect.Method;

public class RootCounter {
  @Test
  public void shouldCountRoots() throws NoSuchMethodException {
    TreeView<String> treeView = new TreeView<>();
    TreeItem<String> treeItemRoot = new TreeItem<>("Root");
    TreeItem<String> treeIA = new TreeItem<>("A");
    TreeItem<String> treeIB = new TreeItem<>("B");
    treeIA.getChildren().add(treeIB);
    treeItemRoot.getChildren().add(treeIA);
    treeView.setRoot(treeItemRoot);
//    Class<?> c = TreeViewFiller.class;
//    Method m = c.getDeclaredMethod("srootCounter", TreeItem.class);
//    Method m2 = c.getDeclaredMethod("rootCounter", TreeItem.class);
//    m.invoke()
  }
}
