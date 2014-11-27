package org.pentaho.platform.plugin.services.importexport.legacy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;
import static org.pentaho.platform.api.repository2.unified.RepositoryFile.SEPARATOR;
import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper
  .ETC_MONDRIAN_JCR_FOLDER;
import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper
  .ETC_SHADOWED_MONDRIAN_FOLDER;

/**
 * An integration test for {@link org.pentaho.platform.plugin.services.importexport.legacy
 * .MondrianCatalogRepositoryHelper} class
 */
@RunWith( SpringJUnit4ClassRunner.class )
public class MondrianCatalogRepositoryHelperTest extends DefaultUnifiedRepositoryBase {

  private static final String TEST_CATALOG = "tst";

  MondrianCatalogRepositoryHelper helper;

  @Override @Before
  public void setUp() throws Exception {
    super.setUp();

    loginAsSysTenantAdmin();
    repo.createFolder( repo.getFile( "/etc" ).getId(), new RepositoryFile.Builder( "mondrian" ).folder( true ).build(),
      "" );

    helper = new MondrianCatalogRepositoryHelper( repo );
  }

  @Test
  public void shadowedNodeIsCreatedWithMondrianSchema() throws Exception {
    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), TEST_CATALOG, "" );

    assertNotNull( repo.getFile( ETC_MONDRIAN_JCR_FOLDER + SEPARATOR + TEST_CATALOG + SEPARATOR + "schema.xml" ) );
    assertNotNull( repo.getFile( ETC_SHADOWED_MONDRIAN_FOLDER + SEPARATOR + TEST_CATALOG + SEPARATOR + "schema.xml" ) );
  }

  @Test
  public void shadowedNodeIsCreatedWithMondrianSchemaTwice() throws Exception {
    helper = new MondrianCatalogRepositoryHelper( repo );
    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), TEST_CATALOG, "" );
    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), TEST_CATALOG + TEST_CATALOG, "" );

    assertNotNull( repo.getFile( ETC_SHADOWED_MONDRIAN_FOLDER + SEPARATOR + TEST_CATALOG + SEPARATOR + "schema.xml" ) );
    assertNotNull( repo.getFile( ETC_SHADOWED_MONDRIAN_FOLDER + SEPARATOR + TEST_CATALOG + TEST_CATALOG + SEPARATOR
        + "schema.xml" ) );
  }
}