package org.psicover.altimeter.ui;

import java.io.Serializable;
import java.util.Calendar;

import org.jfree.data.time.RegularTimePeriod;
import org.psicover.altimeter.bean.SampleRate;

public class AltimeterTimePeriod extends RegularTimePeriod implements Serializable {
	private static final long serialVersionUID = -6349245393675620158L;
	private final long increment;
	private final long time;
	
	public AltimeterTimePeriod(SampleRate rate) {
		this(1000L/rate.samplesPerSecond(), 0);
	}
	
	private AltimeterTimePeriod(long increment, long time) {
		this.increment = increment;
		this.time = time;
	}

	@Override
	public int compareTo(Object o) {
		if(o instanceof AltimeterTimePeriod) {
			long otime = ((AltimeterTimePeriod)o).time;
			if(time > otime) return 1;
			else if (time == otime) return 0;
			else return -1;
		}
		return 0;
	}

	@Override
	public RegularTimePeriod previous() {
		if(this.time == 0)
			return null;
		return new AltimeterTimePeriod(this.increment, this.time-this.increment);
	}

	@Override
	public RegularTimePeriod next() {
		if(this.time+this.increment >= Integer.MAX_VALUE)
			return null;
		return new AltimeterTimePeriod(this.increment, this.time+this.increment);
	}

	@Override
	public long getSerialIndex() {
		return this.time;
	}

	@Override
	public void peg(Calendar calendar) {
		// do nothing?
	}

	@Override
	public long getFirstMillisecond() {
		return this.time;
	}

	@Override
	public long getFirstMillisecond(Calendar calendar) {
		return getFirstMillisecond();
	}

	@Override
	public long getLastMillisecond() {
		return this.time+this.increment-1L;
	}

	@Override
	public long getLastMillisecond(Calendar calendar) {
		return getLastMillisecond();
	}
}
