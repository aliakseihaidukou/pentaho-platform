/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * This resource manages the user settings of the platform
 * 
 *
 */
@Path( "/user-settings" )
public class UserSettingsResource extends AbstractJaxRSResource {

  public UserSettingsResource() {
  }

  private IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  /**
   * Retrieve the global settings and the user settings for the current user
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/user-settings/list</b><br/>
   *  Use GET request type.<br/>
   *  Response content is json or xml based on request "accept" header(
   *  '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}' or
   *  '{@value javax.ws.rs.core.MediaType#APPLICATION_XML}').<br/>
   *  You should be logged in to the system in order to use the method.<br/>
   * </p>
   *
   * <pre> Response examples:
   * <b>JSON:</b>
   * {@code {
   *    "setting": [
   *      {
   *        "name": "recent",
   *        "value": "[{\"fullPath\":\"/home/admin/1.prpti\", \"title\":\"1\", \"lastUse\":1404908321055}]"
   *      },
   *      {
   *        "name": "MANTLE_SHOW_NAVIGATOR",
   *        "value": "false"
   *      }
   *    ]
   *  }
   * }
   *
   * <b>XML:</b>
   * {@code <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *   <settings>
   *     <setting>
   *       <name>recent</name>
   *       <value>[{&quot;fullPath&quot;:&quot;/home/admin/1.prpti&quot;, &quot;title&quot;:&quot;1&quot;, &quot;lastUse&quot;:1404908321055}]</value>
   *     </setting>
   *     <setting>
   *       <name>MANTLE_SHOW_NAVIGATOR</name>
   *       <value>false</value>
   *     </setting>
   *   </settings>
   * }
   *
   * Snippet using Jersey:
   * {@code
   *
   * ...
   * import com.sun.jersey.api.client.Client;
   * import com.sun.jersey.api.client.GenericType;
   * import com.sun.jersey.api.client.WebResource;
   * import com.sun.jersey.api.client.config.DefaultClientConfig;
   * import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
   * import org.pentaho.platform.web.http.api.resources.Setting;
   *
   * import java.util.List;
   *
   * ...
   *
   * public void test() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig(  ) );
   *  client.addFilter( new HTTPBasicAuthFilter( "[name]", "[password]" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/user-settings/list" );
   *  List<Setting> settings = resource.get( new GenericType<List<Setting>>() {} );
   *  for ( Setting setting : settings ) {
   *    // use the settings
   *  }
   * }
   * }
   * </pre>
   * @return list of settings for the platform
   */
  @GET
  @Path( "/list" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public ArrayList<Setting> getUserSettings() {
    try {
      IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
      ArrayList<IUserSetting> userSettings = (ArrayList<IUserSetting>) settingsService.getUserSettings();

      ArrayList<Setting> settings = new ArrayList<Setting>();
      for ( IUserSetting userSetting : userSettings ) {
        settings.add( new Setting( userSetting.getSettingName(), userSetting.getSettingValue() ) );
      }

      return settings;
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Retrieve a particular user setting for the current user<br/>
   * TODO: UserSettingService#getUserSetting(java.lang.String, java.lang.String) checks for
   * PentahoSessionHolder.getSession().getAttribute("SPRING_SECURITY_CONTEXT") which is not defined at the first
   * request, but the user's authentication data presents in SecurityContextHolder.getContext(). Maybe we need to rewrite
   * the code.BTW "SPRING_SECURITY_CONTEXT" isn't checked in setUserSetting.
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/user-settings/[setting]</b><br/>
   *  Use GET request type.<br/>
   *  Response content is {@code 'text/plain'}, is is value of the setting or empty if this setting doesn't exist
   *  for current user.<br/>
   *  You should be logged in to the system in order to use the method.<br/>
   * </p>
   *
   * <p>Snippet using Jersey:
   * <pre>
   * {@code
   *
   *  public void test() {
   *    final String baseUrl = "http://[host]:[port]/[webapp]/";
   *    Client client = Client.create( new DefaultClientConfig(  ) );
   *    client.addFilter( new HTTPBasicAuthFilter( "[name]", "[password]" ) );
   *    //store cookies for authorization(keep the session)
   *    client.addFilter( new ClientFilter() {
   *      private List<Object> cookies;
   *      @Override
   *      public ClientResponse handle( ClientRequest request ) throws ClientHandlerException {
   *        if ( cookies != null ) {
   *          request.getHeaders().put( "Cookie", cookies );
   *        }
   *        ClientResponse response = getNext().handle( request );
   *        // copy cookies
   *        if ( response.getCookies() != null ) {
   *          if ( cookies == null ) {
   *            cookies = new ArrayList<Object>();
   *          }
   *          // A simple addAll just for illustration (should probably check for duplicates and expired cookies)
   *          cookies.addAll( response.getCookies() );
   *        }
   *        return response;
   *      }
   *    } );
   *    final WebResource resource = client.resource( baseUrl + "api/user-settings/[setting]" );
   *    //first request for authorization
   *    resource.get( String.class );
   *    String setting = resource.get( String.class );
   *    // use the setting
   *  }
   *     }
   * </pre>
   * </p>
   *
   * @param setting (Name of the setting)
   * 
   * @return value of the setting for the user or empty response
   */
  @GET
  @Path( "{setting : .+}" )
  public Response getUserSetting( @PathParam( "setting" ) String setting ) {
    IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
    IUserSetting userSetting = settingsService.getUserSetting( setting, null );
    return Response.ok( userSetting != null ? userSetting.getSettingValue() : null ).build();
  }

  /**
   * Save the value of a particular setting for the current user
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/user-settings/[setting]</b><br/>
   *  Use POST request type.<br/>
   *  Response content is {@code 'text/plain'}, is is value of the settingValue.<br/>
   *  You should be logged in to the system in order to use the method.<br/>
   * </p>
   *
   * <p>Snippet using Jersey:
   * <pre>
   *   {@code
   *
   * public void testSetUserSetting() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig(  ) );
   *  client.addFilter( new HTTPBasicAuthFilter( "admin", "password" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/user-settings/[setting]" );
   *  resource.post( "[value]" );
   *  // the setting is set
   * }
   * }
   * </pre>
   * </p>
   *
   * @param setting  (Setting name)
   * @param settingValue   (Value of the setting)
   * 
   * @return settingValue
   */
  @POST
  @Path( "{setting : .+}" )
  public Response setUserSetting( @PathParam( "setting" ) String setting, String settingValue ) {
    IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
    settingsService.setUserSetting( setting, settingValue );
    return Response.ok( settingValue ).build();
  }

}
