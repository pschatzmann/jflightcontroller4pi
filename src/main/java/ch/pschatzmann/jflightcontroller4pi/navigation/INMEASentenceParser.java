package ch.pschatzmann.jflightcontroller4pi.navigation;

public interface INMEASentenceParser {
	public boolean parse(String [] tokens, GPSPosition position);
}
