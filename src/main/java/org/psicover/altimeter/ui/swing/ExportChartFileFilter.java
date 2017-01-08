package org.psicover.altimeter.ui;

import org.psicover.altimeter.io.IExportChartAdapter;

// I'm sad, FileNameExtensionFilter is final...
public class ExportChartFileFilter extends ExportActionFileFilter {
	private final IExportChartAdapter action;

	public ExportChartFileFilter(IExportChartAdapter action, String description, String ... extensions) {
		super(description, extensions);
		this.action = action;
	}
	
	public IExportChartAdapter getAdapter() {
		return action;
	}
	
}
