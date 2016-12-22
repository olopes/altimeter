package org.psicover.altimeter;

import java.io.Serializable;
import java.util.Calendar;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;

public class AltimeterTimePeriod2 extends RegularTimePeriod implements Serializable {
	private static final long serialVersionUID = -6349245393675620158L;
	
	private static final int LAST_MILLIS_IN_SECOND = 999;
	private static final int FIRST_MILLIS_IN_SECOND = 0;
	
	private Second second;
	private final short increment;
	private short step;
    private long firstMillisecond;

	public AltimeterTimePeriod2(SampleRate rate) {
		this(1000/rate.samplesPerSecond(), 0, new Second());
	}
	
	private AltimeterTimePeriod2(int increment, int time, Second second) {
		this.second = second;
		this.increment = (short)increment;
		this.step = (short)time;
		this.firstMillisecond = second.getFirstMillisecond()+this.step;
	}

    /**
     * Returns the first millisecond of the second.  This will be determined
     * relative to the time zone specified in the constructor, or in the
     * calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The first millisecond of the second.
     *
     * @see #getLastMillisecond()
     */
    @Override
    public long getFirstMillisecond() {
        return this.firstMillisecond;
    }

    /**
     * Returns the last millisecond of the second.  This will be
     * determined relative to the time zone specified in the constructor, or
     * in the calendar instance passed in the most recent call to the
     * {@link #peg(Calendar)} method.
     *
     * @return The last millisecond of the second.
     *
     * @see #getFirstMillisecond()
     */
    @Override
    public long getLastMillisecond() {
        return this.firstMillisecond + this.increment-1L;
    }

    /**
     * Recalculates the start date/time and end date/time for this time period
     * relative to the supplied calendar (which incorporates a time zone).
     *
     * @param calendar  the calendar (<code>null</code> not permitted).
     *
     * @since 1.0.3
     */
    @Override
    public void peg(Calendar calendar) {
        this.firstMillisecond = getFirstMillisecond(calendar);
    }

    /**
     * Returns the second preceding this one.
     *
     * @return The second preceding this one.
     */
    @Override
    public RegularTimePeriod previous() {
        AltimeterTimePeriod2 result = null;
        if (this.step > FIRST_MILLIS_IN_SECOND) {
            result = new AltimeterTimePeriod2(this.increment, this.step - this.increment, this.second);
        }
        else {
        	Second previous = (Second) this.second.previous();
            if (previous != null) {
                result = new AltimeterTimePeriod2(this.increment, LAST_MILLIS_IN_SECOND, previous);
            }
        }
        return result;
    }

    /**
     * Returns the second following this one.
     *
     * @return The second following this one.
     */
    @Override
    public RegularTimePeriod next() {
        AltimeterTimePeriod2 result = null;
        if (this.step < LAST_MILLIS_IN_SECOND) {
            result = new AltimeterTimePeriod2(this.increment, this.step + this.increment, this.second);
        }
        else {
        	Second next = (Second) this.second.next();
            if (next != null) {
                result = new AltimeterTimePeriod2(this.increment, FIRST_MILLIS_IN_SECOND, next);
            }
        }
        return result;
    }

    /**
     * Returns a serial index number for the minute.
     *
     * @return The serial index number.
     */
    @Override
    public long getSerialIndex() {
        long secondIndex = this.second.getSerialIndex();
        return secondIndex * 1000L + this.step;
    }

    /**
     * Returns the first millisecond of the minute.
     *
     * @param calendar  the calendar/timezone (<code>null</code> not permitted).
     *
     * @return The first millisecond.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    @Override
    public long getFirstMillisecond(Calendar calendar) {
    	this.second.getFirstMillisecond(calendar);
    	calendar.set(Calendar.MILLISECOND, step);
        return calendar.getTimeInMillis();
    }

    /**
     * Returns the last millisecond of the second.
     *
     * @param calendar  the calendar/timezone (<code>null</code> not permitted).
     *
     * @return The last millisecond.
     *
     * @throws NullPointerException if <code>calendar</code> is
     *     <code>null</code>.
     */
    @Override
    public long getLastMillisecond(Calendar calendar) {
        return getFirstMillisecond(calendar) + this.step + this.increment - 1L;
    }

    /**
     * Returns an integer indicating the order of this Second object relative
     * to the specified
     * object: negative == before, zero == same, positive == after.
     *
     * @param o1  the object to compare.
     *
     * @return negative == before, zero == same, positive == after.
     */
    @Override
    public int compareTo(Object o1) {
        int result;

        // CASE 1 : Comparing to another Second object
        // -------------------------------------------
        if (o1 instanceof AltimeterTimePeriod2) {
        	AltimeterTimePeriod2 s = (AltimeterTimePeriod2) o1;
        	long t1 = this.getFirstMillisecond();
        	long t2 = s.getFirstMillisecond();
            if (t1 < t2) {
                return -1;
            }
            else if (t1 > t2) {
                return 1;
            }
            else {
                return 0;
            }
        }

        // CASE 2 : Comparing to another TimePeriod object
        // -----------------------------------------------
        else if (o1 instanceof RegularTimePeriod) {
            // more difficult case - evaluate later...
            result = 0;
        }

        // CASE 3 : Comparing to a non-TimePeriod object
        // ---------------------------------------------
        else {
            // consider time periods to be ordered after general objects
            result = 1;
        }

        return result;
    }

}
