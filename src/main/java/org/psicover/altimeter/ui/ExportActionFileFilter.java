package org.psicover.altimeter.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

// I'm sad, FileNameExtensionFilter is final...
public abstract class ExportActionFileFilter extends FileFilter {
	private final String description;
	private final String [] extensions;

	public ExportActionFileFilter(String description, String ... extensions) {
		this.description = description;
		this.extensions = new String [extensions.length];
		for(int i = 0; i < extensions.length; i++)
			this.extensions[i] = "."+extensions[i].toLowerCase();
	}
	
	@Override
	public boolean accept(File f) {
		String name = f.getName().toLowerCase();
		for(String ext : extensions) {
			if(name.endsWith(ext)) return true;
		}
		
		return false;
	}
	
	public File ensureFileExtension(File f) {
		if(accept(f)) return f;
		return new File(f.getParentFile(), f.getName()+this.extensions[0]);
	}

	@Override
	public String getDescription() {
		return description;
	}
	
}
