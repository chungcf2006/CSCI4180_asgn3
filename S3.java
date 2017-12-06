import java.io.*;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.List;


public class S3 implements Backend {
    private static final String tmpFolder = "tmp/";
    AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

//    static {
//        System.setProperty("https.proxyHost", "proxy.cse.cuhk.edu.hk");
//        System.setProperty("https.proxyPort", "8000");
//        System.setProperty("http.proxyHost", "proxy.cse.cuhk.edu.hk");
//        System.setProperty("http.proxyPort", "8000");
//    }
    public S3()
    {
        Bucket b = null;
        String bucket_name = "csci4180group12";
        if (s3.doesBucketExist(bucket_name)) {
            b = getBucket(bucket_name);
        } else {
            try {
                b = s3.createBucket(bucket_name);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }

        if (!(new File(tmpFolder)).exists()) {
            System.out.println("tmp folder does not exist, trying to create...");
            if ((new File(tmpFolder)).mkdir())
                System.out.println("tmp folder Created");
        }

        if (!this.exists("tmp/MyDedup.index")) {
            System.out.println("index file does not exist, trying to create...");
            if (this.writeIndex("MyDedup.index", new Index()))
                System.out.println("index file Created");
        }


    }

    public boolean exists (String name) {
        try {
            boolean exists = s3.doesObjectExist("csci4180group12", name);
            return exists;
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
            File tempFile = File.createTempFile("prefix", "suffix");
            //
            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data), tempFile);
            //s3.putObject("csci4180group12", fileName, new ByteArrayInputStream(data),new ObjectMetadata());
            s3.putObject("csci4180group12", fileName, tempFile);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }



    public void joinChunks (String fileName, List<String> chunks) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);

        try {
            for (String hash : chunks) {

                S3Object o = s3.getObject("csci4180group12", hash);
                S3ObjectInputStream s3is = o.getObjectContent();
                byte[] read_buf = new byte[1024];
                int read_len = 0;

                while ((read_len = s3is.read(read_buf)) > 0) {
                    fos.write(read_buf, 0, read_len);
                }

                s3is.close();
            }
            fos.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    public void removeChunks (List<String> chunks) throws IOException {
        try {
            for (String hash : chunks) {

                s3.deleteObject("csci4180group12", hash);
            }

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

           // File source = new File(tmpFolder+filename);
            s3.putObject("csci4180group12", tmpFolder+filename, new File(tmpFolder+filename));

            return true;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Index readIndex (String filename) {
        try {
            S3Object o = s3.getObject("csci4180group12", tmpFolder+filename);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(new File(tmpFolder+filename));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();

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

    public static Bucket getBucket(String bucket_name) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        Bucket named_bucket = null;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket_name)) {
                named_bucket = b;
            }
        }
        return named_bucket;
    }
}