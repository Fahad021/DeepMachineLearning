package lfgen.algo;

import lfgen.datatype.AclfCase;

/**
* @author JeremyChenlk
* @version 2019��2��13�� ����4:00:56
*
* Class description:
*	
*/

public interface ISpecialBusChecker {
	
	void correct(AclfCase aclfCase);

	String getMethodName(); 
}
