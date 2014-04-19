package org.jscsi.target;

import org.jscsi.target.storage.AbstractStorageModule;

/**
 * One Target exists per iSCSI named target.  Holds onto the name and the AbstractStorageModule
 * @author David L. Smith-Uchida
 *
 *jSCSI
 *
 * Copyright (C) 2009 iGeek, Inc.  All Rights Reserved
 */
public class Target
{
    private String targetName;
    private String targetAlias;
    private AbstractStorageModule storageModule;
    private TargetPerformance targetReadPerformance, targetWritePerformance;
    
    public Target(String targetName, String targetAlias, AbstractStorageModule storageModule)
    {
        this.targetName = targetName;
        this.targetAlias = targetAlias;
        this.storageModule = storageModule;
        targetReadPerformance = new TargetPerformance();
        targetWritePerformance = new TargetPerformance();
    }

    public String getTargetName()
    {
        return targetName;
    }

    public String getTargetAlias()
    {
        return targetAlias;
    }
    
    public AbstractStorageModule getStorageModule()
    {
        return storageModule;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((targetName == null) ? 0 : targetName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Target other = (Target) obj;
        if (targetName == null)
        {
            if (other.targetName != null)
                return false;
        } else if (!targetName.equals(other.targetName))
            return false;
        return true;
    }

    public long getBytesReadPerSecond()
    {
    	return targetReadPerformance.getBytesPerSecond();
    }
    
    public long getBytesWrittenPerScond()
    {
    	return targetWritePerformance.getBytesPerSecond();
    }
    
    public void addReadPerformancePoint(long bytesTransferred, long startTime, long endTime)
    {
    	targetReadPerformance.addPerformancePoint(bytesTransferred, startTime, endTime);
    }
    
    public void addWritePerformancePoint(long bytesTransferred, long startTime, long endTime)
    {
    	targetWritePerformance.addPerformancePoint(bytesTransferred, startTime, endTime);
    }
}
