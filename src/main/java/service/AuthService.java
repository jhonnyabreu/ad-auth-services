package service;

import java.util.Map;

import javax.naming.AuthenticationException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import dto.User;
import exception.ResponseErrorBody;
import repository.AdRepostitory;


@Path("/auth")
public class AuthService {
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response login( Map<String,String> payload ) {
		
		String username = payload.get("username");
		String password = payload.get("password");
		
		System.out.println( "\n\n\n");
		
		System.out.println( username );
		System.out.println( password );

		System.out.println( "\n\n\n");
		
		User user = null;
		try {
			user = new AdRepostitory().doLogin(username, password);
			
		} catch (Exception e) {
			//return a json info of the exception
			ResponseErrorBody error = this.createException(e);
			return Response.status(error.getStatus()).entity(error).build();
		}
		
		if (user != null) {
			return Response.ok(user).build();
		}
		else {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
	}
	
	/**
	 * Create a REST response from an Exception <br>
	 * This method parse the exception, and if an AuthorizationException is found,
	 * parse the message for specific error codes
	 *  
	 * @param ex  
	 * 		{@link Exception} The exception from repository
	 * @return json response for the given exception
	 */
	private ResponseErrorBody createException( Exception ex ) {
		ResponseErrorBody body = new ResponseErrorBody();

		if (ex instanceof AuthenticationException) {
			//verify if error is code 49 (Users are unable to log in)
			//ref: https://confluence.atlassian.com/stashkb/ldap-error-code-49-317195698.html
			if ( ex.getMessage().contains("LDAP: error code 49") ){
				body.setStatus(Status.UNAUTHORIZED.getStatusCode());
				
				//52e
				if ( ex.getMessage().contains("data 52e") ) {
					body.setMessage("invalid credentials");
				}
				//530
				else if ( ex.getMessage().contains("data 530") ) {
					body.setMessage("not permitted to logon at this time");
				}
				//531
				else if ( ex.getMessage().contains("data 531") ) {
					body.setMessage("not permitted to logon at this workstation");
				}
				//532
				else if ( ex.getMessage().contains("data 532") ) {
					body.setMessage("password expired");
				}
				//533
				else if ( ex.getMessage().contains("data 533") ) {
					body.setMessage("account disabled");
				}
				//701
				else if ( ex.getMessage().contains("data 701") ) {
					body.setMessage("account expired");
				}
				//773
				else if ( ex.getMessage().contains("data 773") ) {
					body.setMessage("user must reset password");
				}
				//775
				else if ( ex.getMessage().contains("data 775") ) {
					body.setMessage("user account locked");
				}
			}
		}
		
		else {
			body.setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
			body.setMessage(ex.getMessage());
		}
		
		return body;
	}

}
