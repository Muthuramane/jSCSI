package org.jscsi.target;

import java.awt.Cursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.login.ISID;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.target.connection.TargetConnection;
import org.jscsi.target.connection.TargetSession;
import org.jscsi.target.scsi.inquiry.DeviceIdentificationVpdPage;
import org.jscsi.target.settings.StorageFileTargetInfo;
import org.jscsi.target.settings.TargetConfiguration;
import org.jscsi.target.settings.TargetConfigurationXMLParser;
import org.jscsi.target.settings.TargetInfo;
import org.jscsi.target.storage.AbstractStorageModule;
import org.jscsi.target.storage.RandomAccessStorageModule;
import org.jscsi.target.storage.SynchronizedRandomAccessStorageModule;
import org.xml.sax.SAXException;

/**
 * The central class of the jSCSI Target, which keeps track of all active
 * {@link TargetSession}s, stores target-wide parameters and variables, and
 * which contains the {@link #main(String[])} method for starting the program.
 * 
 * @author Andreas Ergenzinger
 */
public final class TargetServer {

    private static final Logger LOGGER = Logger.getLogger(TargetServer.class);

    /**
     * The name of the <i>log4j</i> properties file.
     * 
     * @see #readLog4jConfigurationFile()
     */
    private static final String LOG4J_PROPERTIES_XML = "log4j.xml";

    /**
     * The relative path to <code>src/main/resources/</code>. The
     * {@link #LOG4J_PROPERTIES_XML} file may be located there.
     * 
     * @see #readLog4jConfigurationFile()
     */
    private static final String RESOURCES_DIRECTORY = "src/main/resources/";

    /**
     * A {@link SocketChannel} used for listening to incoming connections.
     */
    private ServerSocketChannel serverSocketChannel;

    /**
     * Contains all active {@link TargetSession}s.
     */
    private Collection<TargetSession> sessions = new Vector<TargetSession>();

    /**
     * The jSCSI Target's global parameters.
     */
    private TargetConfiguration config;

    /**
     * 
     */
    private DeviceIdentificationVpdPage deviceIdentificationVpdPage;
    
    /**
     * The table of targets
     */
    private HashMap<String, Target>targets = new HashMap<String, Target>();
    
    /**
     * A target-wide counter used for providing the value of sent
     * {@link ProtocolDataUnit}s' <code>Target Transfer Tag</code> field, unless
     * that field is reserved.
     */
    private static final AtomicInteger nextTargetTransferTag = new AtomicInteger();

    /**
     * Gets and increments the value to use in the next unreserved
     * <code>Target Transfer Tag</code> field of the next PDU to be sent by the
     * jSCSI Target.
     * 
     * @see #nextTargetTransferTag
     * @return the value to use in the next unreserved <code>Target Transfer Tag
     * </code> field
     */
    public static int getNextTargetTransferTag() {
        // value 0xffffffff is reserved
        int tag;
        do {
            tag = nextTargetTransferTag.getAndIncrement();
        } while (tag == -1);
        return tag;
    }

    /**
     * Starts the jSCSI target.
     * 
     * @param args
     *            all command line arguments are ignored
     */
    public static void main(String[] args) 
    {
        TargetServer target = new TargetServer();
        target.start();
    }

    public void start()
    {
        // initialize loggers based on settings from file
        readLog4jConfigurationFile();

        System.out.println("jSCSI Target");

        // read target settings from configuration file
        // exit if there is a problem
        if (!readConfig()) {
            LOGGER.fatal("Error while trying to read settings from "
                    + TargetConfigurationXMLParser.CONFIGURATION_FILE_NAME
                    + ".\nShutting down.");
            return;
        }
        System.out.println("   port:           " + getConfig().getPort());
        // open the storage medium
        try {
            TargetInfo [] targetInfo = getConfig().getTargetInfo();
            for (TargetInfo curTargetInfo:targetInfo)
            {
                StorageFileTargetInfo curStorageFileTargetInfo = (StorageFileTargetInfo)curTargetInfo;
                RandomAccessStorageModule curStorageModule = SynchronizedRandomAccessStorageModule.open(curStorageFileTargetInfo
                    .getStorageFilePath());
                addStorageModule(curStorageFileTargetInfo.getTargetName(), curStorageFileTargetInfo.getTargetAlias(),
                        curStorageModule);
                // print configuration and medium details
                System.out.println("   target name:    " + curStorageFileTargetInfo.getTargetName());

                System.out.println("   storage file:   " + curStorageFileTargetInfo.getStorageFilePath());
                System.out.println("   file size:      "
                        + curStorageModule.getHumanFriendlyMediumSize());
            }
        } catch (FileNotFoundException e) {
            LOGGER.fatal(e.toString());
            return;
        }
        mainLoop();
    }

    public void mainLoop()
    {
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        // Create a blocking server socket and check for connections
        try {
            // Create a blocking server socket channel on the specified/default
            // port
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true);
            serverSocketChannel.socket().bind(
                    new InetSocketAddress(getConfig().getPort()));

            while (true) {
                // Accept the connection request.
                // If serverSocketChannel is blocking, this method blocks.
                // The returned channel is in blocking mode.
                final SocketChannel socketChannel = serverSocketChannel
                        .accept();

                // deactivate Nagle algorithm
                socketChannel.socket().setTcpNoDelay(true);

                final TargetConnection connection = new TargetConnection(
                        socketChannel, true);
                try {
                    final ProtocolDataUnit pdu = connection.receivePdu();
                    // confirm OpCode
                    if (pdu.getBasicHeaderSegment().getOpCode() != OperationCode.LOGIN_REQUEST)
                        throw new InternetSCSIException();
                    // get initiatorSessionID
                    LoginRequestParser parser = (LoginRequestParser) pdu
                            .getBasicHeaderSegment().getParser();
                    ISID initiatorSessionID = parser.getInitiatorSessionID();

                    /*
                     * TODO get (new or existing) session based on TSIH But
                     * since we don't do session reinstatement and
                     * MaxConnections=1, we can just create a new one.
                     */
                    TargetSession session = new TargetSession(this, connection,
                            initiatorSessionID,
                            parser.getCommandSequenceNumber(),// set ExpCmdSN
                                                              // (PDU is
                                                              // immediate,
                                                              // hence no ++)
                            parser.getExpectedStatusSequenceNumber());

                    sessions.add(session);
                    threadPool.submit(connection);// ignore returned Future
                } catch (DigestException e) {
                    LOGGER.info(e);
                    continue;
                } catch (InternetSCSIException e) {
                    LOGGER.info(e);
                    continue;
                } catch (Exception e) {
                    // something went wrong on the target side
                    // (programming error)
                    LOGGER.fatal(e);
                }
            }
        } catch (IOException e) {
            // this block is entered if the desired port is already in use
            LOGGER.fatal(e);
        }
    }

    /**
     * Reads target settings from configuration file and stores them in the
     * {@link #config} object. Returns <code>false</code> if the operation could
     * not be completed successfully, else it returns <code>true</code>.
     * 
     * @return <code>true</code> if the target settings were read from the
     *         configuration file, <code>false</code> otherwise. {@see
     *         TargetConfiguration}
     */
    private boolean readConfig() {
        try {
            setConfig(new TargetConfigurationXMLParser().parseSettings());
        } catch (SAXException e) {
            LOGGER.fatal(e);
            return false;
        } catch (ParserConfigurationException e) {
            LOGGER.fatal(e);
            return false;
        } catch (IOException e) {
            LOGGER.fatal(e);
            return false;
        }
        return true;
    }

    /**
     * Removes a session from the jSCSI Target's list of active sessions.
     * 
     * @param session
     *            the session to remove from the list of active sessions
     */
    public synchronized void removeTargetSession(TargetSession session) {
        sessions.remove(session);
    }

    /**
     * Reads the <i>log4j</i> properties from the file <code>log4j.xml</code>
     * located either in the base directory or <code>src/main/resources/</code>.
     * <p>
     * The logging properties will be read just once, changing them during
     * runtime will have no effect.
     * <p>
     * If <code>log4j.xml</code> cannot be found, then there will be no logging.
     */
    private static final void readLog4jConfigurationFile() {
        // look in base directory
        String path;
        File file = new File(LOG4J_PROPERTIES_XML);
        if (file.exists())
            path = LOG4J_PROPERTIES_XML;// for release versions
        else
            // use resources directory (location used during development)
            path = RESOURCES_DIRECTORY + LOG4J_PROPERTIES_XML;
        // configure
        DOMConfigurator.configure(path);
    }

    public TargetConfiguration getConfig()
    {
        return config;
    }

    public void setConfig(TargetConfiguration config)
    {
        this.config = config;
        deviceIdentificationVpdPage = new DeviceIdentificationVpdPage(this);
    }

    public DeviceIdentificationVpdPage getDeviceIdentificationVpdPage()
    {
        return deviceIdentificationVpdPage;
    }

    public Target getTarget(String targetName)
    {
        synchronized(targets)
        {
            return targets.get(targetName);
        }
    }

    public String [] getTargetNames()
    {
        String [] returnNames = new String[targets.size()];
        returnNames = targets.keySet().toArray(returnNames);
        return returnNames;
    }
    
    public void addStorageModule(String targetName, String targetAlias, AbstractStorageModule storageModule)
    {
        Target addTarget = new Target(targetName, targetAlias, storageModule);
        synchronized(targets)
        {
            targets.put(targetName, addTarget);
        }
    }
    
    /**
     * Checks to see if this target name is valid.
     * 
     * @param checkTargetName
     * @return true if the the target name is configured
     */
    public boolean isValidTargetName(String checkTargetName)
    {
        return targets.containsKey(checkTargetName);
    }

    public boolean removeStorageModule(String exportTargetName)
    {
        // TODO - check for logins
        synchronized(targets)
        {
            return (targets.remove(exportTargetName) != null);
        }
    }
}
