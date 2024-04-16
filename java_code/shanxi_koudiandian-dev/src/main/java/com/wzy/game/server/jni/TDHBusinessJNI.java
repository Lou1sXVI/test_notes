package com.wzy.game.server.jni;

import com.wzy.game.server.util.PlatformPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class TDHBusinessJNI {
    private static Logger logger = LoggerFactory.getLogger(TDHBusinessJNI.class);
    private static String LINNAEM = "TdhAI";

    static {
        try {
            if (PlatformPath.SystemLoadLibrary(LINNAEM)) {
                System.out.println("load system lib "+ LINNAEM +" sucess");
                logger.info("load system lib {} sucess", LINNAEM);
            } else if (PlatformPath.SystemLoadClass(LINNAEM)) {
                System.out.println("load current path lib  "+LINNAEM +"  sucess");
                logger.info("load current path lib {} sucess", LINNAEM);
            } else if(PlatformPath.SystemLoadAbsolutePath(Paths.get(PlatformPath.getDynamicLibraryDir(),
                    PlatformPath.combinationLibrayName(LINNAEM)))){
                System.out.println("load user path lib  "+LINNAEM +"  sucess");
                logger.info("load user path lib {} sucess", LINNAEM);
            } else {
                logger.info("load failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static native int doAction(int retArray[], int aiSeat,int dealer,int level,int lastSeat,
                                      int pos0Tiles[],int pos1Tiles[],int pos2Tiles[],int pos3Tiles[],
                                      int actionSeat[] , int actionObtsit[],int actionType[],int obtCard[],
                                      int legalSeat[], int legalObtsit[],int legalType[],int legalCard[]
    );
}

