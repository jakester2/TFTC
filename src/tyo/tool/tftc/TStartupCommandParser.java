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
import jssc.SerialPortList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Jacob Tyo
 * @author jtyo@redapt.com
 */
public class TStartupCommandParser {
    
    private final TPortManager manager;
    
    public TStartupCommandParser(TPortManager manager) {
        this.manager = manager;
    }
    
    public void parseArgs(String[] args) {
        TLogger.getLogger().info("Parsing arguments...");
        
        CommandLineParser parser =  new DefaultParser();
        CommandLine cmd;
        
        try {
            cmd = parser.parse(manager.getOptions(), args);
            if(cmd.hasOption("help")) {
                showHelp(manager.getOptions());
                Main.exit(0);
            }
            
            if(cmd.hasOption("version")) {
                showVersion();
                Main.exit(0);
            }
            
            if(cmd.hasOption("list")) {
                showSerialPorts();
                Main.exit(0);
            }
            
            if(cmd.hasOption("b")) {
                int value;
                value = TStartupCommandParser.parseBaudRate(cmd.getOptionValue("b"));
                TLogger.getLogger().info("Setting baud rate to => " + value);
                manager.setBaudRate(value);
            } else {
                TLogger.getLogger().info("Setting baud rate to (DEFAULT) => 9600");
                manager.setBaudRate(SerialPort.BAUDRATE_9600);
            }
            
            if(cmd.hasOption("d")) {
                int value = TStartupCommandParser.parseDataBits(cmd.getOptionValue("d"));
                TLogger.getLogger().info("Setting data bits to => " + value);
                manager.setDataBits(value);
            } else {
                TLogger.getLogger().info("Setting data bits to (DEFAULT) => 8");
                manager.setDataBits(SerialPort.DATABITS_8);
            }
            
            if(cmd.hasOption("s")) {
                int value = TStartupCommandParser.parseStopBits(cmd.getOptionValue("s"));
                TLogger.getLogger().info("Setting stop bits to => " + value);
                manager.setStopBits(value);
            } else {
                TLogger.getLogger().info("Setting stop bits to (DEFAULT) => 1");
                manager.setStopBits(SerialPort.STOPBITS_1);
            }
            
            if(cmd.hasOption("p")) {
                int value = TStartupCommandParser.parseParity(cmd.getOptionValue("p"));
                TLogger.getLogger().info("Setting parity to => " + value);
                manager.setParity(value);
            } else {
                TLogger.getLogger().info("Setting parity to (DEFAULT) => NONE");
                manager.setParity(SerialPort.PARITY_NONE);
            }
            
            if(cmd.hasOption("w")) {
                int value = TStartupCommandParser.parseWriteDelay(cmd.getOptionValue("w"));
                TLogger.getLogger().info("Setting write delay to => " + value + "(ms)");
                manager.setWriteDelay(value);
            } else {
                TLogger.getLogger().info("Setting write delay to (DEFAULT) => 500(ms)");
                manager.setWriteDelay(500);
            }
            
            if(cmd.hasOption("c")) {
                String value = TStartupCommandParser.parseCOMPort(cmd.getOptionValue("c"));
                if (value == null) Main.exit(1);
                TLogger.getLogger().info("Setting com port to => " + value);
                manager.setComPort(value);
            }
            
            if(cmd.hasOption("f")) {
                String value = TStartupCommandParser.parseFile(cmd.getOptionValue("f"));
                if (value == null) Main.exit(1);
                File f = new File(value);
                TLogger.getLogger().info("Setting file to => \"" + f.getName() + "\"");
                manager.setFile(f);
            }
        } catch (ParseException ex) {
            TLogger.getLogger().error(ex);
        }
        
    }

    private static int parseBaudRate(String optionValue) throws NumberFormatException {
        int baud;
        try {
            baud = Integer.parseInt(optionValue);
            switch (optionValue) {
                case "110":     return SerialPort.BAUDRATE_110;
                case "300":     return SerialPort.BAUDRATE_300;
                case "600":     return SerialPort.BAUDRATE_600;
                case "1200":    return SerialPort.BAUDRATE_1200;
                case "4800":    return SerialPort.BAUDRATE_4800;
                case "9600":    return SerialPort.BAUDRATE_9600;
                case "14400":   return SerialPort.BAUDRATE_14400;
                case "19200":   return SerialPort.BAUDRATE_19200;
                case "38400":   return SerialPort.BAUDRATE_38400;
                case "57600":   return SerialPort.BAUDRATE_57600;
                case "115200":  return SerialPort.BAUDRATE_115200;
                default: 
                    TLogger.getLogger().warn("Baud rate is invalid. Requesting (DEFAULT):");
                    return SerialPort.BAUDRATE_9600;
            }
        } catch (NumberFormatException numberFormatException) {
            throw new NumberFormatException();
        }
    }

    private static int parseDataBits(String optionValue) {
        switch (optionValue) {
            case "5": return SerialPort.DATABITS_5;
            case "6": return SerialPort.DATABITS_6;
            case "7": return SerialPort.DATABITS_7;
            case "8": return SerialPort.DATABITS_8;
            default: 
                TLogger.getLogger().warn("Baud rate is invalid. Requesting (DEFAULT):");
                return SerialPort.DATABITS_8;
        }
    }

    private static int parseStopBits(String optionValue) {
        switch (optionValue) {
            case "1": return SerialPort.STOPBITS_1;
            case "1.5": return SerialPort.STOPBITS_1_5;
            case "2": return SerialPort.STOPBITS_2;
            default: 
                TLogger.getLogger().warn("Stop bits invalid. Requesting (DEFAULT):");
                return SerialPort.STOPBITS_1;
        }
    }

    private static int parseParity(String optionValue) {
        switch (optionValue) {
            case "none":    return SerialPort.PARITY_NONE;
            case "even":    return SerialPort.PARITY_EVEN;
            case "odd":     return SerialPort.PARITY_ODD;
            case "mark":    return SerialPort.PARITY_MARK;
            case "space":   return SerialPort.PARITY_SPACE;
            default: 
                TLogger.getLogger().warn("Parity is invalid. Requesting (DEFAULT):");
                return SerialPort.PARITY_NONE;
        }
    }

    private static int parseWriteDelay(String optionValue) {
        int w = Integer.parseInt(optionValue);
        if (w < 100) {
            TLogger.getLogger().warn("Write delay to low. Must be >= 100. Requesting (DEFAULT):");
            return 500;
        }
        return w;
    }

    private static String parseCOMPort(String optionValue) {
        optionValue = optionValue.toUpperCase();
        if (optionValue.contains("COM") && optionValue.length() >= 4) {
            try {
                Integer.parseInt(optionValue.replace("COM", ""));
                return optionValue;
            } catch (NumberFormatException numberFormatException) {
                TLogger.getLogger().error("Invalid com port. Acceptible formats are: 'com1', 'COM3', etc." + numberFormatException);
                return null;
            }
        } else {
            TLogger.getLogger().error("Invalid com port. Acceptible formats are: 'com1', 'COM3', etc.");
            return null;
        }
    }

    private static String parseFile(String optionValue) {
        File file = new File(optionValue);
        if (file.isFile() && file.canRead()) {
            return optionValue;
        } else {
            TLogger.getLogger().error("The chosen file is either does not exist or is unreadlable.");
            return null;
        }
    }
    
    private void showHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Main", options);
    }

    private void showVersion() {
        TLogger.getLogger().info(Main.VERSIONLONG);
    }
    
    private String[] showSerialPorts() {
        String[] portNames = SerialPortList.getPortNames();
        TLogger.getLogger().info("Available COM ports:");
        for (String portName : portNames) {
            TLogger.getLogger().info(portName);
        }
        return portNames;
    }
    
}
