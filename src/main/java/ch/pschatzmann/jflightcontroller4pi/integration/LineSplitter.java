package ch.pschatzmann.jflightcontroller4pi.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import ch.pschatzmann.jflightcontroller4pi.protocols.InputSerialWithEvents;

/**
 * The serial interface does not return full lines. So we need to split them
 * ourself by adding the remainder from the last read...
 * 
 * @author pschatzmann
 *
 */
public class LineSplitter {
	private static Logger log = LoggerFactory.getLogger(LineSplitter.class);
	private List<String> result = new ArrayList();
	private String lastLine="";
	private static String empty[] = {};
	
	public String[] split(String input) {
		if (!StringUtils.isEmpty(input)) {
			String[] result = (lastLine+input).split("\n");
			if (log.isDebugEnabled())
				log.debug("Split into {} Strings",Arrays.asList(result).toString());
			
			if (input.endsWith("\n") || input.endsWith("\r")) {
				lastLine = "";
				return result;
			} else {
				lastLine = result[result.length-1];
				return Arrays.copyOfRange(result, 0, result.length-1);
			}		
		}
		return empty;
	}
	
	public String getLastLine() {
		return this.lastLine;
	}
	

}
