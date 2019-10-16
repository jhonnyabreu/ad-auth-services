package repository;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.ws.rs.NotFoundException;

import config.PropertiesReader;
import dto.User;

public class AdRepostitory {
	
	private static final String HOST = "host";
	
	private static final String USER_ATTR = "user-attribute-name";
	private static final String MAIL_ATTR = "mail-attribute-name";
	private static final String NAME_ATTR = "fullname-attribute-name";
	
	private static final String BASE_DN = "base-dn";
	
	private static final String USE_DOMAIN = "use-domain";
	private static final String DOMAIN = "domain";
	
	private static final String AD_CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	
	/**
	 * Do a login challenge on the Active Directory <br>
	 * The AD information must by properly configured in the properties file 
	 * 
	 * @param username  
	 * 			{@link String} user name
	 * @param password  
	 * 			{@link String} user password
	 * @return user data
	 * @throws NamingException if an error was found on Active Directory
	 * @throws NotFoundException if the properties file was not found
	 */
	public User doLogin(String username, String password) throws NamingException, NotFoundException  {
		
		Properties p = this.readProperties();
		
		String adServer = String.format(p.getProperty(HOST));
		
		//if domain config is enabled, concat to end of the username
		if ( p.getProperty(USE_DOMAIN, "false").equalsIgnoreCase("true") ) {
			username = username + "@" + p.getProperty(DOMAIN);
		}
		
		DirContext context = this.createContext(adServer, username, password);
		
		SearchResult result = this.searchUser( username, context, p );
		
		Attributes attrs = result.getAttributes();
		
		User user = new User();
		user.setName( this.readAttribute( attrs.get(p.getProperty(NAME_ATTR)) ) );
		user.setEmail( this.readAttribute( attrs.get(p.getProperty(MAIL_ATTR)) ) );
		user.setUsername( this.readAttribute( attrs.get( p.getProperty(USER_ATTR) ) ) );
		
		return user;
	}
	
	/**
	 * Read an attribute value from the AD Search Result
	 * @param a 
	 * 		 {@link String} the attribute bean
	 * @return the attribute value
	 * @throws NamingException if any error occurs from reading the value
	 */
	private String readAttribute( Attribute a ) throws NamingException {
		if ( a != null ) {
			return a.get().toString();
		}
		return null;
	}
	
	/**
	 * Search for an user on AD.
	 *
	 * @param username
	 *            {@link String} user login name.
	 * @param context
	 *            {@link DirContext} AD context.
	 * @param p
	 * 			{@link Properties} configuration properties 
	 * @return {@link SearchResult} search results
	 * @throws NamingException
	 *             If more than one user with the supplied account name was found
	 * @throws AuthenticationException
	 *             In case of no user found
	 */
	private SearchResult searchUser(String username, DirContext context, Properties p) throws NamingException {
		// Filter only users
		String searchFilter = String.format("(&(objectClass=user)(%s=%s))", p.getProperty(MAIL_ATTR), username);
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<SearchResult> results = context.search(p.getProperty(BASE_DN), searchFilter, searchControls);

		if (!results.hasMoreElements()) {
			throw new AuthenticationException("User not found.");
		}

		SearchResult searchResult = results.nextElement();
		if (results.hasMoreElements()) {
			throw new NamingException("More then one user with this account name found: '" + username + "' ("+ p.getProperty(USER_ATTR) +").");
		}
		return searchResult;
	}
	
	private DirContext createContext(String adServer, String username, String password) throws NamingException {
		Hashtable<String, String> environment = new Hashtable<String, String>(5);
		environment.put(Context.INITIAL_CONTEXT_FACTORY, AD_CTX_FACTORY);
		environment.put(Context.PROVIDER_URL, adServer);
		environment.put(Context.SECURITY_PRINCIPAL, username);
		environment.put(Context.SECURITY_CREDENTIALS, password);
		return new InitialDirContext(environment);
	}
	
	private Properties readProperties() throws NotFoundException {
		Properties p = PropertiesReader.getInstance().getProperties();
		return p;
	}

}
