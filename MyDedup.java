import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.lang.UnsupportedOperationException;
import java.util.List;
import java.util.ArrayList;
import java.io.RandomAccessFile;
import java.lang.StringBuilder;
import java.math.BigInteger;

public class MyDedup {

  public static Backend selectBackend (String backend) throws UnsupportedOperationException {
    if (backend.equals("local")) {
      return new Local();
    } else if (backend.equals("azure")) {
      return new Azure("DefaultEndpointsProtocol=https;AccountName=csci4180gp12;AccountKey=7fRSQz7pSFbzBSu/pP0WwDGy5ILLody8BcTVxAtVLwvCO9nOenRN5XFOGKWiB1T1P8b+pp4SCbSLYceYt2yfpA==;EndpointSuffix=core.windows.net");
    } else if (backend.equals("s3")) {
      return new S3();
    } else {
      throw new UnsupportedOperationException("Backend Type Not Supported");
    }
  }

  public static String hashToString (byte[] hash) {
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < hash.length; j++)
      sb.append(String.format("%02X", hash[j]));
    return sb.toString();
  }

  public static int modPow(int base, int exp, int mod) {
    if (mod == 1) return 0;
    int c = 1;
    for (int i = 1; i < exp+1; i++)
      c = (c*base) & (mod-1);
    return c;
  }

  private static int sizeWithDedup = 0;
  private static int sizeWithoutDedup = 0;

  public static void extractChunk(long start, long end, RandomAccessFile infile, Index index, String file_to_upload, Backend storage){
    if (end >= start) {
      System.out.printf("(%d, %d) Size: %d byte, %d KB\n", start, end, end-start+1, (end-start+1)/1024);
      try {
        byte[] data = new byte[(int)(end-start+1)];
        infile.seek(start);
        infile.read(data);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(data, 0, data.length);
        byte[] checksumBytes = md.digest();

        String hash = hashToString(checksumBytes);

        index.newChunk(file_to_upload, hash);

        // System.out.println(sb);
        sizeWithoutDedup += end-start+1;
        if (!index.chunkExist(hash)){
          storage.write(hash, data);
          sizeWithDedup += end-start+1;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    
  }

  public static void main(String args[]) {
    // Read Parameters
    if (args.length < 1){
      System.err.println("Usage: java MyDedup upload min_chunk avg_chunk max_chunk d file_to_upload <local|azure|s3>");
      System.err.println("       java MyDedup download file_to_download <local|azure|s3>");
      System.err.println("       java MyDedup delete file_to_delete <local|azure|s3>");
      System.exit(1);
    }

    String operation = args[0];

    if (operation.equals("upload")) {
      if (args.length != 7){
        System.err.println("Usage: java MyDedup upload min_chunk avg_chunk max_chunk d file_to_upload <local|azure|s3>");
        System.exit(1);
      }

      System.out.println("Operation: Upload");

      int min_chunk = Integer.parseInt(args[1]);
      int avg_chunk = Integer.parseInt(args[2]);
      int max_chunk = Integer.parseInt(args[3]);
      int d = Integer.parseInt(args[4]);
      String file_to_upload = args[5];
      String backend = args[6];

      System.out.println("*************************************");
      System.out.println("Parameter List:");
      System.out.printf("min_chunk: %d\n", min_chunk);
      System.out.printf("avg_chunk: %d\n", avg_chunk);
      System.out.printf("max_chunk: %d\n", max_chunk);
      System.out.printf("d: %d\n", d);
      System.out.printf("file_to_upload: %s\n", file_to_upload);
      System.out.printf("backend: %s\n", backend);
      System.out.println("*************************************");

      Backend storage;

      try {
        storage = selectBackend(backend);

        Index index = storage.readIndex("MyDedup.index");

        if (index.fileExist(file_to_upload)) {
          System.out.printf("%s exists, exiting...\n", file_to_upload);
        } else {
          RandomAccessFile infile = new RandomAccessFile(file_to_upload, "r");


          int prevRFP = 0;
          
          boolean reset = true;
          int window_size = min_chunk;
          byte[] window = new byte[max_chunk];
          long prevAnchor = 0;
          

          for (long s = 0; s < infile.length()-min_chunk+1; s++){
            int temp = 0;
            if (reset) {
              window_size = min_chunk;
              infile.seek(s);
              for (long i = s; i < s+min_chunk; i++) {
                temp += ((infile.readByte() & (avg_chunk-1)) * modPow(d, min_chunk-((int)(i-s))-1, avg_chunk)) & (avg_chunk-1);
              }
              reset = false;
            } else {
              infile.seek(s-1);
              byte start = infile.readByte();

              infile.seek(s+min_chunk-1);
              byte end = infile.readByte();

              temp = (prevRFP - (start * modPow(d,min_chunk-1,avg_chunk)) & (avg_chunk-1)) & (avg_chunk-1);
              if (temp < 0)
                temp += avg_chunk;
              temp = ((temp * (d & (avg_chunk-1))) & (avg_chunk-1)) + (end & (avg_chunk-1));
            }

            temp = temp & (avg_chunk-1);

            prevRFP = temp;
            if ((temp == 0) || window_size == max_chunk){
              extractChunk(prevAnchor, s+min_chunk-1, infile, index, file_to_upload, storage);
              s = s+min_chunk-1;
              prevAnchor = s+1;
              reset = true;
            } else {
              window_size++;
            }
          }
          extractChunk(prevAnchor, infile.length()-1, infile, index, file_to_upload, storage);

          infile.close();

          storage.writeIndex("MyDedup.index", index);

          System.out.printf("Total number of chunks in storage: %d\n", index.numChunks());
          System.out.printf("Number of unique chunks in storage: %d\n", index.numUniqueChunks());
          System.out.printf("Number of bytes in storage with deduplication: %d\n", sizeWithDedup);
          System.out.printf("Number of bytes in storage without deduplication: %d\n", sizeWithoutDedup);
          System.out.printf("Space saving: %.4f\n", 1-(sizeWithDedup/(double)sizeWithoutDedup));
        }
            
      } catch (Exception e) {
        e.printStackTrace();
      }


    } else if (operation.equals("download")) {
      if (args.length != 3){
        System.err.println("Usage: java MyDedup download file_to_download <local|azure|s3>");
        System.exit(1);
      }

      System.out.println("Operation: Download");

      String file_to_download = args[1];
      String backend = args[2];

      System.out.println("*************************************");
      System.out.println("Parameter List:");
      System.out.printf("file_to_download: %s\n", file_to_download);
      System.out.printf("backend: %s\n", backend);
      System.out.println("*************************************");

      try {
        Backend storage = selectBackend(backend); 
        Index index = storage.readIndex("MyDedup.index");
        if (!index.fileExist(file_to_download)) {
          System.out.printf("%s does not exist, exiting...\n", file_to_download);
        } else {
          storage.joinChunks("download/"+file_to_download, index.getChunkListByFile(file_to_download));
          System.out.printf("File %s downloaded.\n", file_to_download);
        }
        
      } catch (Exception e) {
        e.printStackTrace();
      }

    } else if (operation.equals("delete")) {
      if (args.length != 3){
        System.err.println("Usage: java MyDedup delete file_to_delete <local|azure|s3>");
        System.exit(1);
      }

      System.out.println("Operation: Delete");

      String file_to_delete = args[1];
      String backend = args[2];

      System.out.println("*************************************");
      System.out.println("Parameter List:");
      System.out.printf("file_to_delete: %s\n", file_to_delete);
      System.out.printf("backend: %s\n", backend);
      System.out.println("*************************************");


    } else {
      System.err.println("Usage: java MyDedup upload min_chunk avg_chunk max_chunk d file_to_upload <local|azure|s3>");
      System.err.println("       java MyDedup download file_to_download <local|azure|s3>");
      System.err.println("       java MyDedup delete file_to_delete <local|azure|s3>");
      System.exit(1);
    }
  }
}