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
import java.io.File;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.apache.commons.cli.Options;
import static tyo.tool.tftc.Main.exit;

/**
 *
 * @author Jacob Tyo
 * @author jtyo@redapt.com
 */
public class TPortManager {
    
    private SerialPort serialPort;
    private final Options options = new Options();
    private TPortWriter portWriter;
    private TPortReader portReader;
    private final TStartupCommandParser getParserUtil = new TStartupCommandParser(this);
    
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int parity;
    private int writeDelay;
    private String comPort;
    private File file;
    
    public TPortManager(Main main) {
        initialize();
    }

    private void initialize() {
        options.addOption("help", false, "show help.");
        options.addOption("version", false, "show version info.");
        options.addOption("list", false, "List the available COM ports.");
        options.addOption("b", "baudrate", true, "Set the baud-rate to <arg>.");
        options.addOption("d", "databits", true, "Set the data-bits to <arg>.");
        options.addOption("s", "stopbits", true, "set the stop-bits to <arg>.");
        options.addOption("p", "parity", true, "Set the parity-bits to <arg>.");
        options.addOption("w", "writerate", true, "Set the rate in which the commands are written (in milliseconds).");
        options.addOption("c", "comport", true, "Connect to COM <arg>.");
        options.addOption("f", "file", true, "UTF-8 file containing a list of commands to execute.");
    }
    
    public void connect() {
        TLogger.getLogger().info("Connecting to " + comPort + "...");
        serialPort = new SerialPort(comPort);
        portWriter = new TPortWriter(this);
        portReader = new TPortReader(this, serialPort);
        try {
            serialPort.openPort();
            serialPort.setParams(baudRate, dataBits, stopBits, parity);
        } catch (SerialPortException ex) {
            TLogger.getLogger().fatal("The port \"" + comPort + "\" is either unavailable, or does not exist.");
            TLogger.getLogger().info("For a list of available ports, run TFTC with the \"-l\" flag.");
            exit(1);
        }
        try {
            serialPort.addEventListener(portReader, SerialPort.MASK_RXCHAR);
        } catch (SerialPortException ex) {
            TLogger.getLogger().fatal(ex);
        }
        portWriter.writeFileToSerial(serialPort, writeDelay, file);
    }
    
    public void disconnect() {
        TLogger.getLogger().info("Disconnecting...");
        try {
            serialPort.closePort();
            serialPort = null;
        } catch (SerialPortException ex) {
            TLogger.getLogger().error(ex);
        }
    }

    /**
     * @return the getParserUtil
     */
    public TStartupCommandParser getGetParserUtil() {
        return getParserUtil;
    }

    public Options getOptions() {
        return options;
    }
    
    /**
     * @param baudRate the baudRate to set
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    /**
     * @param dataBits the dataBits to set
     */
    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    /**
     * @param stopBits the stopBits to set
     */
    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    /**
     * @param parity the parity to set
     */
    public void setParity(int parity) {
        this.parity = parity;
    }

    /**
     * @param writeDelay the writeDelay to set
     */
    public void setWriteDelay(int writeDelay) {
        this.writeDelay = writeDelay;
    }

    /**
     * @param comPort the comPort to set
     */
    public void setComPort(String comPort) {
        this.comPort = comPort;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    public TPortWriter getPortWriter() {
        return portWriter;
    }
    
    public TPortReader getPortReader() {
        return portReader;
    }
    
}
