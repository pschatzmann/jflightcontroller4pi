package ch.pschatzmann.jflightcontroller4pi.guidence.navigation;

/**
 * Parser for GPS NMEA Sentences
 * @author pschatzmann
 *
 */
public interface INMEASentenceParser {
	public boolean parse(String [] tokens, GPSPosition position);
}
