package de.strullerbaumann.telemee.business.logentry.boundary;

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
import de.strullerbaumann.telemee.business.channel.boundary.ChannelService;
import de.strullerbaumann.telemee.business.channel.entity.Channel;
import de.strullerbaumann.telemee.business.channelattribute.entity.ChannelAttribute;
import de.strullerbaumann.telemee.business.logentry.entity.LogEntry;
import de.strullerbaumann.telemee.business.logvalue.entity.LogValue;
import de.strullerbaumann.telemee.business.telemeeapp.boundary.TelemeeAppService;
import de.strullerbaumann.telemee.business.telemeeapp.entity.TelemeeApp;
import de.strullerbaumann.telemee.rest.ChannelAttributeEndpoint;
import de.strullerbaumann.telemee.rest.ChannelEndpoint;
import de.strullerbaumann.telemee.rest.LogEntryEndpoint;
import de.strullerbaumann.telemee.rest.TelemeeAppEndpoint;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogEntryServiceIT {

   @Inject
   private LogEntryService logEntryService;
   @Inject
   private TelemeeAppEndpoint telemeeAppEndpoint;
   @Inject
   private ChannelEndpoint channelEndpoint;
   @Inject
   private ChannelAttributeEndpoint channelAttributeEndpoint;
   @Inject
   private LogEntryEndpoint logEntryEndpoint;

   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
              .addClass(TelemeeApp.class)
              .addClass(TelemeeAppService.class)
              .addClass(TelemeeAppEndpoint.class)
              .addClass(Channel.class)
              .addClass(ChannelEndpoint.class)
              .addClass(ChannelService.class)
              .addClass(ChannelAttribute.class)
              .addClass(ChannelAttributeEndpoint.class)
              .addClass(LogEntry.class)
              .addClass(LogEntryService.class)
              .addClass(LogEntryEndpoint.class)
              .addClass(LogValue.class)
              .addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml")
              .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   private TelemeeApp telemeeApp01;
   private TelemeeApp telemeeApp02;
   private Channel app01_channel01;
   private Channel app02_channel01;
   private ChannelAttribute channelAttributeX;
   private ChannelAttribute channelAttributeY;
   private ChannelAttribute channelAttributeZ;
   private LogEntry logEntry_App01_Channel01_1;
   private LogValue logValue01_x_1;
   private LogValue logValue01_y_1;
   private LogEntry logEntry_App01_Channel01_2;
   private LogValue logValue01_x_2;
   private LogValue logValue01_y_2;
   private LogEntry logEntry_App02_Channel01;
   private LogValue logValue01_z;

   @Before
   public void createTestData() {
      telemeeApp01 = new TelemeeApp();
      telemeeApp01.setName("Testapp01");
      telemeeApp01.setChannels(null);
      telemeeAppEndpoint.create(telemeeApp01);

      telemeeApp02 = new TelemeeApp();
      telemeeApp02.setName("Testapp02");
      telemeeApp02.setChannels(null);
      telemeeAppEndpoint.create(telemeeApp02);

      app01_channel01 = new Channel();
      app01_channel01.setName("Testchannel01 for Testapp01");
//      app01_channel01.setTelemeeApp(telemeeApp01);
      channelEndpoint.create(app01_channel01);

      app02_channel01 = new Channel();
      app02_channel01.setName("Testchannel01 for Testapp02");
//      app02_channel01.setTelemeeApp(telemeeApp02);
      channelEndpoint.create(app02_channel01);

      channelAttributeX = new ChannelAttribute("X");
      channelAttributeEndpoint.create(channelAttributeX);
      channelAttributeY = new ChannelAttribute("Y");
      channelAttributeEndpoint.create(channelAttributeY);
      channelAttributeZ = new ChannelAttribute("Z");
      channelAttributeEndpoint.create(channelAttributeZ);

      logEntry_App01_Channel01_1 = new LogEntry("Testdescription", app01_channel01.getId());
      logValue01_x_1 = new LogValue("1234567890", channelAttributeX.getId());
      logEntry_App01_Channel01_1.addLogValue(logValue01_x_1);
      logValue01_y_1 = new LogValue("123", channelAttributeY.getId());
      logEntry_App01_Channel01_1.addLogValue(logValue01_y_1);
      logEntryEndpoint.create(logEntry_App01_Channel01_1);

      logEntry_App01_Channel01_2 = new LogEntry("Testdescription", app01_channel01.getId());
      logValue01_x_2 = new LogValue("1234567890", channelAttributeX.getId());
      logEntry_App01_Channel01_2.addLogValue(logValue01_x_2);
      logValue01_y_2 = new LogValue("123", channelAttributeY.getId());
      logEntry_App01_Channel01_2.addLogValue(logValue01_y_2);
      logEntryEndpoint.create(logEntry_App01_Channel01_2);

      logEntry_App02_Channel01 = new LogEntry("Testdescription", app02_channel01.getId());
      logValue01_z = new LogValue("123", channelAttributeZ.getId());
      logEntry_App02_Channel01.addLogValue(logValue01_z);
      logEntryEndpoint.create(logEntry_App02_Channel01);
   }

   @Test
   public void testIsDeployed() {
      Assert.assertNotNull(logEntryService);
   }

   @Test
   public void testListAll() {
      List<LogEntry> logEntries_App01_Channel01 = logEntryService.listAll(app01_channel01.getId(), 0l, 0);
      Assert.assertEquals(2, logEntries_App01_Channel01.size());
      Assert.assertEquals(2, logEntries_App01_Channel01.get(0).getLogValues().size());
      Assert.assertEquals(2, logEntries_App01_Channel01.get(1).getLogValues().size());
      Assert.assertTrue(logEntries_App01_Channel01.contains(logEntry_App01_Channel01_1));
      Assert.assertTrue(logEntries_App01_Channel01.contains(logEntry_App01_Channel01_2));
      if (logEntries_App01_Channel01.get(0).equals(logEntry_App01_Channel01_1)) {
         Assert.assertTrue(logEntries_App01_Channel01.get(0).getLogValues().contains(logValue01_x_1));
         Assert.assertTrue(logEntries_App01_Channel01.get(0).getLogValues().contains(logValue01_y_1));
         Assert.assertTrue(logEntries_App01_Channel01.get(1).getLogValues().contains(logValue01_x_2));
         Assert.assertTrue(logEntries_App01_Channel01.get(1).getLogValues().contains(logValue01_y_2));
      } else {
         Assert.assertTrue(logEntries_App01_Channel01.get(1).getLogValues().contains(logValue01_x_1));
         Assert.assertTrue(logEntries_App01_Channel01.get(1).getLogValues().contains(logValue01_y_1));
         Assert.assertTrue(logEntries_App01_Channel01.get(0).getLogValues().contains(logValue01_x_2));
         Assert.assertTrue(logEntries_App01_Channel01.get(0).getLogValues().contains(logValue01_y_2));
      }

      List<LogEntry> logEntries_App02_Channel01 = logEntryService.listAll(app02_channel01.getId(), 0l, 0);
      Assert.assertEquals(1, logEntries_App02_Channel01.size());
      Assert.assertEquals(1, logEntries_App02_Channel01.get(0).getLogValues().size());
      Assert.assertTrue(logEntries_App02_Channel01.contains(logEntry_App02_Channel01));
      Assert.assertTrue(logEntries_App02_Channel01.get(0).getLogValues().contains(logValue01_z));
   }

   @Test
   public void testListLastCount() {
      List<LogEntry> logEntries_App01_Channel01 = logEntryService.listAll(app01_channel01.getId(), 0l, 1);
      Assert.assertEquals(1, logEntries_App01_Channel01.size());
   }

   @Test
   public void testListFromId() {
      List<LogEntry> allLogEntries_App01_Channel01 = logEntryService.listAll(app01_channel01.getId(), 0l, 0);

      // get the highestID of all logentries
      long highestID = 0;
      for (LogEntry logEntry : allLogEntries_App01_Channel01) {
         if (highestID < logEntry.getId()) {
            highestID = logEntry.getId();
         }
      }

      // retrieve from all logentries the logentry above the highestID (this should be 0)
      List<LogEntry> logEntries_App01_Channel01 = logEntryService.listAll(app01_channel01.getId(), highestID, 0);
      Assert.assertEquals(0, logEntries_App01_Channel01.size());

      // retrieve from all logentries the logentry above the highestID-1 (this should be 1)
      logEntries_App01_Channel01 = logEntryService.listAll(app01_channel01.getId(), highestID - 1, 0);
      Assert.assertEquals(1, logEntries_App01_Channel01.size());
   }

   @Test
   public void testGetCount() {
      long countLogEntries_App01_Channel01 = logEntryService.getCount(app01_channel01);
      Assert.assertEquals(2, countLogEntries_App01_Channel01);
   }

}
