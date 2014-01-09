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
import de.strullerbaumann.telemee.business.channel.entity.Channel;
import de.strullerbaumann.telemee.business.logentry.entity.LogEntry;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

/**
 *
 * @author Thomas Struller-Baumann <thomas at struller-baumann.de>
 */
@Stateless
public class LogEntryService {

   @PersistenceContext
   EntityManager em;

   public List<LogEntry> listAll(long channelID, Long fromID, int lastCount) {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<LogEntry> cq = cb.createQuery(LogEntry.class);
      Root<LogEntry> logEntry = cq.from(LogEntry.class);

      Expression<Long> expId = logEntry.get("id");
      cq.where(
              cb.equal(logEntry.get("channelID"), channelID),
              cb.greaterThan(expId, fromID)
      );

      cq.orderBy(cb.desc(logEntry.get("id")));
      TypedQuery<LogEntry> tq = em.createQuery(cq);
      if (lastCount > 0) {
         tq.setMaxResults(lastCount);
      }
      return tq.getResultList();
   }

   public long getCount(Channel channel) {
      long count = 0;
      TypedQuery<Long> findByChannelIdQuery = em.createQuery("SELECT COUNT(l) FROM LogEntry l WHERE l.channelID = :channelId", Long.class);
      findByChannelIdQuery.setParameter("channelId", channel.getId());
      try {
         count = findByChannelIdQuery.getSingleResult();
      } catch (NoResultException nre) {
      }
      return count;
   }
}
