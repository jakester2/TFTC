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
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author Jacob Tyo
 * @author jtyo@redapt.com
 */
public class TPortReader implements SerialPortEventListener {
    
    private final TPortManager portManager;
    private final SerialPort serialPort;
    
    private boolean resultRequested = false;
    private String result = "";
    private String line = "";
    
    public TPortReader(TPortManager portManager, SerialPort serialPort) {
        this.portManager = portManager;
        this.serialPort = serialPort;
    }
    
    public String requestResultOfCommand(String value) {
        result = "";
        resultRequested = true;
        try {
            portManager.getPortWriter().writeStringToSerial(serialPort, value);
        } catch (SerialPortException ex) {
            TLogger.getLogger().fatal(ex);
        }
        int waitCount = 0;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                TLogger.getLogger().error(ex);
            }
            if (result == null) {
                if (waitCount >= 5) {
                    TLogger.getLogger().log("No response recieved...");
                    return null;
                } else {
                    TLogger.getLogger().log("No response recieved. Waiting...");
                    waitCount++;
                    break;
                }
            } else {
                break;
            }
        }
        resultRequested = false;
        return result;
    }
    
    @Override
    public void serialEvent(SerialPortEvent event) {
        String lineEnd = portManager.getPortWriter().getLineEnd();
        
        if(event.isRXCHAR()) {
            String in;
            try {
                in = serialPort.readString(event.getEventValue());
            } catch (SerialPortException ex) {
                TLogger.getLogger().error(ex);
                return;
            }
            line += in;
            if (in.endsWith("\n") || line.contains(lineEnd)) {
                if (resultRequested) {
                    if (line.contains(lineEnd)) line = line.substring(0, line.indexOf(lineEnd) + lineEnd.length());
                    TLogger.getLogger().info("Response: \"" + line + "\"");
                    result += line;
                } else {
                    TLogger.getLogger().info(line.trim());
                }
                line = "";
            }
        }
    }

}
