import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.owpk.message.DataInfo;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;
import org.owpk.util.ServerConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MessageTest {
  public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
    int buffSize = 8192;
    Socket socket = new Socket("localhost", ServerConfig.getPort());
    ObjectEncoderOutputStream out = new ObjectEncoderOutputStream(socket.getOutputStream());
    ObjectDecoderInputStream in = new ObjectDecoderInputStream(socket.getInputStream());
    System.out.println(in);
      try {
        out.writeObject(new Message<>(MessageType.DIR));
        out.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }


      System.out.println(in.readObject());

    byte[] buffer = new byte[buffSize];

    File f = new File("C:\\Test\\in\\1.txt");

    DataInfo[] bufferedData = FileUtility.getChunkedFile(f, MessageType.UPLOAD);
    System.out.println("+====================================================");
    for (int i = 0; i < bufferedData.length; i++) {
      System.out.println(Arrays.toString(bufferedData[i].getPayload()));
    }
    System.out.println("+====================================================");
    for (DataInfo data: bufferedData) {
      out.writeObject(data);
    }

    out.close();
    out.flush();
    in.close();
    socket.close();
  }
}
