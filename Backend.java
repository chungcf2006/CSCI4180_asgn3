import java.io.IOException;
import java.util.List;

public interface Backend {
  public boolean exists (String name);
  public boolean mkdir (String dirName) throws SecurityException;
  public void write (String fileName, byte[] data) throws IOException;
  public void joinChunks (String fileName, List<String> chunks) throws IOException;
  public boolean writeIndex (String filename, Index index);
  public Index readIndex (String filename);
}