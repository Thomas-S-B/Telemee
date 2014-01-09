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
import de.strullerbaumann.telemee.business.telemeeapp.entity.TelemeeApp;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
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
@Path("/channels")
public class ChannelEndpoint {

   @PersistenceContext(unitName = "telemee")
   private EntityManager em;

   @Inject
   ChannelService channelService;

   @POST
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   public Response create(Channel entity) {
      em.persist(entity);
      return Response.ok(entity).status(Status.CREATED).build();
   }

   @POST
   @Path("/{idChannel:[0-9][0-9]*}/channelattribute/{idChannelAttribute:[0-9][0-9]*}")
   public Response addChannelAttributeToChannel(@PathParam("idChannel") Long idChannel, @PathParam("idChannelAttribute") Long idChannelAttribute) {
      channelService.addChannelAttributeToChannel(idChannelAttribute, idChannel);
      return Response.status(Status.CREATED).build();
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   @Produces({MediaType.APPLICATION_JSON})
   public Response findById(@PathParam("id") Long id) {
      TypedQuery<Channel> findByIdQuery = em.createQuery("SELECT DISTINCT c FROM Channel c WHERE c.id = :entityId ORDER BY c.id", Channel.class);
      findByIdQuery.setParameter("entityId", id);
      Channel entity;
      try {
         entity = findByIdQuery.getSingleResult();
      } catch (NoResultException nre) {
         return Response.status(Status.NOT_FOUND).build();
      }
      return Response.ok(entity).build();
   }

   @GET
   @Path("/findBy")
   @Produces({MediaType.APPLICATION_JSON})
   public Response findByName(@QueryParam("name") String name) {
      TypedQuery<Channel> findByNameQuery = em.createQuery("SELECT DISTINCT c FROM Channel c WHERE c.name = :entityName", Channel.class);
      findByNameQuery.setParameter("entityName", name);
      Channel entity;
      try {
         entity = findByNameQuery.getSingleResult();
      } catch (NoResultException nre) {
         return Response.status(Status.NOT_FOUND).build();
      }
      return Response.ok(entity).build();
   }

   @GET
   @Produces({MediaType.APPLICATION_JSON})
   public List<Channel> listAll() {
      return em.createQuery("SELECT DISTINCT c FROM Channel c ORDER BY c.id", Channel.class).getResultList();
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes({MediaType.APPLICATION_JSON})
   public Response update(Channel entity) {
      em.merge(entity);
      return Response.noContent().build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id) {
      Channel channel = em.find(Channel.class, id);
      if (channel == null) {
         return Response.status(Status.NOT_FOUND).build();
      }

      //Delete realtion between Channel and Apps
      TypedQuery<TelemeeApp> findAppsWithThisChannel = em.createQuery("SELECT DISTINCT t FROM TelemeeApp t LEFT JOIN FETCH t.channels", TelemeeApp.class);
      try {
         List<TelemeeApp> telemeeApps = findAppsWithThisChannel.getResultList();
         for (TelemeeApp app : telemeeApps) {
            if (app.getChannels().contains(channel)) {
               app.getChannels().remove(channel);
               em.merge(app);
            }
         }
      } catch (NoResultException nre) {
         return Response.status(Status.NOT_FOUND).build();
      }

      em.remove(channel);
      return Response.noContent().build();
   }

}
