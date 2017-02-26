package com.a.eye.skywalking.collector.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.a.eye.skywalking.collector.actor.WorkerRef;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.support.membermodification.MemberModifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pengys5
 */
public class WorkersRefCenterTestCase {

    ActorSystem system;

    @Before
    public void createSystem() {
        system = ActorSystem.create();
    }

    @After
    public void terminateSystem() throws IllegalAccessException {
        system.terminate();
        system.awaitTermination();
        system = null;

        MemberModifier.field(WorkersRefCenter.class, "actorToRole").set(WorkersRefCenter.INSTANCE, new ConcurrentHashMap());
        MemberModifier.field(WorkersRefCenter.class, "roleToActor").set(WorkersRefCenter.INSTANCE, new ConcurrentHashMap());
    }

    @Test
    public void testRegister() throws IllegalAccessException {
        final Props props = Props.create(WorkersListener.class);
        final TestActorRef<WorkersListener> actorRef1 = TestActorRef.create(system, props, "WorkersListener1");
        final TestActorRef<WorkersListener> actorRef2 = TestActorRef.create(system, props, "WorkersListener2");
        final TestActorRef<WorkersListener> actorRef3 = TestActorRef.create(system, props, "WorkersListener3");

        WorkersRefCenter.INSTANCE.register(actorRef1, "WorkersListener");
        WorkersRefCenter.INSTANCE.register(actorRef2, "WorkersListener");
        WorkersRefCenter.INSTANCE.register(actorRef3, "WorkersListener");

        Map<WorkerRef, String> actorToRole = (Map<WorkerRef, String>) MemberModifier.field(WorkersRefCenter.class, "actorToRole").get(WorkersRefCenter.INSTANCE);

        for (Map.Entry<WorkerRef, String> entry : actorToRole.entrySet()) {
            WorkerRef workerRef = entry.getKey();
            if (workerRef.equals(actorRef1) || workerRef.equals(actorRef2) || workerRef.equals(actorRef3)) {
                Assert.assertEquals("WorkersListener", entry.getValue());
            } else {
                Assert.fail();
            }
        }

        Map<String, List<WorkerRef>> roleToActor = (Map<String, List<WorkerRef>>) MemberModifier.field(WorkersRefCenter.class, "roleToActor").get(WorkersRefCenter.INSTANCE);
        List<WorkerRef> workerRefs = roleToActor.get("WorkersListener");
        Assert.assertEquals(actorRef1.path().toString(), workerRefs.get(0).path().toString());
        Assert.assertEquals(actorRef2.path().toString(), workerRefs.get(1).path().toString());
        Assert.assertEquals(actorRef3.path().toString(), workerRefs.get(2).path().toString());
    }

    @Test
    public void testUnRegister() throws IllegalAccessException {
        final Props props = Props.create(WorkersListener.class);
        final TestActorRef<WorkersListener> actorRef1 = TestActorRef.create(system, props, "WorkersListener1");
        final TestActorRef<WorkersListener> actorRef2 = TestActorRef.create(system, props, "WorkersListener2");
        final TestActorRef<WorkersListener> actorRef3 = TestActorRef.create(system, props, "WorkersListener3");

        WorkersRefCenter.INSTANCE.register(actorRef1, "WorkersListener");
        WorkersRefCenter.INSTANCE.register(actorRef2, "WorkersListener");
        WorkersRefCenter.INSTANCE.register(actorRef3, "WorkersListener");

        Map<ActorRef, String> actorToRole = (Map<ActorRef, String>) MemberModifier.field(WorkersRefCenter.class, "actorToRole").get(WorkersRefCenter.INSTANCE);
        Map<String, List<ActorRef>> roleToActor = (Map<String, List<ActorRef>>) MemberModifier.field(WorkersRefCenter.class, "roleToActor").get(WorkersRefCenter.INSTANCE);

        WorkersRefCenter.INSTANCE.unregister(actorRef1);
        Assert.assertEquals(null, actorToRole.get(actorRef1));

        ActorRef[] actorRefs = {actorRef2, actorRef3};
        Assert.assertArrayEquals(actorRefs, roleToActor.get("WorkersListener").toArray());
    }

    @Test
    public void testSizeOf() throws NoAvailableWorkerException {
        final Props props = Props.create(WorkersListener.class);
        final TestActorRef<WorkersListener> actorRef1 = TestActorRef.create(system, props, "WorkersListener1");
        final TestActorRef<WorkersListener> actorRef2 = TestActorRef.create(system, props, "WorkersListener2");
        final TestActorRef<WorkersListener> actorRef3 = TestActorRef.create(system, props, "WorkersListener3");

        WorkersRefCenter.INSTANCE.register(actorRef1, "WorkersListener");
        WorkersRefCenter.INSTANCE.register(actorRef2, "WorkersListener");
        WorkersRefCenter.INSTANCE.register(actorRef3, "WorkersListener");

        Assert.assertEquals(3, WorkersRefCenter.INSTANCE.availableWorks("WorkersListener").size());
    }
}