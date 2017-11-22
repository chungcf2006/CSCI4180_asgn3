import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Index implements Serializable {
  public Map<String,List<String>> files;
  public Map<String,Integer> chunks;
  private static final long serialVersionUID = 3834356246540151321l;

  public Index () {
    this.files = new HashMap<String,List<String>>();
    this.chunks = new HashMap<String,Integer>();
  }

  public void newChunk (String filename, String hash) {
    filename = filename.substring(filename.lastIndexOf("/")>0?filename.lastIndexOf("/")+1:0);

    if (!this.files.containsKey(filename)){
      this.files.put(filename, new ArrayList<String>());
      // System.out.printf("Created Chunk List for %s\n", filename);
    }
    this.files.get(filename).add(hash);
    // System.out.printf("Added hash %s for %s\n", hash, filename);

    if (!this.chunks.containsKey(hash)){
      this.chunks.put(hash, 0);
    }
    this.chunks.put(hash, this.chunks.get(hash) + 1);
    // System.out.printf("Added counter for hash %s, current value: %d\n", hash, this.chunks.get(hash));

  }

  public boolean chunkExist (String hash) {
    return chunks.get(hash) > 1;
  }

  public boolean fileExist (String filename) {
    filename = filename.substring(filename.lastIndexOf("/")>0?filename.lastIndexOf("/")+1:0);
    return files.containsKey(filename);
  }

  public List<String> getChunkListByFile (String filename) {
    filename = filename.substring(filename.lastIndexOf("/")>0?filename.lastIndexOf("/")+1:0);
    if (this.files.containsKey(filename))
      return this.files.get(filename);
    else
      return null;
  }

  public int numChunks () {
    int count = 0;
    for (Map.Entry<String,Integer> chunk : chunks.entrySet()) {
      count += chunk.getValue();
    }
    return count;
  }

  public int numUniqueChunks () {
    return chunks.size();
  }
}