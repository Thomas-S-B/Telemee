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
import de.strullerbaumann.telemee.business.logentry.boundary.LogEntryService;
import de.strullerbaumann.telemee.business.logentry.entity.LogEntry;
import de.strullerbaumann.telemee.business.telemeeapp.entity.TelemeeApp;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Stateless
@Path("/logentries")
public class LogEntryEndpoint {

   @PersistenceContext(unitName = "telemee")
   private EntityManager em;

   @Inject
   LogEntryService logEntryService;

   @POST
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   public Response create(LogEntry entity) {
      em.persist(entity);
      return Response.ok(entity).status(Status.CREATED).build();
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   @Produces({MediaType.APPLICATION_JSON})
   public Response findById(@PathParam("id") Long id) {
      TypedQuery<LogEntry> findByIdQuery = em.createQuery("SELECT DISTINCT l FROM LogEntry l LEFT JOIN FETCH l.logValues WHERE l.id = :entityId ORDER BY l.id", LogEntry.class);
      findByIdQuery.setParameter("entityId", id);
      LogEntry entity;
      try {
         entity = findByIdQuery.getSingleResult();
      } catch (NoResultException nre) {
         return Response.status(Status.NOT_FOUND).build();
      }
      return Response.ok(entity).build();
   }

   @GET
   @Path("/countByAppId/{id:[0-9][0-9]*}")
   @Produces({MediaType.TEXT_PLAIN})
   public String countByAppId(@PathParam("id") Long id) {
      TypedQuery<TelemeeApp> findAppByIdQuery = em.createQuery("SELECT DISTINCT t FROM TelemeeApp t LEFT JOIN FETCH t.channels WHERE t.id = :entityId", TelemeeApp.class);
      findAppByIdQuery.setParameter("entityId", id);
      TelemeeApp app;
      long countLogEntries = 0;
      try {
         app = findAppByIdQuery.getSingleResult();
         for (Channel channel : app.getChannels()) {
            countLogEntries = countLogEntries + logEntryService.getCount(channel);
         }
      } catch (NoResultException nre) {
         return "0";
      }
      return String.valueOf(countLogEntries);
   }

   @GET
   @Path("/countByChannelId/{id:[0-9][0-9]*}")
   @Produces({MediaType.TEXT_PLAIN})
   public String countByChannelId(@PathParam("id") Long id) {
      TypedQuery<Channel> findByIdQuery = em.createQuery("SELECT DISTINCT c FROM Channel c WHERE c.id = :entityId ORDER BY c.id", Channel.class);
      findByIdQuery.setParameter("entityId", id);
      Channel channel;
      long countLogEntries = 0;
      try {
         channel = findByIdQuery.getSingleResult();
         countLogEntries = countLogEntries + logEntryService.getCount(channel);
      } catch (NoResultException nre) {
         return "0";
      }
      return String.valueOf(countLogEntries);
   }

   @GET
   @Produces({MediaType.APPLICATION_JSON})
   public List<LogEntry> listAll(@QueryParam("channelID") long channelID, @QueryParam("fromID") long fromID, @QueryParam("lastCount") int lastCount) {
      List<LogEntry> logEntries = logEntryService.listAll(channelID, fromID, lastCount);
      return logEntries;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes({MediaType.APPLICATION_JSON})
   public Response update(LogEntry entity) {
      em.merge(entity);
      return Response.noContent().build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id) {
      LogEntry entity = em.find(LogEntry.class, id);
      if (entity == null) {
         return Response.status(Status.NOT_FOUND).build();
      }
      em.remove(entity);
      return Response.noContent().build();
   }

   @DELETE
   @Path("/deleteByChannelId/{channelID:[0-9][0-9]*}")
   public Response deleteByChannelId(@PathParam("channelID") Long channelID) {
      Query query = em.createQuery("DELETE FROM LogEntry l WHERE l.channelID = :channelID");
      query.setParameter("channelID", channelID);
      query.executeUpdate();
      return Response.noContent().build();
   }
}
