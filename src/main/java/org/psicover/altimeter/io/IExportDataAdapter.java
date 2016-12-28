package org.psicover.altimeter.io;

import java.io.File;

import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.ui.AltimeterIOException;

public interface IExportDataAdapter {
	
	void write(AltimeterFile data, AltimeterSession currentSession, File outputFile) throws AltimeterIOException;

}
