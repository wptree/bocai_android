
package com.bocai.net;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;

public class FileCache
{
	private static final long CACHE_CHECK_INTERVAL = 120000L;
	public static final String CACHE_ROOT = "cache";
	private static final String LOG_TAG = "FileCache";
	private static final long MAX_CACHE_SIZE = 5242880L;
    static final int READ_BUFFER_SIZE = 3072;
	private static File cacheRootDir = null;
    private static long cacheSize = 65535L;
    private static FileCache instance = new FileCache();
    private static long lastCacheCheckTime = 65535L;
    private static LastModTimeComparator lastModTimeComp = new LastModTimeComparator();

    private static class LastModTimeComparator
        implements Comparator<File>
    {

        public int compare(File file, File file1)
        {
            long l = file.lastModified();
            long l1 = file1.lastModified();
            return (int)(l - l1);
        }

        private LastModTimeComparator()
        {
        }

    }

    private static class FileSizeFilter
        implements FileFilter
    {

        public boolean accept(File file)
        {
         
            if(file.isFile())
            {
                totalSize = totalSize + file.length();
            } else
            {
                file.listFiles(this);
            }
            return false;
        }

        long totalSize;

        private FileSizeFilter()
        {
            totalSize = 0L;
        }

    }


    private FileCache()
    {
    }

    public static String absolutePath(String fileName)
    {
        File file = cacheRootDir;
        return (new File(file, fileName)).getAbsolutePath();
    }

    public static long cacheSize()
    {
        return cacheSize;
    }

    public static void checkCache(boolean flag)
    {
        
        long l = System.currentTimeMillis();
        if(!flag)
        {
            long l1 = lastCacheCheckTime + CACHE_CHECK_INTERVAL;
            if(l < l1)
                return;
        }
        Thread thread = new Thread(cacheChecker);
        thread.start();
        if(!flag)
            return;
        try {
			thread.join();
		} catch (InterruptedException e) {
			Log.w(LOG_TAG, e.getMessage());
		}
        return;
    }

    public static boolean delete(File f)
    {
    	if (f.exists() && f.isDirectory()) {
    		if (f.listFiles().length == 0) {
    			f.delete();
    		} else {
    			File delFile[] = f.listFiles();
    			int i = f.listFiles().length;
    			for (int j = 0; j < i; j++) {
    				if (delFile[j].isDirectory()) {
    					delete(delFile[j]);
    				}
    				delFile[j].delete();
    			}
    		}
    	}
    	return true;
    }

    public static boolean fileExists(String fileName)
    {
        boolean flag;
        if(fileName == null)
        {
            flag = false;
        } else
        {
            File file = cacheRootDir;
            flag = (new File(file, fileName)).exists();
        }
        return flag;
    }

    public static File getFile(String fileName)
    {
    	String path,name;
    	File answer;
    	if(fileName == null){
    		return null;
    	}
    	
    	int index = fileName.lastIndexOf('/');
    	if(index != -1){
    		path = fileName.substring(0,index);
    		name = fileName.substring(index + 1);
    		if(path == null){
    			answer = new File(cacheRootDir,name);
    		}else{
    		  	File dir = new File(cacheRootDir,path);
    		  	if(!dir.exists()){
    		  		dir.mkdir();
    		  	}
    		  	answer = new File(dir,name);
    		}
    	}else{
    		answer = new File(cacheRootDir,fileName);
    	}
    	return answer;
    }

    public static FileCache getInstance()
    {
        return instance;
    }

    public static File getURL(String fileName)
    {
        return getFile(urlToFilePath(fileName));
    }

    public static File putFile(String fileName, InputStream inputStream, int length)
    {
     
        File file = getFile(fileName);;
        FileOutputStream fileOutputStream = null;
        if(!file.exists())
        {
            file.getParentFile().mkdirs();
            try {
				file.createNewFile();
			} catch (IOException e) {
				Log.w(LOG_TAG, e.getMessage());
			}
        }
        try {
			fileOutputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			Log.w(LOG_TAG,e.getMessage(),e);
		}
		
		int bufferSize = 3072; //3kb
		int byteRead = -1;
		
		byte data[] = new byte[bufferSize];
		try{
			do{
				byteRead = inputStream.read(data);
				if(byteRead == -1){
					break;
				}
				fileOutputStream.write(data, 0, byteRead);
			}while(true);
			inputStream.close();
			fileOutputStream.flush();
			fileOutputStream.close();
		}catch(Exception e){
			Log.w(LOG_TAG, e.getMessage(),e);
		}
		
        return file;
    
    }

    public static File putURL(String fileName, InputStream inputStream, int length)
    {
        return putFile(urlToFilePath(fileName), inputStream, length);
    }

    public static long sizeOfDirectory(File file)
    {
        FileSizeFilter fileSizeFilter = new FileSizeFilter();
        file.listFiles(fileSizeFilter);
        return fileSizeFilter.totalSize;
    }

    public static boolean urlExists(String url)
    {
        return fileExists(urlToFilePath(url));
    }

    public static String urlToFilePath(String url)
    {
        String filePath;
        if(url == null)
        {
            filePath = null;
        } else
        {
            int i = 0;
            if(url.startsWith("http://"))
                i = 7;
            int j = url.indexOf('/', i);
            if(j != -1)
                i = j;
            filePath = url.substring(i);
        }
        return filePath;
    }

    public void clear()
    {
      
        try
        {
            if(delete(cacheRootDir))
            {
                return;
            } else
            {
               Log.w("FileCache", "Error deleting cache root directory!");
                return;
            }
        }
        catch(Exception exception)
        {
            Log.e("FileCache", "Error clearing cache:", exception);
        }
    }

    public void finalize()
    {
        if(cacheRootDir != null)
        {
        	clear();
        }
        cacheRootDir = null; 
    }

    public void initialize(Context context)
    {
        if(cacheRootDir == null)
        {
            cacheRootDir = context.getFileStreamPath("cache");
            
            if(!cacheRootDir.exists())
                cacheRootDir.mkdirs();
        }
    }

 
    
 private static final Runnable cacheChecker = new Runnable() {
        PriorityQueue<File> agedFiles = null;

        private long visitFile(File paramFile, long paramLong)
        {
          if (paramFile.isDirectory())
          {
            File[] arrayOfFile = paramFile.listFiles();
            if(arrayOfFile.length == 0){
            	return paramLong;
            }
            for (int j = 0; j < arrayOfFile.length; j++)
            {
              File localFile = arrayOfFile[j];
              if (localFile.isFile())
              {
                this.agedFiles.add(localFile);
                long length = localFile.length();
                paramLong += length;
              } else {
            	  paramLong = visitFile(localFile, paramLong);
              }
            }
          }
          else
          {
            this.agedFiles.add(paramFile);
            long length = paramFile.length();
            paramLong += length;
          }
          return paramLong;
        }
        
        @Override
        public void run(){
        	
        	if(agedFiles == null){
        		agedFiles = new PriorityQueue<File>(100,FileCache.lastModTimeComp);
        	}
        	FileCache.setCacheSize(visitFile(FileCache.cacheRootDir,0L));
        	String rate = FileCache.cacheSize + " / " + MAX_CACHE_SIZE;
        	Log.i("FileCache", rate);
        	if(FileCache.cacheSize <= MAX_CACHE_SIZE) {
        		 FileCache.setLastCacheCheckTime(System.currentTimeMillis());
                 this.agedFiles.clear();
        	}
        	
        	while((this.agedFiles.size() > 0) && (FileCache.cacheSize > 2621440L)){
        		File file = agedFiles.remove();
        		long length = file.length();
        		if(file.delete()){
        			FileCache.subSize(length);
        		}else{
        			StringBuilder sb = new StringBuilder().append("Unable to delete cache file: ");
        			sb.append(file.getAbsolutePath());
        			Log.w(LOG_TAG, sb.toString());
        		}
        	}
        	
        	 long lengthAfterTrim = visitFile(FileCache.cacheRootDir, 0L);
        	 FileCache.setLastCacheCheckTime(System.currentTimeMillis());
             this.agedFiles.clear();
             rate = lengthAfterTrim + " / " + MAX_CACHE_SIZE;
             Log.i(LOG_TAG, rate);
        	
        }
      };
      
     static long setCacheSize(long size){
          cacheSize = size;
          return cacheSize;
     }

     static long subSize(long size){
    	  cacheSize = cacheSize - size;
          return cacheSize;
     }

     static long setLastCacheCheckTime(long time){
         lastCacheCheckTime = time;
         return lastCacheCheckTime;
     }
}
