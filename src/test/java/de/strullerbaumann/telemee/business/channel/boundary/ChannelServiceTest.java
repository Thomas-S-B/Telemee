/*
 * Created on 09.12.2013 - 05:22:59
 *
 * Copyright(c) 2013 Thomas Struller-Baumann. All Rights Reserved.
 * This software is the proprietary information of Thomas Struller-Baumann.
 */
package de.strullerbaumann.telemee.business.channel.boundary;

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
import de.strullerbaumann.telemee.business.telemeeapp.entity.TelemeeApp;
import de.strullerbaumann.telemee.rest.ChannelAttributeEndpoint;
import de.strullerbaumann.telemee.rest.ChannelEndpoint;
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

/**
 *
 * @author Thomas Struller-Baumann <thomas at struller-baumann.de>
 */
@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)  //alternative to @Inject UserTransaction utx, needed because of em
public class ChannelServiceTest {

   @PersistenceContext(unitName = "telemee")
   private EntityManager em;

   @Inject
   private ChannelService channelService;

   @Inject
   private ChannelEndpoint channelEndpoint;

   @Inject
   private ChannelAttributeEndpoint channelAttributeEndpoint;

   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
              //              .addClass(TelemeeAppEndpoint.class)
              //              .addClass(TelemeeAppService.class)
              .addClass(ChannelEndpoint.class)
              .addClass(ChannelService.class)
              .addClass(TelemeeApp.class)
              .addClass(ChannelAttributeEndpoint.class)
              .addClass(ChannelAttribute.class)
              .addClass(Channel.class)
              .addClass(LogEntry.class)
              .addClass(LogValue.class)
              .addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml")
              .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   @Test
   public void testIsDeployed() {
      Assert.assertNotNull(channelService);
   }

   @Test
   public void testAddChannelAttributeToChannel() throws Exception {
      Channel channel = new Channel("Testchannel01 for Testapp01");
      channelEndpoint.create(channel);
      ChannelAttribute channelAttribute = new ChannelAttribute("Testchannel");
      channelAttributeEndpoint.create(channelAttribute);

      em.flush(); // Write channel and channelAttribute in db
      em.clear(); // force reload

      channelService.addChannelAttributeToChannel(channelAttribute.getId(), channel.getId());
      Channel channelRetrieved = em.find(Channel.class, channel.getId());

      Assert.assertTrue(channelRetrieved.getChannelAttributes().contains(channelAttribute));
   }

   @Test
   public void testAddChannelAttributeToChannelFailed() {
      try {
         channelService.addChannelAttributeToChannel(1l, 2l);
      } catch (Exception e) {
         Assert.assertEquals(TransactionRolledbackLocalException.class, e.getCause().getClass());
      }
   }
}
