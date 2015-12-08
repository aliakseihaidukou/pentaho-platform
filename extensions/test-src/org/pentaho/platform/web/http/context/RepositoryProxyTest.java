/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.http.context;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.jcr.Repository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RepositoryProxyTest {

  @Test
  public void testInvoke() throws Throwable {
    final ProxyRepositoryFactoryBean factoryBean = spy( new ProxyRepositoryFactoryBean() );
    final Repository repositoryToReturn = mock( Repository.class );
    doAnswer( new Answer<Repository>() {
      @Override
      public Repository answer( InvocationOnMock invocation ) throws Throwable {
        Thread.sleep( 1000 );
        return repositoryToReturn;
      }
    } ).when( factoryBean ).createRepositorySuper();
    final Object repository = factoryBean.createRepository();

    assertEquals( repository, repositoryToReturn );
  }
}
