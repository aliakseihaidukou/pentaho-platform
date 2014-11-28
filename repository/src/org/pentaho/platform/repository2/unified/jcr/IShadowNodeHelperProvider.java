package org.pentaho.platform.repository2.unified.jcr;

/**
 * @author Andrey Khayrutdinov
 */
public interface IShadowNodeHelperProvider {
  String getShadowFolderPath();

  IShadowNodeHelper createHelperFor( String folderInsideShadowZone );
}
