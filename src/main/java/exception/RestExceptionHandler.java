package exception;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RestExceptionHandler implements ExceptionMapper<Exception> {
	
	@Override
    public Response toResponse(Exception e) {        
		
		//print no console
		e.printStackTrace();
		
		return serverError(e.getMessage()).build();
		
    }
	
	public static ResponseBuilder serverError(String message) {
		ResponseBuilder builder = Response.serverError();
		builder.header("X-Message", message);
		builder.type(MediaType.APPLICATION_JSON);
		builder.entity(createBody(Status.INTERNAL_SERVER_ERROR.getStatusCode(), message));
		return builder;
	}
	
	private static ResponseErrorBody createBody(int status, String message) {
		ResponseErrorBody body = new ResponseErrorBody();
		body.setStatus(status);
		body.setMessage(message);
		return body;
	}

}
