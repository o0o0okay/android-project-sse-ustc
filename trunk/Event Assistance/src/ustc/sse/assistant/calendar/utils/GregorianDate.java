/**
 * 
 */
package ustc.sse.assistant.calendar.utils;

/**
 * 
 * This date represent Gregorian date.
 * @author 李健
 *
 */
public class GregorianDate extends AbstractDate {

	public GregorianDate(Integer gregorianYear, Integer gregorianMonth, Integer gregorianDay, Integer lunarDay, Integer lunarMonth, Integer lunarYear) {
		super(gregorianYear, gregorianMonth, gregorianDay, lunarDay, lunarMonth, lunarYear);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((gregorianDay == null) ? 0 : gregorianDay.hashCode());
		result = prime * result
				+ ((gregorianMonth == null) ? 0 : gregorianMonth.hashCode());
		result = prime * result
				+ ((gregorianYear == null) ? 0 : gregorianYear.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		AbstractDate other = (AbstractDate) obj;
		if (gregorianDay == null) {
			if (other.gregorianDay != null)
				return false;
		} else if (!gregorianDay.equals(other.gregorianDay))
			return false;
		if (gregorianMonth == null) {
			if (other.gregorianMonth != null)
				return false;
		} else if (!gregorianMonth.equals(other.gregorianMonth))
			return false;
		if (gregorianYear == null) {
			if (other.gregorianYear != null)
				return false;
		} else if (!gregorianYear.equals(other.gregorianYear))
			return false;
		return true;
	}


}