package org.psicover.altimeter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DumpToHka {

	private static void close(Closeable c) {
		if(c==null) return;
		
		try {
			c.close();
		} catch (IOException e) {
		}
	}
	
	public static void main(String[] args) {
		File inf = new File("dump20161211.txt");
		File outf = new File("dump20161211.fda");
		
		Pattern p = Pattern.compile(">\\s+0x([0-9a-f][0-9a-f])", Pattern.CASE_INSENSITIVE);
		BufferedReader r = null;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outf);
			r = new BufferedReader(new FileReader(inf));
			String line = null;
			while ((line = r.readLine())!= null) {
				Matcher m = p.matcher(line);
				if(m.matches()) {
					int byteVal = Integer.parseInt(m.group(1), 16);
					out.write(byteVal);
				}
			}
		} catch(IOException e) {
			close(r);
			close(out);
		}

	}

}
