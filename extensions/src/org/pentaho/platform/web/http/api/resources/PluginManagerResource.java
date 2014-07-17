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
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.ui.xul.XulOverlay;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path( "/plugin-manager/" )
public class PluginManagerResource {

  private static final String NEW_TOOLBAR_BUTTON_SETTING = "new-toolbar-button"; //$NON-NLS-1$

  public PluginManagerResource() {
  }

  /**
   * Retrieve the list of XUL overlays for the provided id
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/plugin-manager/overlays?id=[id]</b><br/>
   *  Use GET request type. Only '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}' value is valid
   *  for "Accept" header.<br/>
   *  Response content is '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}', is is array of overlays for [id]
   *  parameter or all available overlays if the parameter is empty.<br/>
   *  You should be logged in to the system in order to use the method.<br/>
   * </p>
   *
   * <p>Response example:
   * <pre>
   *   {@code
   *
   * {
   *  "overlay": [
   *    {
   *      "id": "dataaccess",
   *      "priority": "9999",
   *      "resourceBundleUri": "api/repos/data-access/resources/messages/messages",
   *      "source": "<overlay id=\"dataaccess\" resourcebundle=\"api/repos/data-access/resources/messages/messages\" loadatstart=\"false\">\n\t\t\t<menubar id=\"filemenu\">\n\t\t\t    <menuitem id=\"manageDatasourceItem\" insertafter=\"openMenuItem\" label=\"${manageDatasourceEllipsis}\" js-command=\"window.top.pho.showDatasourceManageDialog(window.top.datasourceEditorCallback)\"/>\n\t\t\t</menubar>\n\t\t\t<menubar id=\"newmenu\">\n\t\t\t\t<menuitem id=\"newDatasourceItem\" label=\"${newDatasourceEllipsis}\" js-command=\"window.top.pho.openDatasourceEditor(window.top.datasourceEditorCallback)\"/>\t\t\t\t\n\t\t\t</menubar>\n\t    </overlay>"
   *    }
   *  ]
   * }
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
   * import org.pentaho.platform.web.http.api.resources.Overlay;
   * ...
   * public void testGetOverlays() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig() );
   *  client.addFilter( new HTTPBasicAuthFilter( "[user]", "[password]" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/plugin-manager/overlays?id=[id]" );
   *  final List<Overlay> overlays = resource.get( new GenericType<List<Overlay>>() { } );
   *  for ( Overlay overlay : overlays ) {
   *    // use the overlay
   *  }
   * }
   * }
   * </pre>
   * </p>
   *
   * @param id - overlay ID, may be absent
   * @return list of {@link org.pentaho.platform.web.http.api.resources.Overlay}
   */
  @GET
  @Path( "/overlays" )
  @Produces( { APPLICATION_JSON } )
  public List<Overlay> getOverlays( @QueryParam( "id" ) @DefaultValue( "" ) String id ) {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() ); //$NON-NLS-1$
    List<XulOverlay> overlays = pluginManager.getOverlays();
    ArrayList<Overlay> result = new ArrayList<Overlay>();
    for ( XulOverlay overlay : overlays ) {
      if ( !id.isEmpty() && !overlay.getId().equals( id ) ) {
        continue;
      }
      Overlay tempOverlay =
          new Overlay( overlay.getId(), overlay.getOverlayUri(), overlay.getSource(), overlay.getResourceBundleUri(),
              overlay.getPriority() );
      result.add( tempOverlay );
    }
    return result;
  }

  /**
   * Retrieve the list of plugin perspective in the platform
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/plugin-manager/perspectives</b><br/>
   *  Use GET request type. Only '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}' value is valid
   *  for "Accept" header.<br/>
   *  Response content is '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}', is is array of perspectives.<br/>
   *  You should be logged in to the system in order to use the method. Count of perspectives may vary 
   *  for different user roles.<br/>
   * </p>
   *
   * <p>Response example:
   * <pre>
   *   {@code
   *
   * {
   *  "pluginPerspective": [
   *    {
   *      "contentUrl": "mantle/home",
   *      "id": "home.perspective",
   *      "layoutPriority": "-2",
   *      "resourceBundleUri": "content/default-plugin/resources/messages/messages",
   *      "title": "${home}"
   *    },
   *    ...
   *  ]
   * }
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
   * import org.pentaho.platform.web.http.api.resources.PluginPerspective;
   * ...
   * public void testGetPerspectives() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig() );
   *  client.addFilter( new HTTPBasicAuthFilter( "[user]", "[password]" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/plugin-manager/perspectives" );
   *  final List<PluginPerspective> perspectives = resource.get( new GenericType<List<PluginPerspective>>() { } );
   *  for ( PluginPerspective perspective : perspectives ) {
   *    // use the perspective
   *  }
   * }
   * }
   * </pre>
   * </p>
   * 
   * @return list of {@link org.pentaho.platform.web.http.api.resources.PluginPerspective}
   */
  @GET
  @Path( "/perspectives" )
  @Produces( { APPLICATION_JSON } )
  public ArrayList<PluginPerspective> getPluginPerpectives() {
    IPluginPerspectiveManager manager =
        PentahoSystem.get( IPluginPerspectiveManager.class, PentahoSessionHolder.getSession() ); //$NON-NLS-1$

    ArrayList<PluginPerspective> perspectives = new ArrayList<PluginPerspective>();

    for ( IPluginPerspective perspective : manager.getPluginPerspectives() ) {
      PluginPerspective pp = new PluginPerspective();
      pp.setId( perspective.getId() );
      pp.setTitle( perspective.getTitle() );
      pp.setContentUrl( perspective.getContentUrl() );
      pp.setLayoutPriority( perspective.getLayoutPriority() );
      pp.setRequiredSecurityActions( perspective.getRequiredSecurityActions() );
      pp.setResourceBundleUri( perspective.getResourceBundleUri() );
      if ( perspective.getOverlays() != null ) {
        ArrayList<Overlay> safeOverlays = new ArrayList<Overlay>();
        for ( XulOverlay orig : perspective.getOverlays() ) {
          Overlay tempOverlay =
              new Overlay( orig.getId(), orig.getOverlayUri(), orig.getSource(), orig.getResourceBundleUri(), orig
                  .getPriority() );
          safeOverlays.add( tempOverlay );
        }
        pp.setOverlays( safeOverlays );
      }
      perspectives.add( pp );
    }

    return perspectives;
  }

  /**
   * Retrieve the list of registered plugin IDs
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/plugin-manager/ids</b><br/>
   *  Use GET request type.Only '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}' value is valid
   *  for "Accept" header.<br/>
   *  Response content is '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}', is is value of the settingValue.<br/>
   *  You should be logged in to the system and have
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction#NAME}',
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction#NAME}' and
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction#NAME}' privileges
   *  in order to use the method.
   * </p>
   *
   * <p>Response example:
   * <pre>
   *   {@code
   *
   *   {
   *      "strings": [
   *        "pentaho-interactive-reporting",
   *        "url",
   *        "data-access",
   *        "Enterprise Flash Charting",
   *        "admin-plugin",
   *        "cgg",
   *        "xaction",
   *        "test-plugin-perspective",
   *        "jpivot",
   *        "pdi-platform-plugin",
   *        "pentaho-cdf",
   *        "BI Server default plugin",
   *        "analyzer",
   *        "pentaho-geo",
   *        "common-ui",
   *        "reporting",
   *        "pentaho-mobile"
   *      ]
   *    }
   *  }
   * </pre>
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
   * import org.pentaho.platform.web.http.api.resources.StringListWrapper;
   * ...
   * public void testGetPluginIds() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig(  ) );
   *  client.addFilter( new HTTPBasicAuthFilter( "[user]", "[password]" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/plugin-manager/ids" );
   *  final StringListWrapper listWrapper = resource.get( StringListWrapper.class );
   *  for ( String pluginID : listWrapper.getStrings() ) {
   *    //use pluginID
   *  }
   * }
   * }
   * </pre>
   * </p>
   * @return list of ids
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_JSON } )
  public Response getPluginIds() {
    if ( canAdminister() ) {
      IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() );
      return Response.ok( new StringListWrapper( pluginManager.getRegisteredPlugins() ), MediaType.APPLICATION_JSON )
          .build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Retrieve the plugins setting with a provided setting name. This will search the plugins's settings.xml and return
   * the selected setting. If nothing found, the response will be empty.
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/plugin-manager/[pluginId]/setting/[settingName]</b><br/>
   *  Use GET request type. Only '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}' value is valid
   *  for "Accept" header.<br/>
   *  Response content is '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}', is is value of the settingValue.<br/>
   *  You should be logged in to the system and have
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction#NAME}',
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction#NAME}' and
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction#NAME}' privileges
   *  in order to use the method.<br/>
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
   * public void testGetPluginSetting() {
   *  final String baseUrl = "http://[host]:[port]/[webapp]/";
   *  Client client = Client.create( new DefaultClientConfig(  ) );
   *  client.addFilter( new HTTPBasicAuthFilter( "[user]", "[password]" ) );
   *  final WebResource resource = client.resource( baseUrl + "api/plugin-manager/[pluginId]/setting/[settingName]" );
   *  final String setting = resource.get( String.class );
   *  //use the setting
   * }
   * }
   * </pre>
   * </p>
   *
   * @param pluginId (Plugin ID for the setting being searched)
   * @param settingName (Setting name of a selected plugin)
   * @return Value of the setting or empty response
   */
  @GET
  @Path( "/{pluginId}/setting/{settingName}" )
  @Produces( { APPLICATION_JSON } )
  public String getPluginSetting( @PathParam( "pluginId" ) String pluginId,
      @PathParam( "settingName" ) String settingName ) {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() ); //$NON-NLS-1$
    return (String) pluginManager.getPluginSetting( pluginId, settingName, null );
  }

  /**
   * Retrieve the list of setting of a selected setting name from all registered plugins. 
   *
   * <p>
   *  Endpoint address is <b>http://[host]:[port]/[webapp]/api/plugin-manager/settings/[settingName]</b><br/>
   *  Use GET request type. Only '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}' value is valid
   *  for "Accept" header.<br/>
   *  Response content is '{@value javax.ws.rs.core.MediaType#APPLICATION_JSON}'.<br/>
   *  You should be logged in to the system and have
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction#NAME}',
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction#NAME}' and
   *  '{@value org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction#NAME}' privileges
   *  in order to use the method(except '{@value PluginManagerResource#NEW_TOOLBAR_BUTTON_SETTING}' setting).<br/>
   * </p>
   *
   * <pre>
   *   Response example for '{@value PluginManagerResource#NEW_TOOLBAR_BUTTON_SETTING}' setting:
   *   {@code {
   *      "Item": [
   *        {
   *          "@type": "setting",
   *          "name": "pentaho-interactive-reporting",
   *          "value": "2,newInteractiveReport,interactiveReport,api/repos/pentaho-interactive-reporting/prpti.new"
   *        },
   *        {
   *          "@type": "setting",
   *          "name": "analyzer",
   *          "value": "1,newAnalyzer,analyzer,api/repos/xanalyzer/service/selectSchema"
   *        }
   *      ]
   *    }}
   * </pre>
   *
   * @param settingName (name of the plugin setting)
   * @return list of <code> Setting </code>
   */
  @GET
  @Path( "/settings/{settingName}" )
  @Produces( { APPLICATION_JSON } )
  public Response getPluginSettings( @PathParam( "settingName" ) String settingName ) {
    // A non-admin still require this setting. All other settings should be admin only
    if ( !NEW_TOOLBAR_BUTTON_SETTING.equals( settingName ) ) {
      if ( !canAdminister() ) {
        return Response.status( UNAUTHORIZED ).build();
      }
    }
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() ); //$NON-NLS-1$
    ArrayList<Setting> settings = new ArrayList<Setting>();
    for ( String id : pluginManager.getRegisteredPlugins() ) {
      Setting s = new Setting( id, (String) pluginManager.getPluginSetting( id, settingName, null ) );
      if ( !StringUtils.isEmpty( s.getValue() ) ) {
        settings.add( s );
      }
    }
    return Response.ok( new JaxbList<Setting>( settings ), MediaType.APPLICATION_JSON ).build();
  }

  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }
}
