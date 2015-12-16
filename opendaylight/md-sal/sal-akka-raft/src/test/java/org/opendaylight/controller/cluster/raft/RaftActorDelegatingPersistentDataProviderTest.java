/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.cluster.raft;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import akka.japi.Procedure;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.cluster.DataPersistenceProvider;
import org.opendaylight.controller.cluster.PersistentDataProvider;
import org.opendaylight.controller.cluster.raft.protobuff.client.messages.Payload;
import org.opendaylight.controller.cluster.raft.protobuff.client.messages.PersistentPayload;

/**
 * Unit tests for RaftActorDelegatingPersistentDataProvider.
 *
 * @author Thomas Pantelis
 */
public class RaftActorDelegatingPersistentDataProviderTest {
    private static final Payload PERSISTENT_PAYLOAD = new TestPersistentPayload();

    private static final Payload NON_PERSISTENT_PAYLOAD = new TestNonPersistentPayload();

    private static final Object OTHER_DATA_OBJECT = new Object();

    @Mock
    private ReplicatedLogEntry mockPersistentLogEntry;

    @Mock
    private ReplicatedLogEntry mockNonPersistentLogEntry;

    @Mock
    private DataPersistenceProvider mockDelegateProvider;

    @Mock
    private PersistentDataProvider mockPersistentProvider;

    @SuppressWarnings("rawtypes")
    @Mock
    private Procedure mockProcedure;

    private RaftActorDelegatingPersistentDataProvider provider;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        doReturn(PERSISTENT_PAYLOAD).when(mockPersistentLogEntry).getData();
        doReturn(NON_PERSISTENT_PAYLOAD).when(mockNonPersistentLogEntry).getData();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Exception {
                final Object[] args = invocation.getArguments();
                ((Procedure<Object>)args[1]).apply(args[0]);
                return null;
            }
        }).when(mockPersistentProvider).persist(any(Object.class), any(Procedure.class));

        doNothing().when(mockDelegateProvider).persist(any(Object.class), any(Procedure.class));
        doNothing().when(mockProcedure).apply(any(Object.class));
        provider = new RaftActorDelegatingPersistentDataProvider(mockDelegateProvider, mockPersistentProvider);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPersistWithPersistenceEnabled() {
        doReturn(true).when(mockDelegateProvider).isRecoveryApplicable();

        provider.persist(mockPersistentLogEntry, mockProcedure);
        verify(mockDelegateProvider).persist(mockPersistentLogEntry, mockProcedure);

        provider.persist(mockNonPersistentLogEntry, mockProcedure);
        verify(mockDelegateProvider).persist(mockNonPersistentLogEntry, mockProcedure);

        provider.persist(OTHER_DATA_OBJECT, mockProcedure);
        verify(mockDelegateProvider).persist(OTHER_DATA_OBJECT, mockProcedure);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPersistWithPersistenceDisabled() {
        doReturn(false).when(mockDelegateProvider).isRecoveryApplicable();

        provider.persist(mockPersistentLogEntry, mockProcedure);
        verify(mockPersistentProvider).persist(mockPersistentLogEntry, mockProcedure);

        provider.persist(mockNonPersistentLogEntry, mockProcedure);
        verify(mockDelegateProvider).persist(mockNonPersistentLogEntry, mockProcedure);

        provider.persist(OTHER_DATA_OBJECT, mockProcedure);
        verify(mockDelegateProvider).persist(OTHER_DATA_OBJECT, mockProcedure);
    }

    static class TestNonPersistentPayload extends Payload {
        @SuppressWarnings("rawtypes")
        @Override
        public <T> Map<GeneratedExtension, T> encode() {
            return null;
        }

        @Override
        public Payload decode(
                org.opendaylight.controller.protobuff.messages.cluster.raft.AppendEntriesMessages.AppendEntries.ReplicatedLogEntry.Payload payload) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    static class TestPersistentPayload extends TestNonPersistentPayload implements PersistentPayload {
    }
}