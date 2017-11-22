import java.io.*;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;

import java.util.List;


public class Azure implements Backend {
  private static final String tmpFolder = "tmp/";
  private String storageConnectionString;
  private CloudBlobContainer container;

  public Azure (String connectionString) {
    storageConnectionString = connectionString;
    try {
      CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
      CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
      container = blobClient.getContainerReference("data");
      container.createIfNotExists();

      if (!(new File(tmpFolder)).exists()) {
      System.out.println("tmp folder does not exist, trying to create...");
        if ((new File(tmpFolder)).mkdir())
          System.out.println("tmp folder Created");
      }

      if (!this.exists("MyDedup.index")) {
        System.out.println("index file does not exist, trying to create...");
        if (this.writeIndex("MyDedup.index", new Index()))
          System.out.println("index file Created");
      }

      
    } catch(Exception ex) {    
      ex.printStackTrace();    
    }
    
  }

  private CloudBlockBlob getBlob(String filename) {
    try {
      return this.container.getBlockBlobReference(filename);
    } catch(Exception ex) {    
      ex.printStackTrace(); 
      return null;   
    }
  }

  public boolean exists (String name) {
    try {
      return this.container.getBlockBlobReference(name).exists();
    } catch(Exception ex) {    
      ex.printStackTrace(); 
      return false;   
    }
  }

  public boolean mkdir (String dirName) throws SecurityException {
    return (new File(dirName)).mkdir();
  }

  public void write (String fileName, byte[] data) throws IOException {
    try {
      System.out.printf("Writing: %s\n", fileName);
      getBlob(fileName).upload(new ByteArrayInputStream(data), data.length);
    } catch(Exception ex) {    
      ex.printStackTrace();
    }
  }

  

  public void joinChunks (String fileName, List<String> chunks) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);

    try {
      for (String hash : chunks) {

        getBlob(hash).download(new FileOutputStream(tmpFolder+hash+".download"));

        RandomAccessFile infile = new RandomAccessFile(tmpFolder+hash+".download", "r");
        for (long i=0; i<infile.length(); i++) {
          fos.write(infile.read());
        }
      }
      fos.close();
    } catch(Exception ex) {    
      ex.printStackTrace();
    }
  }

  public boolean writeIndex (String filename, Index index) {
    try {
      FileOutputStream fs = new FileOutputStream(tmpFolder+filename);    
      ObjectOutputStream os =  new ObjectOutputStream(fs);    
      os.writeObject(index);    
      os.close();

      File source = new File(tmpFolder+filename);
      getBlob(filename).upload(new FileInputStream(source), source.length());

      return true;
    } catch(Exception ex) {    
      ex.printStackTrace();    
    }
    return false;
  }

  public Index readIndex (String filename) {
    try {    
      getBlob(filename).download(new FileOutputStream(tmpFolder+filename));
      FileInputStream fis = new FileInputStream(tmpFolder+filename);
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