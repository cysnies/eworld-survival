package org.hibernate.event.internal;

import org.hibernate.engine.spi.CascadingAction;

public class DefaultPersistOnFlushEventListener extends DefaultPersistEventListener {
   public DefaultPersistOnFlushEventListener() {
      super();
   }

   protected CascadingAction getCascadeAction() {
      return CascadingAction.PERSIST_ON_FLUSH;
   }
}
