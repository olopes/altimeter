package org.psicover.altimeter.bean;

import java.io.Serializable;
import java.util.List;

public class AltimeterFile implements Serializable {
	private static final long serialVersionUID = 7048329981569329087L;

	private final AltimeterSession[] sessions;
	private final String fileName;

	public AltimeterFile(String fileName, List<AltimeterSession> sessions) {
		this(fileName, sessions.toArray(new AltimeterSession[sessions.size()]));
	}

	public AltimeterFile(String fileName, AltimeterSession[] sessions) {
		this.fileName = fileName;
		this.sessions = sessions;
	}

	public AltimeterSession[] getSessions() {
		return sessions;
	}

	public String getFileName() {
		return fileName;
	}
}
