package de.strullerbaumann.telemee.business.telemeeapp.boundary;

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
import de.strullerbaumann.telemee.business.telemeeapp.entity.TelemeeApp;
import de.strullerbaumann.telemee.rest.ChannelEndpoint;
import de.strullerbaumann.telemee.rest.TelemeeAppEndpoint;
import javax.ejb.TransactionRolledbackLocalException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)  //alternative to @Inject UserTransaction utx, needed because of em
public class TelemeeAppServiceIT {

   @PersistenceContext(unitName = "telemee")
   private EntityManager em;

   @Inject
   private TelemeeAppService telemeeAppService;

   @Inject
   private TelemeeAppEndpoint telemeeAppEndpoint;

   @Inject
   private ChannelEndpoint channelEndpoint;

   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
              .addClass(TelemeeAppEndpoint.class)
              .addClass(TelemeeAppService.class)
              .addClass(TelemeeApp.class)
              .addClass(ChannelEndpoint.class)
              .addClass(ChannelService.class)
              .addClass(Channel.class)
              .addClass(ChannelAttribute.class)
              .addClass(LogEntry.class)
              .addClass(LogValue.class)
              .addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml")
              .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   @Test
   public void testIsDeployed() {
      Assert.assertNotNull(telemeeAppService);
   }

   @Test
   public void testAddChannelToApp() throws Exception {
      // create App
      TelemeeApp telemeeApp01 = new TelemeeApp();
      telemeeApp01.setName("Testapp01");
      telemeeApp01.setChannels(null);
      telemeeAppEndpoint.create(telemeeApp01);
      // create Channel
      Channel app01_channel01 = new Channel();
      app01_channel01.setName("Testchannel01 for Testapp01");
//      app01_channel01.setTelemeeApp(telemeeApp01);
      channelEndpoint.create(app01_channel01);

      em.flush(); // Write app and channel in db
      em.clear(); // force reload

      telemeeAppService.addChannelToApp(app01_channel01.getId(), telemeeApp01.getId());
      TelemeeApp telemeeApp01Retrieved = em.find(TelemeeApp.class, telemeeApp01.getId());

      Assert.assertTrue(telemeeApp01Retrieved.getChannels().contains(app01_channel01));
   }

   @Test
   public void testAddChannelToAppFailed() {
      try {
         telemeeAppService.addChannelToApp(1l, 2l);
      } catch (Exception e) {
         Assert.assertEquals(TransactionRolledbackLocalException.class, e.getCause().getClass());
      }
   }
}
