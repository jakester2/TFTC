/*
* Copyright (c) 2016 Jacob Tyo
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of 
* this software and associated documentation files (the "Software"), to deal in 
* the Software without restriction, including without limitation the rights to 
* use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
* of the Software, and to permit persons to whom the Software is furnished to do 
* so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all 
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS 
* FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
* COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
* IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
* CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package tyo.tool.tftc;

import tyo.util.TLogger;

/**
 *
 * @author Jacob Tyo
 * @author jtyo@redapt.com
 */
public class Main {
    public static final String VERSION = "0.9.001";
    public static final String BLDDATE = "02/07/2016";
    public static final String VERSIONLONG = "TFTC - Tyo File-to-COM (ver" + VERSION + ") MIT License 2015-2016";

    TPortManager manager = new TPortManager(this);
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main(args);
    }

    public Main(String[] args) {       
        manager.getGetParserUtil().parseArgs(args);
        manager.connect();
        try {
            TLogger.getLogger().info("Final sleep before exiting...");
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            TLogger.getLogger().fatal(ex);
        }
        manager.disconnect();
        exit(0);
    }
    
    public static void exit(int i) {
        if (i == 1) TLogger.getLogger().fatal("End.");
        if (i == 0) TLogger.getLogger().info("End.");
        System.exit(i);
    }

}

