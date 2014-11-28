package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;

import static org.pentaho.platform.api.repository2.unified.RepositoryFile.SEPARATOR;

/**
 * @author Andrey Khayrutdinov
 */
public class ShadowNodeHelperProviderImpl implements IShadowNodeHelperProvider {
  private static final Log logger = LogFactory.getLog( ShadowNodeHelperProviderImpl.class );

  // todo Khayrutdinov : needs to be configured
  private static final String SHADOWED = "shadowed";
  private static final String ETC_SHADOWED = ClientRepositoryPaths.getEtcFolderPath() + SEPARATOR + SHADOWED;

  private final IUnifiedRepository unifiedRepository;
  private final IRepositoryFileAclDao repositoryFileAclDao;

  public ShadowNodeHelperProviderImpl( IUnifiedRepository unifiedRepository,
                                       IRepositoryFileAclDao repositoryFileAclDao ) {
    this.unifiedRepository = unifiedRepository;
    this.repositoryFileAclDao = repositoryFileAclDao;
  }

  // todo Khayrutdinov : should be changed to be invoked by Spring
  @Deprecated
  private void ensureInit() {
    init();
  }

  public void init() {
    RepositoryFile shadowedFolder = unifiedRepository.getFile( getShadowFolderPath() );
    if ( shadowedFolder == null ) {
      logger.info( "No shadow folder was found: " + getShadowFolderPath() );
      String etc = ClientRepositoryPaths.getEtcFolderPath();
      RepositoryFile folder =
        unifiedRepository.createFolder( unifiedRepository.getFile( etc ).getId(), JcrShadowNodeHelper.purFolder(
          SHADOWED ), "" );
      if ( folder == null ) {
        logger.error( "Shadow folder was not created: " + getShadowFolderPath() );
      } else {
        logger.info( "Shadow folder was created: " + getShadowFolderPath() );
      }
    }
  }

  @Override public String getShadowFolderPath() {
    return ETC_SHADOWED;
  }

  public RepositoryFile ensureExistsInShadowFolder( String folderName ) {
    RepositoryFile folder = unifiedRepository.getFile( getShadowFolderPath() + RepositoryFile.SEPARATOR + folderName );
    if ( folder == null ) {
      RepositoryFile shadowFolder = unifiedRepository.getFile( getShadowFolderPath() );
      folder = unifiedRepository.createFolder( shadowFolder.getId(), JcrShadowNodeHelper.purFolder( folderName ), "" );
    }
    return folder;
  }

  @Override public IShadowNodeHelper createHelperFor( String folderInsideShadowZone ) {
    ensureInit();
    RepositoryFile folder = ensureExistsInShadowFolder( folderInsideShadowZone );
    return new JcrShadowNodeHelper( unifiedRepository, repositoryFileAclDao, folder.getPath() );
  }
}
