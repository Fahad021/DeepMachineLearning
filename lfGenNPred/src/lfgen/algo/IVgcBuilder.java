package lfgen.algo;

import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019��2��13�� ����6:21:30
*
* Class description:
*	
*/

public interface IVgcBuilder {
	VoltageGenCondition nowCondition();
	VoltageGenCondition nextCondition();
}
