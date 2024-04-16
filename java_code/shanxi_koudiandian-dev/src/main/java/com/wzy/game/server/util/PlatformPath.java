package com.wzy.game.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlatformPath {
    private static Logger logger = LoggerFactory.getLogger(PlatformPath.class);

    //默认的平台后缀
    private static final String DEFAULT_SUFFIX_DYNAMICLIBRAY = "";
    private static final String WIN_SUFFIX_DYNAMICLIBRAY = ".dll";
    private static final String LINUX_SUFFIX_DYNAMICLIBRAY = ".so";
    private static final String MAC_0S_SUFFIX_DYNAMICLIBRAY = ".dylib";
    private static final String MAC_OX_X_SUFFIX_DYNAMICLIBRAY = ".dylib";

    //默认的平台自定义目录
    private static String DEFAULTDIR_WIN = "C:\\worksht\\coding\\MJ_GDTDH_AI_CPP\\TdhAI\\x64\\Debug\\";
    private static String DEFAULTDIR_LINUX = "/usr/local/customize/lib/";
    private static String DEFAULTDIR_MAX = "/usr/lib/";

    /**
     * 获得当前平台的JNI库的默认后缀
     * @return
     */
    public static String getDynamicLibrarySuffix(){
        if(OSinfo.getOSname() == EPlatform.Windows){
            return WIN_SUFFIX_DYNAMICLIBRAY;
        }else if(OSinfo.getOSname() == EPlatform.Linux){
            return LINUX_SUFFIX_DYNAMICLIBRAY;
        }else if(OSinfo.getOSname() == EPlatform.Mac_OS){
            return MAC_0S_SUFFIX_DYNAMICLIBRAY;
        }else if(OSinfo.getOSname() == EPlatform.Mac_OS_X){
            return MAC_OX_X_SUFFIX_DYNAMICLIBRAY;
        }
        return DEFAULT_SUFFIX_DYNAMICLIBRAY;
    }

    /**
     * 获取当前平台默认的自定义目录
     * @return
     */
    public static String getDynamicLibraryDir(){
        if(OSinfo.getOSname() == EPlatform.Windows){
            return DEFAULTDIR_WIN;
        }else if(OSinfo.getOSname() == EPlatform.Linux){
            return DEFAULTDIR_LINUX;
        }else if(OSinfo.getOSname() == EPlatform.Mac_OS){
            return DEFAULTDIR_MAX;
        }else if(OSinfo.getOSname() == EPlatform.Mac_OS_X){
            return DEFAULTDIR_MAX;
        }
        return DEFAULT_SUFFIX_DYNAMICLIBRAY;
    }

    /**
     * 组合用户指定文件的完整的JNI库名称
     * @param libname
     * @return
     */
    public static String combinationLibrayName(String libname){
        StringBuffer fileName = new StringBuffer(libname);
        fileName.append(getDynamicLibrarySuffix());
        return fileName.toString();
    }

    /**
     * 获取项目加载类的根路径
     * @return
     */
    public static String getPath(){
        String path = "";
        try{
            //jar 中没有目录的概念
            URL location = PlatformPath.class.getProtectionDomain().getCodeSource().getLocation();//获得当前的URL
            File file = new File(location.getPath());//构建指向当前URL的文件描述符
            if(file.isDirectory()){//如果是目录,指向的是包所在路径，而不是文件所在路径
                path = file.getAbsolutePath();//直接返回绝对路径
            }else{//如果是文件,这个文件指定的是jar所在的路径(注意如果是作为依赖包，这个路径是jvm启动加载的jar文件名)
                path = file.getParent();//返回jar所在的父路径
            }
            logger.info("project path={}",path);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("{}",e);
        }
        return path;
    }

    /**
     * 使用相对目录加载JNI库
     * @param libname
     * @return
     */
    public static boolean SystemLoadLibrary(String libname) {
        try {
            System.loadLibrary(libname);
        } catch (UnsatisfiedLinkError ex) {
            System.out.println("系统目录下 "+libname+" 动态库不存在");
            logger.error("系统目录下 {} 动态库不存在 \n {}", libname,System.getProperty("java.library.path"));
            return false;
        }
        return true;
    }

    /**
     * 加载当前类路径下的完整的库名字
     * @param lib
     * @return
     */
    public static boolean SystemLoadClass(String lib) {
        String filename = "";
        try {
            String path = getPath();
            String libname = combinationLibrayName(lib);
            Path filePath = Paths.get(path,libname);
            filename = filePath.toString();
            System.load(filename);
        } catch (UnsatisfiedLinkError ex) {
            System.out.println("当前目录的 "+filename+" 动态库不存在");
            logger.error("当前目录的 {} 动态库不存在", filename);
            return false;
        }
        return true;
    }

    /**
     * 根据指定目录指定文件加载
     * @param path
     * @return
     */
    public static boolean SystemLoadAbsolutePath(Path path) {
        try {
            System.load(path.toString());
        } catch (UnsatisfiedLinkError ex) {
            System.out.println("用户指定的 "+path.toString()+" 动态库不存在");
            logger.error("用户自定义的 {} 动态库不存在", path.toString());
            return false;
        }
        return true;
    }

}
