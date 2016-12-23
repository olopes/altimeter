package org.psicover.altimeter.io;

import java.io.UnsupportedEncodingException;

public class AltimeterAdapter {
	
	static enum Command {
		UPLOAD   (15,-38,16,0,-54,3),
		CLEAR    (15,-38,16,0,-52,3),
		SETUP_1HZ(15,-38,16,0,-53,3,0),
		SETUP_2HZ(15,-38,16,0,-53,3,1),
		SETUP_3HZ(15,-38,16,0,-53,3,2),
		SETUP_4HZ(15,-38,16,0,-53,3,3),
		;
		public final byte [] data;
		public final String msg;
		private Command(final int ... data) {
			this.data = new byte[7];
			for(int i = 0; i < 7 && i < data.length; i++)
				this.data[i] = (byte) data[i];
			String msg;
			try {
				msg=new String(this.data, "ASCII");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				msg="";
			}
			this.msg=msg;
			
		}
	}

}
