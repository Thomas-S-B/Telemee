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
public class TelemeeAppEndpointTest {

   @Inject
   private TelemeeAppEndpoint telemeeAppEndpoint;

   private static TelemeeApp persistedTelemeeApp;

   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
              .addClass(TelemeeAppEndpoint.class)
              .addClass(TelemeeAppService.class)
              .addClass(TelemeeApp.class)
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
      Assert.assertNotNull(telemeeAppEndpoint);
   }

   @Test
   @InSequence(2)
   public void testCreate() {
      TelemeeApp telemeeApp = new TelemeeApp();
      telemeeApp.setName("TesttelemeeApp");
      Assert.assertEquals(null, telemeeApp.getId());
      persistedTelemeeApp = (TelemeeApp) telemeeAppEndpoint.create(telemeeApp).getEntity();
      Assert.assertNotNull(telemeeApp.getId());
   }

   @Test
   @InSequence(3)
   public void testFindById() {
      Long idToFind = persistedTelemeeApp.getId();
      TelemeeApp retTelemeeApp = (TelemeeApp) telemeeAppEndpoint.findById(idToFind).getEntity();
      Assert.assertEquals(idToFind, retTelemeeApp.getId());
   }

   @Test
   @InSequence(4)
   public void testFindByIdFails() {
      Long idToFind = -23l;
      TelemeeApp retTelemeeApp = (TelemeeApp) telemeeAppEndpoint.findById(idToFind).getEntity();
      Assert.assertEquals(null, retTelemeeApp);
   }

   @Test
   @InSequence(5)
   public void testFindByName() {
      String nameToFind = persistedTelemeeApp.getName();
      TelemeeApp retTelemeeApp = (TelemeeApp) telemeeAppEndpoint.findByName(nameToFind).getEntity();
      Assert.assertEquals(nameToFind, retTelemeeApp.getName());
   }

   @Test
   @InSequence(6)
   public void testFindByNameFails() {
      String nameToFind = "James";
      TelemeeApp retTelemeeApp = (TelemeeApp) telemeeAppEndpoint.findByName(nameToFind).getEntity();
      Assert.assertEquals(null, retTelemeeApp);
   }

   @Test
   @InSequence(7)
   public void testListAll() {
      final List<TelemeeApp> listAll = telemeeAppEndpoint.listAll();
      Assert.assertEquals(1, listAll.size());
   }

   @Test
   @InSequence(8)
   public void testUpdate() {
      String newName = "Rigby";
      persistedTelemeeApp.setName(newName);
      telemeeAppEndpoint.update(persistedTelemeeApp);
      TelemeeApp retTelemeeApp = (TelemeeApp) telemeeAppEndpoint.findByName(newName).getEntity();
      Assert.assertEquals(newName, retTelemeeApp.getName());
   }

   @Test
   @InSequence(9)
   public void testDeleteById() {
      Long idToDelete = persistedTelemeeApp.getId();
      telemeeAppEndpoint.deleteById(idToDelete);
      TelemeeApp retTelemeeApp = (TelemeeApp) telemeeAppEndpoint.findById(idToDelete).getEntity();
      Assert.assertEquals(null, retTelemeeApp);
   }
}
