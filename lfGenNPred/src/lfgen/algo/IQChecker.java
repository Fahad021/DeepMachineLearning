package lfgen.algo;

import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019��2��13�� ����3:48:58
*
* Class description:
*	
*/

public interface IQChecker {
	
	boolean correct(AclfCase aclfCase);
	
	void setQLimitSparseFactor(double qLimitSparseFactor);
}
