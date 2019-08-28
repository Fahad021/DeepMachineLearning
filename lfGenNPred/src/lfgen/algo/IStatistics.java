package lfgen.algo;
/**
* @author JeremyChenlk
* @version 2019��2��13�� ����4:46:25
*
* Class description:
*	
*/

public interface IStatistics {
	
	int getCallTimes();
	long getInitTimeUse();
	long getTimeUse();
	
	double getAverageTimeUse();
	void resetStatistics();
}
