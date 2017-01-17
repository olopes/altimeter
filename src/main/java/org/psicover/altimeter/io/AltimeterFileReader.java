package org.psicover.altimeter.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.psicover.altimeter.LogFactory;
import org.psicover.altimeter.Physics;
import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.bean.SampleRate;

/**
 * Read and write FDA/HKA files
 * @author oscar
 *
 */
public class AltimeterFileReader {
	private final static Logger logger = LogFactory.getLogger(AltimeterFileReader.class);

	private static String signatureAsHex(byte[] signature) {
		StringBuilder sb = new StringBuilder();
		for(byte c : signature) {
			int b = c & 0xff;
			sb.append(Integer.toHexString(b>>4)).append(Integer.toHexString(b&0xf));
		}
		return sb.toString();
	}

	public static AltimeterFile readFile(File f) throws AltimeterIOException {
		
		List<AltimeterSession> result = new ArrayList<>();
		Pattern sigPatt = Pattern.compile("070fda1000ca(..)00", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
		
		try (FileInputStream in = new FileInputStream(f)){
			byte []signature = new byte[8];
			int r = 0;
			r = in.read(signature);
			if(r != signature.length) throw new IOException("Could not read file signature"); //$NON-NLS-1$
			// validate file signature
			String hexSig = signatureAsHex(signature);
			logger.fine("Signature: "+hexSig); //$NON-NLS-1$
			Matcher matcher = sigPatt.matcher(hexSig);
			if(matcher.matches()) {
				logger.fine("Found good signature type: "+matcher.group(1)); //$NON-NLS-1$
			} else {
				throw new IOException("Unknown signature type: "+hexSig); //$NON-NLS-1$
			}

			byte []header = new byte[4];
			r = in.read(header);
			if(r != header.length) throw new IOException("Could not read file header"); //$NON-NLS-1$
			
			// calculate file size
			int size = (header[1]&0xff)-2; // why??
			size = size << 8 | (header[2]&0xff);
			size = size << 8 | (header[3]&0xff);
			logger.fine("File size: "+size); //$NON-NLS-1$
			
			boolean preamble = true;
			byte [] sampleData = new byte[4];
			List<AltimeterSample> samples = new ArrayList<>();
			AltimeterSession session = null;
			float time = 0;
			float tIncr = 0;
			// these will be useful to estimate chart ranges
			int minPa = Integer.MAX_VALUE;
			int maxPa = Integer.MIN_VALUE;
			int minTemp = Integer.MAX_VALUE;
			int maxTemp = Integer.MIN_VALUE;
			double minAlt = 10000;
			double maxAlt = -10000;
			while((r = in.read(sampleData))>0) {
				if(sampleData[0] == -1 && sampleData[1] == -1 && sampleData[2] == -1 && sampleData[3] == -1) {
					preamble = true;
				} else if(preamble) {
					if(null != session) {
						session.setData(samples.toArray(new AltimeterSample[samples.size()]));
					}
					SampleRate rate = SampleRate.findRate(sampleData[3]);
					tIncr = 1.0f/rate.samplesPerSecond();
					time = 0;
					logger.fine("New session found. Sample rate: "+rate); //$NON-NLS-1$
					preamble = false;
					samples = new ArrayList<>();
					session = new AltimeterSession();
					session.setLimits(minPa, maxPa, minTemp, maxTemp, minAlt, maxAlt);
					session.setRate(rate);
					result.add(session);
					
					// reset limits;
					minPa = Integer.MAX_VALUE;
					maxPa = Integer.MIN_VALUE;
					minTemp = Integer.MAX_VALUE;
					maxTemp = Integer.MIN_VALUE;
					minAlt = 10000;
					maxAlt = -10000;
				} else {
					AltimeterSample sample = new AltimeterSample(sampleData, time);
					samples.add(sample);
					time += tIncr;
					
					// check limits;
					if(minPa < sample.getPressure())
						minPa = sample.getPressure();
					if(maxPa > sample.getPressure())
						maxPa = sample.getPressure();
					
					if(minTemp < sample.getTemperature())
						minTemp = sample.getTemperature();
					if(maxTemp > sample.getTemperature())
						maxTemp = sample.getTemperature();
					
					if(maxAlt < sample.getAltitude())
						maxAlt = sample.getAltitude();
					if(maxAlt > sample.getAltitude())
						maxAlt = sample.getAltitude();
				}
			}
			if(null != session) {
				session.setData(samples.toArray(new AltimeterSample[samples.size()]));
			}
		} catch(IOException e) {
			result = null;
			throw new AltimeterIOException(e);
		}
		return new AltimeterFile(f.getName(), result);
	}
	
	
	public static void main(String[] args) throws Exception {
		AltimeterFile altimeterFile = readFile(new File("session20161211.fda")); //$NON-NLS-1$
		for(AltimeterSession session : altimeterFile.getSessions()) {
			int sr = session.getRate().samplesPerSecond();
			AltimeterSample[] data = session.getData();
			System.out.println();
			System.out.println("********************************"); //$NON-NLS-1$
			System.out.println("Session start: "+data.length+" samples at "+sr+" samples per second"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
			double minA=Double.POSITIVE_INFINITY, maxA=Double.NEGATIVE_INFINITY, meanA=0;
			double minT=Double.POSITIVE_INFINITY, maxT=Double.NEGATIVE_INFINITY, meanT=0;
			int i = 0;
			for(AltimeterSample sample : data) {
				i++;
				double h2 = Physics.keisan.convert(sample.getPressure(), sample.getTemperature());
				double h3 = Physics.wiki.convert(sample.getPressure(), sample.getTemperature());
				
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
					System.out.printf("%5d s => %s (%.4f/%.4f m)", i/sr, sample, h2, h3); //$NON-NLS-1$
				} else {
					System.out.printf("           %s (%.4f/%.4f m)", sample, h2, h3); //$NON-NLS-1$
				}
				System.out.println();
			}
			System.out.println();
			System.out.printf("Altitude: min=%.4f; max=%.4f; mean=%.4f; var=%.4f", minA, maxA, meanA/i, maxA-minA); //$NON-NLS-1$
			System.out.println();
			System.out.printf("Temperature: min=%.4f; max=%.4f; mean=%.4f; var=%.4f", minT, maxT, meanT/i, maxT-minT); //$NON-NLS-1$
			System.out.println();
			System.out.println("********************************"); //$NON-NLS-1$
			System.out.println();
		}
	}

}
