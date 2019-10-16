package config;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.NotFoundException;

import org.jboss.logging.Logger;


public class PropertiesReader {
	
	private static final Logger LOGGER = Logger.getLogger( PropertiesReader.class.getName() );
	
	//SINGLETON (currently not used)
	private static PropertiesReader _INSTANCE;
	
	public static PropertiesReader getInstance() throws NotFoundException {
			return new PropertiesReader();
	}
	
	
	private PropertiesReader() throws NotFoundException {
		this.readProperties();
	}
	
	private Properties properties;
	
	public Properties getProperties(){
		return this.properties;
	}
	
	private void readProperties() throws NotFoundException{
		try {
			//search the properties location (from parameter or current directory)
			
			String arquivo = System.getProperty("properties.file");
			if (arquivo == null) {
				//current directory
				File f = new File(System.getProperty("java.class.path"));
				File dir = f.getAbsoluteFile().getParentFile();
				String path = dir.toString();
				arquivo = path + File.separator + "ad-auth-service.properties";
			}
			
			LOGGER.info("Properties File: " + arquivo);
			
			File f = new File ( arquivo );
			if (f.exists()){
				properties = new Properties();
				properties.load(new FileInputStream(f));
			}
			else{
				throw new NotFoundException("Properties File NOT found ["+f.getAbsolutePath()+"]");
			}
		} catch (IOException e) {
			throw new NotFoundException("Error Reading Properties File: ["+e.getMessage()+"]", e);
		}
	}

}
