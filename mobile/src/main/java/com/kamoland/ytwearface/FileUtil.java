package com.kamoland.ytwearface;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static byte[] loadByteArray(InputStream in) throws FileNotFoundException, IOException {
        int size;
        byte[] w = new byte[1024];

        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            while (true) {
                size = in.read(w);
                if (size <= 0) break;
                out.write(w, 0, size);
            }
            out.flush();
            return out.toByteArray();

        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception ex) {}
            try {
                if (out != null) out.close();
            } catch (Exception ex) {}
        }
    }

    public static String loadTextFile(InputStream in) {
        try {
            byte[] b = loadByteArray(in);
            String contents = new String(b);

            return contents;

        } catch (FileNotFoundException ex) {
            return "";

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
