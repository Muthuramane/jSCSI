package org.jscsi.target;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

class PerformancePoint
{
	long bytesTransferred;
	long startTime, endTime;
	
	public PerformancePoint(long bytesTransferred, long startTime, long endTime)
	{
		this.bytesTransferred = bytesTransferred;
		this.startTime = startTime;
		this.endTime = endTime;
	}
}

public class TargetPerformance
{
	private LinkedList<PerformancePoint> performanceLog = new LinkedList<PerformancePoint>();

	public static final long kTimeToAverageOver = 10L * 1000L;	// average over 30 seconds

	public TargetPerformance()
	{
		
	}
	
	public void addPerformancePoint(long bytesTransferred, long startTime, long endTime)
	{
		PerformancePoint addPoint = new PerformancePoint(bytesTransferred, startTime, endTime);
		synchronized(performanceLog)
		{
			while(!performanceLog.isEmpty() && (endTime - performanceLog.getFirst().endTime > kTimeToAverageOver))
				performanceLog.removeFirst();
			performanceLog.add(addPoint);
		}
	}
	
	public long getBytesPerSecond()
	{
		long curTime = System.currentTimeMillis();
		long bytesPerSecond = 0L;
		synchronized(performanceLog)
		{
			// Remove any samples that are too old
			while(!performanceLog.isEmpty() && (curTime - performanceLog.getFirst().endTime > kTimeToAverageOver))
				performanceLog.removeFirst();
			if (!performanceLog.isEmpty())
			{
				ListIterator<PerformancePoint>iterator = performanceLog.listIterator();
				long startTime = performanceLog.getFirst().startTime;
				long endTime = performanceLog.getLast().endTime;
				long elapsedTime = endTime - startTime;
				long bytesTransferred = 0L;
				while (iterator.hasNext())
				{
					bytesTransferred += iterator.next().bytesTransferred;
				}
				if (elapsedTime == 0)
					elapsedTime = 1;	// Pesky divide by zero errors
				bytesPerSecond = (bytesTransferred/elapsedTime) * 1000L;
			}
		}
		return bytesPerSecond;
	}
}
