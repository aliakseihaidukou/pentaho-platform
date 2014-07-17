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

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IThemeManager;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

/**
 * Resource manages themes for the platform
 * 
 *
 */
@Path( "/theme" )
public class ThemeResource extends AbstractJaxRSResource {

  public ThemeResource() {
  }

  private IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  /**
   * List the current supported themes in the platform
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/theme/list</b><br/>
   *  Use GET request type.<br/>
   *  Response content is json or xml based on request "accept" header(
   *  '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}' or
   *  '{@value javax.ws.rs.core.MediaType#APPLICATION_XML}').<br/>
   *  You should be logged in to the system in order to use the method.<br/>
   * </p>
   *
   * <p>Response example:
   * <pre>
   *   <b>JSON:</b>
   *   {@code
   *
   * {
   *  "theme": [
   *    {
   *      "id": "onyx",
   *      "name": "Onyx"
   *    },
   *    ...
   *  ]
   * }
   *  }
   *  <b>XML:</b>
   *  {@code
   *
   *  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *  <themes>
   *    <theme>
   *      <id>onyx</id>
   *      <name>Onyx</name>
   *    </theme>
   *    ...
   *  </themes>
   *  }
   * </pre>
   * </p>
   *
   * <p>Snippet using Jersey:
   * <pre>
   *   {@code
   *
   * import com.sun.jersey.api.client.Client;
   * import com.sun.jersey.api.client.GenericType;
   * import com.sun.jersey.api.client.WebResource;
   * import com.sun.jersey.api.client.config.DefaultClientConfig;
   * import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
   * import org.pentaho.platform.web.http.api.resources.Theme;
   * ...
   * public void testGetSystemThemes() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig() );
   *  client.addFilter( new HTTPBasicAuthFilter( "[user]", "[password]" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/theme/list" );
   *  final List<Theme> themes = resource.get( new GenericType<List<Theme>>() { } );
   *  for ( Theme theme : themes ) {
   *    // use the theme
   *  }
   * }
   * }
   * </pre>
   * </p>
   *
   * @return list of themes
   */
  @GET
  @Path( "/list" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public List<Theme> getSystemThemes() {
    ArrayList<Theme> themes = new ArrayList<Theme>();
    IThemeManager themeManager = PentahoSystem.get( IThemeManager.class );
    List<String> ids = themeManager.getSystemThemeIds();
    for ( String id : ids ) {
      org.pentaho.platform.api.ui.Theme theme = themeManager.getSystemTheme( id );
      if ( theme.isHidden() == false ) {
        themes.add( new Theme( id, theme.getName() ) );
      }
    }
    return themes;
  }

  /**
   * TODO: deny setting theme that doesn't present in the system - UI may be broken
   * Set the current theme to the one provided in this request
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/theme/set</b><br/>
   *  Use POST request type. Theme id is sent as raw text.<br/>
   *  Response content is 'text/plain' new active theme id.<br/>
   *  You should be logged in to the system in order to use the method.<br/>
   * </p>
   *
   * <p>Snippet using Jersey:
   * <pre>
   *   {@code
   *
   * import com.sun.jersey.api.client.Client;
   * import com.sun.jersey.api.client.WebResource;
   * import com.sun.jersey.api.client.config.DefaultClientConfig;
   * import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
   * ...
   * public void testSetTheme() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig() );
   *  client.addFilter( new HTTPBasicAuthFilter( "[user]", "[password]" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/theme/set" );
   *  final String newTheme = resource.post( String.class, "[id]" );
   *  // new theme is set
   * }
   * }
   * </pre>
   * </p>
   * 
   * @param theme (theme to be changed to)
   * 
   * @return new active theme
   */
  @POST
  @Path( "/set" )
  @Consumes( { WILDCARD } )
  @Produces( "text/plain" )
  public Response setTheme( String theme ) {
    getPentahoSession().setAttribute( "pentaho-user-theme", theme );
    IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
    settingsService.setUserSetting( "pentaho-user-theme", theme );
    return getActiveTheme();
  }

  /**
   * //TODO: using the snippet you will always get 'default-theme'. That is caused by login system('pentaho-user-theme'
   * //TODO: isn't set to session yet). Maybe this need to be changed.
   * //TODO: To get correct value, user need to be in active session(store cookies from previous response, see
   * //TODO: org.pentaho.platform.web.http.api.resources.UserSettingsResource#getUserSetting(java.lang.String))
   *
   * Return the name of the active theme
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/theme/active</b><br/>
   *  Use GET request type.<br/>
   *  Response content is 'text/plain' active theme id.<br/>
   *  You should be logged in to the system in order to use the method.<br/>
   * </p>
   *
   * <p>Snippet using Jersey:
   * <pre>
   *   {@code
   *
   * import com.sun.jersey.api.client.Client;
   * import com.sun.jersey.api.client.WebResource;
   * import com.sun.jersey.api.client.config.DefaultClientConfig;
   * import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
   * ...
   * public void testGetActiveTheme() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig() );
   *  client.addFilter( new HTTPBasicAuthFilter( "[user]", "[password]" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/theme/active" );
   *  final String theme = resource.get( String.class );
   *  // use the theme
   * }
   * }
   * </pre>
   * </p>
   * 
   * @return active theme
   */
  @GET
  @Path( "/active" )
  @Produces( "text/plain" )
  public Response getActiveTheme() {
    IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
    return Response.ok(
      StringUtils.defaultIfEmpty( (String) getPentahoSession().getAttribute( "pentaho-user-theme" ), settingsService
        .getUserSetting( "pentaho-user-theme", PentahoSystem.getSystemSetting( "default-theme", "onyx" ) )
          .getSettingValue() ) ).type( MediaType.TEXT_PLAIN ).build();
  }

}
