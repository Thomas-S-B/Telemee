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
import java.util.List;
import javax.ejb.Stateless;
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
@Path("/channelattributes")
public class ChannelAttributeEndpoint {

   @PersistenceContext(unitName = "telemee")
   private EntityManager em;

   @POST
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   public Response create(ChannelAttribute entity) {
      em.persist(entity);
      return Response.ok(entity).status(Status.CREATED).build();
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   @Produces({MediaType.APPLICATION_JSON})
   public Response findById(@PathParam("id") Long id) {
      //TODO qeury auslagern namedQuery inm Entity
      TypedQuery<ChannelAttribute> findByIdQuery = em.createQuery("SELECT DISTINCT c FROM ChannelAttribute c WHERE c.id = :entityId ORDER BY c.id", ChannelAttribute.class);
      findByIdQuery.setParameter("entityId", id);
      ChannelAttribute entity;
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
      TypedQuery<ChannelAttribute> findByNameQuery = em.createQuery("SELECT DISTINCT c FROM ChannelAttribute c WHERE c.name = :entityName", ChannelAttribute.class);
      findByNameQuery.setParameter("entityName", name);
      ChannelAttribute entity;
      try {
         entity = findByNameQuery.getSingleResult();
      } catch (NoResultException nre) {
         return Response.status(Status.NOT_FOUND).build();
      }
      return Response.ok(entity).build();
   }

   @GET
   @Produces({MediaType.APPLICATION_JSON})
   public List<ChannelAttribute> listAll() {
      return em.createQuery("SELECT DISTINCT c FROM ChannelAttribute c ORDER BY c.id", ChannelAttribute.class).getResultList();
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes({MediaType.APPLICATION_JSON})
   public Response update(ChannelAttribute entity) {
      em.merge(entity);
      return Response.noContent().build();
   }

//   @DELETE
//   @Path("/{id:[0-9][0-9]*}")
//   public Response deleteById(@PathParam("id") Long id) {
//      ChannelAttribute entity = em.find(ChannelAttribute.class, id);
//      if (entity == null) {
//         return Response.status(Status.NOT_FOUND).build();
//      }
//      em.remove(entity);
//      return Response.noContent().build();
//   }
//
   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id) {
      ChannelAttribute channelAttribute = em.find(ChannelAttribute.class, id);
      if (channelAttribute == null) {
         return Response.status(Status.NOT_FOUND).build();
      }

      //Delete realtion between Channel and Apps
      TypedQuery<Channel> findChannelsWithThisChannelAttribute = em.createQuery("SELECT DISTINCT c FROM Channel c LEFT JOIN FETCH c.channelAttributes", Channel.class);
      try {
         List<Channel> channels = findChannelsWithThisChannelAttribute.getResultList();
         for (Channel channel : channels) {
            if (channel.getChannelAttributes().contains(channelAttribute)) {
               channel.getChannelAttributes().remove(channelAttribute);
               em.merge(channel);
            }
         }
      } catch (NoResultException nre) {
         return Response.status(Status.NOT_FOUND).build();
      }

      em.remove(channelAttribute);
      return Response.noContent().build();
   }
}
