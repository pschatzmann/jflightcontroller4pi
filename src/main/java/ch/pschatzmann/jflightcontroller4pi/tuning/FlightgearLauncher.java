package ch.pschatzmann.jflightcontroller4pi.tuning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IDevice;

/**
 * Launch Flightgear via the command line. If it is already running we just
 * reset it via telnet. Because this is not very reliable because flightgear
 * tends to crash or hang we need to recover from these situations as well.
 * 
 * @author pschatzmann
 *
 */
public class FlightgearLauncher {
	private static Logger log = LoggerFactory.getLogger(FlightgearLauncher.class);
	private String host = "localhost";
	private int port = 7002;
	private String startCommand = "fgfs --altitude=10000 --vc=100 --timeofday=noon --generic=socket,in,10,,7000,udp,my-io --generic=socket,out,10,,7001,udp,my-io --airport=LSGS --timeofday=noon --telnet=7002 --httpd=7003 ";
	private int maxWaitStart = 400; // in sec
	private int maxWaitRestart = 60; // in sec
	private ForkJoinPool forkJoinPool = new ForkJoinPool(1);
	private Process process;
	
	/**
	 * Default constructor
	 */
	public FlightgearLauncher() {
		log.info("FlightgearLauncher");
	}

	/**
	 * Restart the flight if flightgear has already been started otherwise we
	 * start it.
	 * 
	 * @return
	 */
	public boolean start() {
		if (restart()) {
			return true;
		}
		if (this.isRunning()) {
			this.kill();
		}
		return start1();
	}

	/**
	 * lauch flightgear via the command line
	 * 
	 * @return
	 */
	protected boolean start1() {
		try {
			log.info("start");
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.redirectErrorStream(true);
			processBuilder.command("bash", "-c", startCommand);
			log.info(startCommand);
			process = processBuilder.start();

			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			int count = 0;
			String line;
			while (count < maxWaitStart && process.isAlive()) {
				line = reader.readLine();
				if (line != null) {
					log.info(line);
					if (line.contains("Hobbs system started")) {
						Thread.sleep(2000);
						log.info("-> start success");
						return true;
					}

				}
				if (line == null && process.isAlive()) {
					Thread.sleep(1000);
					count++;
				}
			}
			log.info("-> start failed");
			return false;

		} catch (Exception ex) {
			log.info("-> start failed: {}", ex.getMessage());
			return false;
		}

	}

	/**
	 * Time limited restart. We fail if the restart is not returing within 60
	 * seconds. We need this because sometimes filightgear seems to hang and
	 * does not respond to user input any more
	 * 
	 * @return
	 */

	protected boolean restart() {
		boolean result = false;
		try {
			result = forkJoinPool.submit(() -> {
				return restart1();
			}).get(this.maxWaitRestart, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			log.info("restart has timed out!");
			result = false;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * Restart via telnet
	 * 
	 * @return
	 */
	protected boolean restart1() {
		TelnetClient telnet = new TelnetClient();
		try {
			log.info("restart");
			telnet.connect(host, port);

			OutputStream out = telnet.getOutputStream();
			// write command
			out.write(("run reposition" + System.lineSeparator()).getBytes());
			out.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
			String line = in.readLine();
			while (line != null) {
				log.info(line);
				if (line.contains("<completed>")) {
					log.info("-> restart success");
					return true;
				}
				line = in.readLine();
			}

			log.info("-> restart failed");
			return false;
		} catch (Exception ex) {
			log.info("-> restart failed: {}", ex.getMessage());
			return false;
		} finally {
			if (telnet != null) {
				try {
					telnet.disconnect();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}

		}
	}

	/**
	 * Checks if flightgear has already been started
	 * 
	 * @return
	 */
	protected boolean isRunning() {
		boolean result = process != null;
		log.info("isRunning ? is {}", result);
		return result;
	}

	/**
	 * Detrmines the process id
	 * 
	 * @return
	 * @throws IOException
	 */
	protected Collection<String> getProcessIDs() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		processBuilder.command("bash", "-c", "pgrep fgfs");
		Process process = processBuilder.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		Collection<String> result = new ArrayList();
		String line = reader.readLine();
		while (line!=null) {
			result.add(line);
			line = reader.readLine();
		}
		log.info("The process ids are: {} ", result.toString());
		process.destroy();
		return result;
	}

	/**
	 * Kills the flightgear process
	 */
	protected void kill() {
		try {
			log.info("kill");
			if (process!=null) {
				process.destroyForcibly().waitFor();
				log.info("-> the process has been killed");
			}
			
			Collection ids = getProcessIDs();
			if (!ids.isEmpty()) {
				log.warn("We kill the following processes {}",ids.toString());
				// this should not be necessary
				for (String id : getProcessIDs()) {
					String cmd = "kill "+id;
					log.info(cmd);
		            Process p = Runtime.getRuntime().exec(cmd);
				}
			}
			
		} catch (Exception ex) {
			log.error("Could not kill flightgear", ex);
		}
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the telnetPort
	 */
	public int getTelnetPort() {
		return port;
	}

	/**
	 * @param telnetPort
	 *            the telnetPort to set
	 */
	public void setTelnetPort(int telnetPort) {
		this.port = telnetPort;
	}

	/**
	 * @return the startCommand
	 */
	public String getStartCommand() {
		return startCommand;
	}

	/**
	 * @param startCommand
	 *            the startCommand to set
	 */
	public void setStartCommand(String startCommand) {
		this.startCommand = startCommand;
	}

	/**
	 * @return the maxWaitStart
	 */
	public int getMaxWaitStart() {
		return maxWaitStart;
	}

	/**
	 * @param maxWaitStart the maxWaitStart to set
	 */
	public void setMaxWaitStart(int maxWaitStart) {
		this.maxWaitStart = maxWaitStart;
	}

	/**
	 * @return the maxWaitRestart
	 */
	public int getMaxWaitRestart() {
		return maxWaitRestart;
	}

	/**
	 * @param maxWaitRestart the maxWaitRestart to set
	 */
	public void setMaxWaitRestart(int maxWaitRestart) {
		this.maxWaitRestart = maxWaitRestart;
	}

}
