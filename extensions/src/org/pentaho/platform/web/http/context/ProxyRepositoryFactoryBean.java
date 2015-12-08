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
* Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.web.http.context;

import org.springframework.extensions.jcr.jackrabbit.RepositoryFactoryBean;

import javax.jcr.Repository;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyRepositoryFactoryBean extends RepositoryFactoryBean {


  @Override
  protected Repository createRepository() throws Exception {
    return (Repository) Proxy.newProxyInstance( Repository.class.getClassLoader(), new Class[] { Repository.class }, new RepositoryProxy( this ) );
  }

  protected Repository createRepositorySuper() throws Exception {
    return super.createRepository();
  }

  @Override
  public Class<?> getObjectType() {
    return Repository.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public static class RepositoryProxy implements InvocationHandler {

    private Repository repository;

    public RepositoryProxy( final ProxyRepositoryFactoryBean proxyRepositoryFactoryBean ) throws Exception {
      final Thread thread = new Thread( new Runnable() {
        @Override
        public void run() {
          synchronized ( RepositoryProxy.class ) {
            try {
              repository = proxyRepositoryFactoryBean.createRepositorySuper();
            } catch ( Exception e ) {
              e.printStackTrace();
            } finally {
              RepositoryProxy.class.notifyAll();
            }
          }
        }
      } );
      thread.setName( "Init Repository" );
      thread.setDaemon( true );
      thread.setContextClassLoader( null );
      thread.start();
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
      synchronized ( RepositoryProxy.class ) {
        if ( repository == null ) {
          RepositoryProxy.class.wait();
        }
      }
      return method.invoke( repository, args );
    }
  }
}
