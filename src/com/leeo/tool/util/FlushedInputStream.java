package com.leeo.tool.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * BitmapFactory类的decodeStream方法在网络超时或较慢的时候无法获取完整的数据，
 * 这里我们通过继承FilterInputStream类的skip方法来强制实现flush流中的数据，
 * 主要原理就是检查是否到文件末端，告诉http类是否继续。
 */
public class FlushedInputStream extends FilterInputStream { 
    public FlushedInputStream(InputStream inputStream) { 
        super(inputStream); 
    } 
 
    @Override 
    public long skip(long n) throws IOException { 
        long totalBytesSkipped = 0L; 
        while (totalBytesSkipped < n) { 
            long bytesSkipped = in.skip(n - totalBytesSkipped); 
            if (bytesSkipped == 0L) { 
                  int bytes = read(); 
                  if (bytes < 0) { 
                      break;  // we reached EOF 
                  } else { 
                      bytesSkipped = 1; // we read one byte 
                  } 
           } 
            totalBytesSkipped += bytesSkipped; 
        } 
        return totalBytesSkipped; 
    } 
}
