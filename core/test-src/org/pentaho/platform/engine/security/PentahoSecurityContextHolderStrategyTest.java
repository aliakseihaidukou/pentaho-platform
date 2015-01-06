package org.pentaho.platform.engine.security;

import org.junit.Test;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class PentahoSecurityContextHolderStrategyTest {

  @Test
  public void testGetContext() throws Exception {
    final PentahoSecurityContextHolderStrategy strategy = new PentahoSecurityContextHolderStrategy();
    SecurityContext context = strategy.getContext();
    assertNotNull( context );
    final Authentication authentication = mock( Authentication.class );
    context.setAuthentication( authentication );

    assertSame( authentication, strategy.getContext().getAuthentication() );
    Thread thread = new Thread( new Runnable() {
      @Override public void run() {
        assertSame( authentication, strategy.getContext().getAuthentication() );
        Authentication authentication2 = mock( Authentication.class );
        strategy.getContext().setAuthentication( authentication2 );
        assertSame( authentication2, strategy.getContext().getAuthentication() );
        synchronized ( this ) {
          notify();
        }
      }
    });
    thread.start();
    synchronized ( thread ) {
      thread.wait();
    }
    assertSame( authentication, strategy.getContext().getAuthentication() );
  }

  @Test
  public void testMultipleRequests() throws Exception {
    final PentahoSecurityContextHolderStrategy strategy = new PentahoSecurityContextHolderStrategy();
    SecurityContext context = strategy.getContext();
    assertNotNull( context );
    final Authentication authentication1 = mock( Authentication.class );
    context.setAuthentication( authentication1 );

    assertSame( authentication1, strategy.getContext().getAuthentication() );

    final AtomicInteger hasErrors = new AtomicInteger( 0 );

    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if ( !authentication1.equals( strategy.getContext().getAuthentication() ) ) {
          hasErrors.incrementAndGet();
          fail();
        }
        Authentication authentication2 = mock( Authentication.class );
        strategy.getContext().setAuthentication( authentication2 );
        if ( !authentication2.equals( strategy.getContext().getAuthentication() ) ) {
          hasErrors.incrementAndGet();
          fail();
        }
      }
    };

    ThreadFactory tf = new ThreadFactory() {
      @Override
      public Thread newThread( Runnable r ) {
        return new Thread( r );
      }
    };

    ExecutorService executor = Executors.newSingleThreadExecutor( tf );
    for ( int i = 0; i < 5; i++ ) {
      executor.execute( runnable );
    }
    executor.shutdown();
    while ( !executor.isTerminated() ) {
      Thread.sleep( 100 );
    }

    assertSame( 0, hasErrors.get() );
    assertSame( authentication1, strategy.getContext().getAuthentication() );
  }
}
