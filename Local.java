import java.io.*;

import java.util.List;


public class Local implements Backend {
  private static final String root = "data/";

  public Local () {
    if (!this.exists(root)) {
      System.out.println("data Directory does not exist, trying to create...");
      if (this.mkdir(root))
        System.out.println("data Directory Created");
    }

    if (!this.exists(root+"MyDedup.index")) {
      System.out.println("index file does not exist, trying to create...");
      if (this.writeIndex("MyDedup.index", new Index()))
        System.out.println("index file Created");
    }
  }

  public boolean exists (String name) {
    return (new File(name)).exists();
  }

  public boolean mkdir (String dirName) throws SecurityException {
    return (new File(dirName)).mkdir();
  }

  public void write (String fileName, byte[] data) throws IOException {
    FileOutputStream fos = new FileOutputStream(root+fileName);
    fos.write(data);
    fos.close();
  }

  public void joinChunks (String fileName, List<String> chunks) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);

    for (String hash : chunks) {
      RandomAccessFile infile = new RandomAccessFile(root+hash, "r");
      for (long i=0; i<infile.length(); i++) {
        fos.write(infile.read());
      }
    }
    fos.close();
  }
  public void removeChunks (List<String> chunks) throws IOException {


  }


  public boolean writeIndex (String filename, Index index) {
    try {    
      FileOutputStream fs = new FileOutputStream(root+filename);    
      ObjectOutputStream os =  new ObjectOutputStream(fs);    
      os.writeObject(index);    
      os.close();
      return true;
    } catch(Exception ex) {    
      ex.printStackTrace();    
    }
    return false;
  }

  public Index readIndex (String filename) {
    try {    
      FileInputStream fis = new FileInputStream(root+filename);
      ObjectInputStream ois = new ObjectInputStream(fis);
      Index result = (Index) ois.readObject();
      ois.close();

      return result;
    } catch(Exception ex) {    
      ex.printStackTrace();    
    }
    return null;
  }
}