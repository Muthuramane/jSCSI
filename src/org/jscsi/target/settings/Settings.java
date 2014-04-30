package org.jscsi.target.settings;

/**
 * Instances of this class contain all session-wide and connection-specific
 * parameters that are either declared by the initiator or negotiated in the
 * text parameter negotiation stages.
 * 
 * @author Andreas Ergenzinger
 */
public final class Settings {

    /**
     * The {@link #settingsId} variable allows to compare the age of different
     * {@link Settings} objects - newer instances have a higher
     * {@link #settingsId}.
     * <p>
     * What it is really used for, however, is for determining if the buffered
     * {@link Settings} object, which can be accessed via
     * {@link ConnectionSettingsNegotiator#getSettings()} is up to date or if it
     * has to be replaced.
     * 
     * @see ConnectionSettingsNegotiator#getSettings()
     * @see SessionSettingsNegotiator#getCurrentSettingsId()
     */
    private final long settingsId;

    // connection parameters
    /**
     * The <code>DataDigest</code> parameter.
     */
    String dataDigest;

    /**
     * The <code>HeaderDigest</code> parameter.
     */
    String headerDigest;

    /**
     * The <code>IFMarker</code> parameter.
     */
    Boolean ifMarker;

    /**
     * The <code>IFMarkInt</code> parameter.
     */
    Integer ifMarkInt;

    /**
     * The <code>MaxRecvDataSegmentLength</code> parameter.
     */
    Integer maxRecvDataSegmentLength;

    /**
     * The <code>OFMarker</code> parameter.
     */
    Boolean ofMarker;

    /**
     * The <code>OFMarkInt</code> parameter.
     */
    Integer ofMarkInt;

    // session parameters
    
    /**
     * The <code>TargetName</code> parameter
     */
    String targetName;
    
    /**
     * The <code>DataPDUInOrder</code> parameter.
     */
    Boolean dataPduInOrder;

    /**
     * The <code>DataSequenceInOrder</code> parameter.
     */
    Boolean dataSequenceInOrder;

    /**
     * The <code>DefaultTime2Retain</code> parameter.
     */
    Integer defaultTime2Retain;

    /**
     * The <code>DefaultTime2Wait</code> parameter.
     */
    Integer defaultTime2Wait;

    /**
     * The <code>ErrorRecoveryLevel</code> parameter.
     */
    Integer errorRecoveryLevel;

    /**
     * The <code>FirstBurstLength</code> parameter.
     */
    Integer firstBurstLength;

    /**
     * The <code>ImmediateData</code> parameter.
     */
    Boolean immediateData;

    /**
     * The <code>InitialR2T</code> parameter.
     */
    Boolean initialR2T;

    /**
     * The <code>InitiatorAlias</code> parameter.
     */
    String initiatorAlias;

    /**
     * The <code>InitiatorName</code> parameter.
     */
    String initiatorName;

    /**
     * The <code>MaxBurstLength</code> parameter.
     */
    Integer maxBurstLength;

    /**
     * The <code>MaxConnections</code> parameter.
     */
    Integer maxConnections;

    /**
     * The <code>MaxOutstandingR2T</code> parameter.
     */
    Integer maxOutstandingR2T;

    /**
     * The <code>SessionType</code> parameter.
     */
    String sessionType;

    /**
     * Throws a {@link SettingsException} if the parameter is <code>null</code>.
     * 
     * @param member
     *            the member variable to check
     * @throws SettingsException
     *             if the parameter is <code>null</code>
     */
    private static void checkIfNull(Object member) throws SettingsException {
        if (member == null)
            throw new SettingsException();
    }

    /**
     * The constructor based on the builder pattern.
     * 
     * @param c
     *            {@link ConnectionSettingsBuilderComponent} with the current
     *            connection-specific parameters
     * @param s
     *            {@link SessionSettingsBuilderComponent} with the current
     *            session-wide parameters
     */
    Settings(final ConnectionSettingsBuilderComponent c,
            final SessionSettingsBuilderComponent s) {
        settingsId = s.settingsId;
        // connection parameters
        dataDigest = c.dataDigest;
        headerDigest = c.headerDigest;
        ifMarker = c.ifMarker;
        ifMarkInt = c.ifMarkInt;
        maxRecvDataSegmentLength = c.maxRecvDataSegmentLength;
        ofMarker = c.ofMarker;
        ofMarkInt = c.ofMarkInt;
        targetName = c.targetName;
        // session parameters

        dataPduInOrder = s.dataPduInOrder;
        dataSequenceInOrder = s.dataSequenceInOrder;
        defaultTime2Retain = s.defaultTime2Retain;
        defaultTime2Wait = s.defaultTime2Wait;
        errorRecoveryLevel = s.errorRecoveryLevel;
        firstBurstLength = s.firstBurstLength;
        immediateData = s.immediateData;
        initialR2T = s.initialR2T;
        initiatorAlias = s.initiatorAlias;
        initiatorName = s.initiatorName;
        maxBurstLength = s.maxBurstLength;
        maxConnections = s.maxConnections;
        maxOutstandingR2T = s.maxOutstandingR2T;
        sessionType = s.sessionType;
    }

    /**
     * Returns the {@link #settingsId}.
     * 
     * @return the {@link #settingsId}
     */
    long getSettingsId() {
        return settingsId;
    }

    /**
     * Returns the value of the <code>DataDigest</code> parameter.
     * 
     * @return the value of the <code>DataDigest</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public String getDataDigest() throws SettingsException {
        checkIfNull(dataDigest);
        return dataDigest;
    }

    /**
     * Returns the value of the <code>HeaderDigest</code> parameter.
     * 
     * @return the value of the <code>HeaderDigest</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public String getHeaderDigest() throws SettingsException {
        checkIfNull(headerDigest);
        return headerDigest;
    }

    /**
     * Returns the value of the <code>IFMarker</code> parameter.
     * 
     * @return the value of the <code>IFMarker</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public boolean getIfMarker() throws SettingsException {
        checkIfNull(ifMarker);
        return ifMarker;
    }

    /**
     * Returns the value of the <code>IFMarkInt</code> parameter.
     * 
     * @return the value of the <code>IFMarkInt</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getIfMarkInt() throws SettingsException {
        checkIfNull(ifMarkInt);
        return ifMarkInt;
    }

    /**
     * Returns the value of the <code>MaxRecvDataSegmentLenght</code> parameter.
     * 
     * @return the value of the <code>MaxRecvDataSegmentLenght</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getMaxRecvDataSegmentLength() throws SettingsException {
        checkIfNull(maxRecvDataSegmentLength);
        return maxRecvDataSegmentLength;
    }

    /**
     * Returns the value of the <code>OFMarker</code> parameter.
     * 
     * @return the value of the <code>OFMarker</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public boolean getOfMarker() throws SettingsException {
        checkIfNull(ofMarker);
        return ofMarker;
    }

    /**
     * Returns the value of the <code>OFMarkInt</code> parameter.
     * 
     * @return the value of the <code>OFMarkInt</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getOfMarkInt() throws SettingsException {
        checkIfNull(ofMarkInt);
        return ofMarkInt;
    }

    /**
     * Returns the value of the <code>DataPDUInOrder</code> parameter.
     * 
     * @return the value of the <code>DataPDUInOrder</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public boolean getDataPduInOrder() throws SettingsException {
        checkIfNull(dataPduInOrder);
        return dataPduInOrder;
    }

    /**
     * Returns the value of the <code>DataSequenceInOrder</code> parameter.
     * 
     * @return the value of the <code>DataSequenceInOrder</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public boolean getDataSequenceInOrder() throws SettingsException {
        checkIfNull(dataSequenceInOrder);
        return dataSequenceInOrder;
    }

    /**
     * Returns the value of the <code>ErrorRecoveryLevel</code> parameter.
     * 
     * @return the value of the <code>ErrorRecoveryLevel</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getErrorRecoveryLevel() throws SettingsException {
        checkIfNull(errorRecoveryLevel);
        return errorRecoveryLevel;
    }

    /**
     * Returns the value of the <code>DefaultTime2Retain</code> parameter.
     * 
     * @return the value of the <code>DefaultTime2Retain</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getDefaultTime2Retain() throws SettingsException {
        checkIfNull(defaultTime2Retain);
        return defaultTime2Retain;
    }

    /**
     * Returns the value of the <code>DefaultTime2Wait</code> parameter.
     * 
     * @return the value of the <code>DefaultTime2Wait</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getDefaultTime2Wait() throws SettingsException {
        checkIfNull(defaultTime2Wait);
        return defaultTime2Wait;
    }

    /**
     * Returns the value of the <code>FirstBurstLength</code> parameter.
     * 
     * @return the value of the <code>FirstBurstLength</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getFirstBurstLength() throws SettingsException {
        checkIfNull(firstBurstLength);
        return firstBurstLength;
    }

    /**
     * Returns the value of the <code>ImmediateData</code> parameter.
     * 
     * @return the value of the <code>ImmediateData</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public boolean getImmediateData() throws SettingsException {
        checkIfNull(immediateData);
        return immediateData;
    }

    /**
     * Returns the value of the <code>InitialR2T</code> parameter.
     * 
     * @return the value of the <code>InitialR2T</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public boolean getInitialR2T() throws SettingsException {
        checkIfNull(initialR2T);
        return initialR2T;
    }

    /**
     * Returns the value of the <code>InitiatorAlias</code> parameter.
     * 
     * @return the value of the <code>InitiatorAlias</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public String getInitiatorAlias() throws SettingsException {
        checkIfNull(initiatorAlias);
        return initiatorAlias;
    }

    /**
     * Returns the value of the <code>InitiatorName</code> parameter.
     * 
     * @return the value of the <code>InitiatorName</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public String getInitiatorName() throws SettingsException {
        checkIfNull(initiatorName);
        return initiatorName;
    }

    /**
     * Returns the value of the <code>MaxBurstLength</code> parameter.
     * 
     * @return the value of the <code>MaxBurstLength</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getMaxBurstLength() throws SettingsException {
        checkIfNull(maxBurstLength);
        return maxBurstLength;
    }

    /**
     * Returns the value of the <code>MaxConnections</code> parameter.
     * 
     * @return the value of the <code>MaxConnections</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getMaxConnections() throws SettingsException {
        checkIfNull(maxConnections);
        return maxConnections;
    }

    /**
     * Returns the value of the <code>MaxOutstandingR2T</code> parameter.
     * 
     * @return the value of the <code>MaxOutstandingR2T</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public int getMaxOutstandingR2T() throws SettingsException {
        checkIfNull(maxOutstandingR2T);
        return maxOutstandingR2T;
    }

    /**
     * Returns the value of the <code>SessionType</code> parameter.
     * 
     * @return the value of the <code>SessionType</code> parameter
     * @throws SettingsException
     *             if the parameter has not been declared or negotiated and
     *             there is no default value
     */
    public String getSessionType() throws SettingsException {
        checkIfNull(maxOutstandingR2T);
        return sessionType;
    }
    
    public String getTargetName() throws SettingsException {
        return targetName;
    }
}
