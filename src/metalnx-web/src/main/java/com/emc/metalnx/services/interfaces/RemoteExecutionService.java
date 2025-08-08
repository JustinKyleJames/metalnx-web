 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.MetalnxException;

import java.io.IOException;

public interface RemoteExecutionService {

	String execute(String command) throws MetalnxException, IOException, 
		DataGridConnectionRefusedException ;
	
}
