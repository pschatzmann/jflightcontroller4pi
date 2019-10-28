package ch.pschatzmann.jflightcontroller4pi;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Starts the FlightController with a configuration file
 */
public class Main {
	private static Logger log = LoggerFactory.getLogger(Main.class);

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String configFileName = getConfigFileName(args);
		ApplicationContext context;
		// We use Spring so that we can configure the functionality
		if (new File("configFileName").exists())
			context = new FileSystemXmlApplicationContext(configFileName);
		else 
			context = new ClassPathXmlApplicationContext(configFileName);
		
		FlightController flightController = (FlightController) context.getBean("flightController");
		
		// just display the current mode as information
		log.info("The flight controller is in the following mode: '{}'",flightController.getMode());
		
		// shutdown all devices if we exit the program
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("Shutting down ...");
					flightController.shutdown();
					System.out.println("The aircraft has stopped!");

				} catch (Exception e) {
					Thread.currentThread().interrupt();
					log.error(e.getMessage(),e);
					System.exit(-1);
				}
			}
		});

		// startup flight controller 
		try {
			flightController.run();
		} catch (Exception e) {
			log.error(e.getMessage(),e);;
		}

	}

	private static String getConfigFileName(String[] args) {
		String config = "config.xml";
		if (args.length>0) {
			config = args[0];
		}
		return config;
	}
}
