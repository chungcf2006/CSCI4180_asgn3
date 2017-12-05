import java.io.*;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.List;


public class S3 implements Backend {
    private static final String tmpFolder = "tmp/";
    AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

    public S3()
    {
        Bucket b = null;
        String bucket_name = "csci4180group12";
        if (s3.doesBucketExist(bucket_name)) {
            System.out.format("Bucket %s already exists.\n", bucket_name);
            b = getBucket(bucket_name);
        } else {
            try {
                b = s3.createBucket(bucket_name);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }


    }

    public boolean exists (String name) {
        try {
            S3Object o = s3.getObject("csci4180group12", name);
            S3ObjectInputStream s3is = o.getObjectContent();
            return true;
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
            //
            s3.putObject("csci4180group12", fileName, new File(fileName));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }



    public void joinChunks (String fileName, List<String> chunks) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);

        try {
            for (String hash : chunks) {
                S3Object o = s3.getObject("csci4180group12", tmpFolder+hash+".download");
                S3ObjectInputStream s3is = o.getObjectContent();
                byte[] read_buf = new byte[1024];
                int read_len = 0;

                RandomAccessFile infile = new RandomAccessFile(tmpFolder+hash+".download", "r");
                while ((read_len = s3is.read(read_buf)) > 0) {
                    fos.write(read_buf, 0, read_len);
                }

                s3is.close();
//                for (long i=0; i<infile.length(); i++) {
//                    fos.write(infile.read());
//                }
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

           // File source = new File(tmpFolder+filename);
            //
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