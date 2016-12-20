package org.psicover.altimeter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read and write FDA/HKA files
 * @author oscar
 *
 */
public class AltimeterFileIO {
	private static String signatureAsHex(byte[] signature) {
		StringBuilder sb = new StringBuilder();
		for(byte c : signature) {
			int b = c & 0xff;
			sb.append(Integer.toHexString(b>>4)).append(Integer.toHexString(b&0xf));
		}
		return sb.toString();
	}

	public static List<AltimeterSession> readFile(File f) {
		
		List<AltimeterSession> result = new ArrayList<>();
		Pattern sigPatt = Pattern.compile("070fda1000ca(..)00", Pattern.CASE_INSENSITIVE);
		
		try (FileInputStream in = new FileInputStream(f)){
			byte []signature = new byte[8];
			int r = 0;
			r = in.read(signature);
			if(r != signature.length) throw new IOException("Could not read file signature");
			// validate file signature
			String hexSig = signatureAsHex(signature);
			System.out.println("Signature: "+hexSig);
			Matcher matcher = sigPatt.matcher(hexSig);
			if(matcher.matches()) {
				System.out.println("Found good signature type: "+matcher.group(1));
			} else {
				throw new IOException("Unknown signature type: "+hexSig);
			}

			byte []header = new byte[4];
			r = in.read(header);
			if(r != header.length) throw new IOException("Could not read file header");
			
			// calculate file size
			int size = (header[1]&0xff)-2; // why??
			size = size << 8 | (header[2]&0xff);
			size = size << 8 | (header[3]&0xff);
			System.out.println("File size: "+size);
			
			boolean preamble = true;
			byte [] sampleData = new byte[4];
			List<AltimeterSample> samples = new ArrayList<>();
			AltimeterSession session = null;
			while((r = in.read(sampleData))>0) {
				if(sampleData[0] == -1 && sampleData[1] == -1 && sampleData[2] == -1 && sampleData[3] == -1) {
					preamble = true;
				} else if(preamble) {
					if(null != session) {
						session.setData(samples.toArray(new AltimeterSample[samples.size()]));
					}
					SampleRate rate = SampleRate.findRate(sampleData[3]);
					System.out.println("New session found. Sample rate: "+rate);
					preamble = false;
					samples = new ArrayList<>();
					session = new AltimeterSession();
					session.setRate(rate);
					result.add(session);
				} else {
					samples.add(new AltimeterSample(sampleData));
				}
			}
			if(null != session) {
				session.setData(samples.toArray(new AltimeterSample[samples.size()]));
			}
		} catch(IOException e) {
			e.printStackTrace();
			result = null;
		}
		return result;
		
	}
	
	
	public static void main(String[] args) {
		List<AltimeterSession> sessions = readFile(new File("session20161211.fda"));
		for(AltimeterSession session : sessions) {
			int sr = session.getRate().samplesPerSecond();
			AltimeterSample[] data = session.getData();
			System.out.println();
			System.out.println("********************************");
			System.out.println("Session start: "+data.length+" samples at "+sr+" samples per second");
			double minA=Double.POSITIVE_INFINITY, maxA=Double.NEGATIVE_INFINITY, meanA=0;
			double minT=Double.POSITIVE_INFINITY, maxT=Double.NEGATIVE_INFINITY, meanT=0;
			int i = 0;
			for(AltimeterSample sample : data) {
				i++;
				double h1 = Physics.Pa2M(sample.getPressure());
				double h2 = Physics.keisanAltitude(sample.getPressure(), sample.getTemperature());
				double h3 = Physics.wikipediaAltitude(sample.getPressure(), sample.getTemperature());
				
				double h = h3;
				meanA += h;
				if(h < minA)
					minA = h;
				if(h > maxA)
					maxA = h;
				
				double t = sample.getTemperature();
				meanT += t;
				if(t < minT)
					minT = t;
				if(t > maxT)
					maxT = t;
				
				if(i%sr == 1) {
					System.out.printf("%5d s => %s (%.4f/%.4f/%.4f m)", i/sr, sample, h1, h2, h3);
				} else {
					System.out.printf("           %s (%.4f/%.4f/%.4f m)", sample, h1, h2, h3);
				}
				System.out.println();
			}
			System.out.println();
			System.out.printf("Altitude: min=%.4f; max=%.4f; mean=%.4f; var=%.4f", minA, maxA, meanA/i, maxA-minA);
			System.out.println();
			System.out.printf("Temperature: min=%.4f; max=%.4f; mean=%.4f; var=%.4f", minT, maxT, meanT/i, maxT-minT);
			System.out.println();
			System.out.println("********************************");
			System.out.println();
		}
	}

}
