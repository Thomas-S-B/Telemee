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
import de.strullerbaumann.telemee.business.channel.boundary.ChannelService;
import de.strullerbaumann.telemee.business.channel.entity.Channel;
import de.strullerbaumann.telemee.business.channelattribute.entity.ChannelAttribute;
import de.strullerbaumann.telemee.business.logentry.entity.LogEntry;
import de.strullerbaumann.telemee.business.logvalue.entity.LogValue;
import de.strullerbaumann.telemee.business.telemeeapp.boundary.TelemeeAppService;
import de.strullerbaumann.telemee.business.telemeeapp.entity.TelemeeApp;
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
public class ChannelEndpointIT {

   @Inject
   private ChannelEndpoint channelEndpoint;

   @Inject
   private TelemeeAppEndpoint telemeeAppEndpoint;

   private static Channel persistedChannel;

   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
              .addClass(TelemeeApp.class)
              .addClass(TelemeeAppEndpoint.class)
              .addClass(TelemeeAppService.class)
              .addClass(ChannelAttribute.class)
              .addClass(ChannelService.class)
              .addClass(ChannelEndpoint.class)
              .addClass(Channel.class)
              .addClass(LogEntry.class)
              .addClass(LogValue.class)
              .addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml")
              .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   @Test
   @InSequence(1)
   public void testIsDeployed() {
      Assert.assertNotNull(channelEndpoint);
   }

   @Test
   @InSequence(2)
   public void testCreate() {
      Channel channel = new Channel();
      channel.setName("Testchannel");
      Assert.assertEquals(null, channel.getId());
      persistedChannel = (Channel) channelEndpoint.create(channel).getEntity();
      Assert.assertNotNull(channel.getId());
   }

   @Test
   @InSequence(3)
   public void testFindById() {
      Long idToFind = persistedChannel.getId();
      Channel retChannel = (Channel) channelEndpoint.findById(idToFind).getEntity();
      Assert.assertEquals(idToFind, retChannel.getId());
   }

   @Test
   @InSequence(4)
   public void testFindByIdFails() {
      Long idToFind = -23l;
      Channel retChannel = (Channel) channelEndpoint.findById(idToFind).getEntity();
      Assert.assertEquals(null, retChannel);
   }

   @Test
   @InSequence(5)
   public void testFindByName() {
      String nameToFind = persistedChannel.getName();
      Channel retChannel = (Channel) channelEndpoint.findByName(nameToFind).getEntity();
      Assert.assertEquals(nameToFind, retChannel.getName());
   }

   @Test
   @InSequence(6)
   public void testFindByNameFails() {
      String nameToFind = "James";
      Channel retChannel = (Channel) channelEndpoint.findByName(nameToFind).getEntity();
      Assert.assertEquals(null, retChannel);
   }

   @Test
   @InSequence(7)
   public void testListAll() {
      final List<Channel> listAll = channelEndpoint.listAll();
      Assert.assertEquals(1, listAll.size());
   }

   @Test
   @InSequence(8)
   public void testUpdate() {
      String newName = "Rigby";
      persistedChannel.setName(newName);
      channelEndpoint.update(persistedChannel);
      Channel retChannel = (Channel) channelEndpoint.findByName(newName).getEntity();
      Assert.assertEquals(newName, retChannel.getName());
   }

   @Test
   @InSequence(9)
   public void testDeleteById() {
      TelemeeApp app = new TelemeeApp("Testapp");
      app.getChannels().add(persistedChannel);
      telemeeAppEndpoint.create(app);

      TelemeeApp retApp = (TelemeeApp) telemeeAppEndpoint.findById(app.getId()).getEntity();
      Assert.assertNotNull(retApp);
      Assert.assertEquals(1, retApp.getChannels().size());

      Long idToDelete = persistedChannel.getId();
      channelEndpoint.deleteById(idToDelete);
      Channel retChannel = (Channel) channelEndpoint.findById(idToDelete).getEntity();
      Assert.assertEquals(null, retChannel);

      retApp = (TelemeeApp) telemeeAppEndpoint.findById(app.getId()).getEntity();
      Assert.assertNotNull(retApp);
      Assert.assertEquals(0, retApp.getChannels().size());
   }
}
