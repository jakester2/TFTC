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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Jacob Tyo
 * @author jtyo@redapt.com
 */
public class TPortWriter {
    
    private boolean     JuniperSNMode = false;
    private boolean     parsingOpperand = false;
    private int         customWriteDelay = 0;
    private int         writeDelay;
    private SerialPort  serialPort;
    private String      lineEnd = "\r\n";
    
    private final TPortManager portManager;
    private final HashMap<String, String> oppArray = new HashMap<>();
    
    public TPortWriter(TPortManager portManager) {
        this.portManager = portManager;
    }
    
    public void writeStringToSerial(SerialPort serialPort, String line) throws SerialPortException {
        TLogger.getLogger().log(serialPort.getPortName() + " <-TX- \"" + line + "\"");
        serialPort.writeBytes((line + "\r").getBytes());//Write data to port
    }
    
    public void writeFileToSerial(SerialPort serialPort, int writeDelay, File file) {
        this.writeDelay = writeDelay;
        this.serialPort = serialPort;
        
        String line;
        try (
            InputStream fis = new FileInputStream(file.getPath());
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
        ) {
            while ((line = br.readLine()) != null) {
                line = parseLineForOpperand(line);
                try {
                    Thread.sleep((customWriteDelay > writeDelay) ? customWriteDelay : writeDelay);
                } catch (InterruptedException ex) {
                    TLogger.getLogger().error(ex);
                }
                if (!parsingOpperand) writeStringToSerial(serialPort, line);
            } 
        } catch (FileNotFoundException ex) {
            TLogger.getLogger().fatal("File not found: " + ex);
        } catch (IOException ex) {
            TLogger.getLogger().fatal("File IO Exception: " + ex);
        } catch (SerialPortException ex) {
            TLogger.getLogger().fatal("Serial Port Exception: " + ex);
        }
    }
    
    private String parseLineForOpperand(String line) {
        customWriteDelay = 0;
        parsingOpperand = false;

        // Detect an opperation and execute accordingly
        if (line.startsWith("#")) { 
            parsingOpperand = true;
            String[] args;
            if (line.contains("FILE") && line.contains("|")) {
                args = line.replace("#", "").trim().split(" ", 4);
            } else {
                args = line.replace("#", "").trim().split(" ", 2);
            }
            switch (args[0].toUpperCase()) {
                case "//":      break;
                case "LINEEND": parseOppLineEnd(args[1]); break;
                case "NEWLINE": sendNewLine(); break;
                case "JSN":     setJSN(args[1]); break;
                case "WAIT":    parseOppWait(args[1]); break;
                case "FILE":    parseOppFile(args[1]); break;
                case "VAR":     parseOppVar(args[1], JuniperSNMode); break;
                case "CTRLD":   sendCtrlD(); break;
            }
        } else {
            line = fillInVariables(line);
        }
        return line;
    }

    private void parseOppWait(String arg) {
        try {
            customWriteDelay = Integer.parseInt(arg);
            TLogger.getLogger().info("Waiting " + customWriteDelay + "(milliseconds)");
        } catch (NumberFormatException nfe) {
            TLogger.getLogger().error("The parameter '" + arg + "' is not a number.");
        } catch (ArrayIndexOutOfBoundsException oob) {
            TLogger.getLogger().error("Wait parameter missing!");
        }
    }

    private void parseOppFile(String arg) {
        String line;
        File file = new File(fillInVariables(arg));
        try (
            InputStream fis = new FileInputStream(file.getPath());
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
        ) {
            while ((line = br.readLine()) != null) {
                try {
                    Thread.sleep(writeDelay);
                } catch (InterruptedException ex) {
                    TLogger.getLogger().error(ex);
                }
                TLogger.getLogger().info("Writing contents of file: " + file.getPath());
                writeStringToSerial(serialPort, line);
            }
        } catch (FileNotFoundException ex) {
            TLogger.getLogger().error("File not found: " + ex);
        } catch (IOException ex) {
            TLogger.getLogger().error("File IO Exception: " + ex);
        } catch (SerialPortException ex) {
            TLogger.getLogger().error(ex);
        }
    }
    
    private void parseOppVar(String arg, boolean juniperSN) {
        String key = arg.split(" ", 2)[0];
        String value = null;
        try {
            value = arg.split(" ", 2)[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            TLogger.getLogger().fatal("Missing parameter for VAR call.");
        }
        // Run if this needs to be executed first
        if (value.contains("[") && value.contains("]")) {
            value = value.replaceAll("\\[", "").replaceAll("]", "");
            String result = portManager.getPortReader().requestResultOfCommand(value);
            if (result != null) {
                if (juniperSN) {
                    // This step performs a unique method for slicing Juniper SN strings.
                    setOppVar(key, result.split("\r\n")[1].split(" ")[0]);
                } else {
                    // Normal result
                    setOppVar(key, result);
                }
            } else {
                TLogger.getLogger().error("Looks like the response was NULL");
            }
        } else {
            TLogger.getLogger().error("Variable set mode");
            setOppVar(key, value);
        }
        
    }

    private void setOppVar(String key, String value) {
        TLogger.getLogger().info("Setting VAR: " + key + " <- \"" + value + "\"");
        oppArray.put(key, value);
    }
    
    private String getOppVar(String key) {
        String output = oppArray.get(key);
        if (output == null) return "<NOVAR>";
        return output;
    }

    private String fillInVariables(String line) {
        // Replace defined variable tags with values
            if (line.contains("[") && line.contains("]")) {
                Pattern p = Pattern.compile("\\[(.*?)\\](?!\\s*\\])\\s*", Pattern.DOTALL);
                Matcher m = p.matcher(line);
                String after = line;
                while (m.find()) {
                    after = after.replace("[" + m.group(1) + "]", getOppVar(m.group(1)));
                }
                return after;
            }
        return line;
    }

    private void sendCtrlD() {
        try {
            TLogger.getLogger().info("Sending CTRL+D...");
            serialPort.writeString("\\cD");
        } catch (SerialPortException ex) {
            TLogger.getLogger().warn(ex);
        }
    }

    private void setJSN(String arg) {
        JuniperSNMode = Boolean.parseBoolean(arg);
    }

    private void parseOppLineEnd(String arg) {
        TLogger.getLogger().info("Setting LINEEND to => \"" + arg + "\"");
        lineEnd = arg;
    }
    
    public String getLineEnd() {
        return lineEnd;
    }

    private void sendNewLine() {
        try {
            TLogger.getLogger().info("Sending Newline...");
            this.serialPort.writeString("\n");
        } catch (SerialPortException ex) {
            TLogger.getLogger().error(ex);
        }
    }

}
