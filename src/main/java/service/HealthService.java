package service;

import java.util.Calendar;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import config.PropertiesReader;


@Path("/health")
public class HealthService {
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public Calendar ping() {
        return Calendar.getInstance();
    }
	
	@GET
	@Path("/debug")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties debug() throws Exception {
		Properties p = PropertiesReader.getInstance().getProperties();
		return p;
    }
	
	
	
}