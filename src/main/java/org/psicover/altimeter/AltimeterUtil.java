package org.psicover.altimeter;

import org.psicover.altimeter.ui.AltimeterVisualization;

public class AltimeterUtil {

	public static void main(String[] args) {
		// TODO parse arguments
		if(args.length == 0)
			AltimeterVisualization.main(args);
		else
			ExportData.main(args);
	}

}
