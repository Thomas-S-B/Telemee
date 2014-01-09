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
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Thomas Struller-Baumann <thomas at struller-baumann.de>
 */
@Stateless
public class ChannelService {

   @PersistenceContext(unitName = "telemee")
   private EntityManager em;

   public void addChannelAttributeToChannel(Long idChannelAttribute, Long idChannel) {
      Channel channel = em.find(Channel.class, idChannel);
      if (channel == null) {
         throw new NoResultException("Channel with ID " + idChannel + " does not exist");
      }
      ChannelAttribute channelAttribute = em.find(ChannelAttribute.class, idChannelAttribute);
      if (channelAttribute == null) {
         throw new NoResultException("ChannelAttribute with ID " + idChannelAttribute + " does not exist");
      }
      if (!channel.getChannelAttributes().contains(channelAttribute)) {
         channel.getChannelAttributes().add(channelAttribute);
         em.merge(channelAttribute);
      }
   }

}
