package org.opendaylight.controller.cluster.datastore;

import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * ListenerRegistrationProxy acts as a proxy for a ListenerRegistration that was done on a remote shard
 *
 * Registering a DataChangeListener on the Data Store creates a new instance of the ListenerRegistrationProxy
 * The ListenerRegistrationProxy talks to a remote ListenerRegistration actor.
 */
public class ListenerRegistrationProxy implements ListenerRegistration {
    @Override
    public Object getInstance() {
        throw new UnsupportedOperationException("getInstance");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("close");
    }
}