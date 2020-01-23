package ch.pschatzmann.jflightcontroller4pi.control;

public interface IFilter {

	void add(double value);

	double getValue();

}