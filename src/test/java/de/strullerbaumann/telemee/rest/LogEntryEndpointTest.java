package de.strullerbaumann.telemee.rest;

/*
 * #%L
 * telemee
 * %%
 * Copyright (C) 2013 Thomas Struller-Baumann
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import de.strullerbaumann.telemee.business.channel.entity.Channel;
import de.strullerbaumann.telemee.business.channelattribute.entity.ChannelAttribute;
import de.strullerbaumann.telemee.business.logentry.boundary.LogEntryService;
import de.strullerbaumann.telemee.business.logentry.entity.LogEntry;
import de.strullerbaumann.telemee.business.logvalue.entity.LogValue;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogEntryEndpointTest {

   @Inject
   private LogEntryEndpoint logEntryEndpoint;

   private static LogEntry persistedLogEntry;

   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
              .addClass(LogEntryEndpoint.class)
              .addClass(LogEntryService.class)
              .addClass(Channel.class)
              .addClass(ChannelAttribute.class)
              .addClass(LogEntry.class)
              .addClass(LogValue.class)
              .addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml")
              .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   @Test
   @InSequence(1)
   public void testIsDeployed() {
      Assert.assertNotNull(logEntryEndpoint);
   }

   @Test
   @InSequence(2)
   public void testCreate() {
      LogEntry logEntry = new LogEntry();
      logEntry.setChannelID(1l);
      logEntry.setDescription("TestlogEntry");
      Assert.assertEquals(null, logEntry.getId());
      persistedLogEntry = (LogEntry) logEntryEndpoint.create(logEntry).getEntity();
      Assert.assertNotNull(logEntry.getId());
   }

   @Test
   @InSequence(3)
   public void testFindById() {
      Long idToFind = persistedLogEntry.getId();
      LogEntry retLogEntry = (LogEntry) logEntryEndpoint.findById(idToFind).getEntity();
      Assert.assertEquals(idToFind, retLogEntry.getId());
   }

   @Test
   @InSequence(4)
   public void testFindByIdFails() {
      Long idToFind = -23l;
      LogEntry retLogEntry = (LogEntry) logEntryEndpoint.findById(idToFind).getEntity();
      Assert.assertEquals(null, retLogEntry);
   }

   @Test
   @InSequence(5)
   public void testListAll() {
      final List<LogEntry> listAll = logEntryEndpoint.listAll(1, 0, 0);
      Assert.assertEquals(1, listAll.size());
   }

   @Test
   @InSequence(6)
   public void testUpdate() {
      String newDescription = "New description";
      persistedLogEntry.setDescription(newDescription);
      logEntryEndpoint.update(persistedLogEntry);
      LogEntry retLogEntry = (LogEntry) logEntryEndpoint.findById(persistedLogEntry.getId()).getEntity();
      Assert.assertEquals(newDescription, retLogEntry.getDescription());
   }

   @Test
   @InSequence(7)
   public void testDeleteById() {
      Long idToDelete = persistedLogEntry.getId();
      logEntryEndpoint.deleteById(idToDelete);
      LogEntry retLogEntry = (LogEntry) logEntryEndpoint.findById(idToDelete).getEntity();
      Assert.assertEquals(null, retLogEntry);
   }

   @Test
   @InSequence(8)
   public void testDeleteByChannelId() {
      Long channelIdToDelete = 1l;

      // Create the formerly deleted again
      LogEntry logEntry = new LogEntry();
      logEntry.setChannelID(channelIdToDelete);
      logEntry.setDescription("TestlogEntry");
      persistedLogEntry = (LogEntry) logEntryEndpoint.create(logEntry).getEntity();
      Long logEntryId = persistedLogEntry.getId();

      LogEntry retLogEntry = (LogEntry) logEntryEndpoint.findById(logEntryId).getEntity();
      Assert.assertNotNull(retLogEntry);

      logEntryEndpoint.deleteByChannelId(channelIdToDelete);

      retLogEntry = (LogEntry) logEntryEndpoint.findById(logEntryId).getEntity();
      Assert.assertNull(retLogEntry);
   }

}
