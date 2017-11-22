import java.io.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class IndexReader {
  public static void main (String[] args) {
    try {
      FileInputStream fis = new FileInputStream(args[0]);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Index index = (Index) ois.readObject();
      ois.close();

      System.out.println(index);
      System.out.println(index.files.size());

      for (Map.Entry<String, List<String>> entry : index.files.entrySet()) {
        System.out.printf("Filename: %s\n", entry.getKey());
        for (String hash : entry.getValue()) {
          System.out.printf("\t%s\n", hash);
        }
        System.out.println();
      }


      for (Map.Entry<String,Integer> entry : index.chunks.entrySet()) {
        System.out.printf("%s: %d\n", entry.getKey(), entry.getValue());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
}