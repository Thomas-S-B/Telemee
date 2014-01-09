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

import de.strullerbaumann.telemee.business.telemeeapp.boundary.TelemeeAppService;
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
@Path("/telemeeapps")
public class TelemeeAppEndpoint {

   @PersistenceContext(unitName = "telemee")
   private EntityManager em;

   @Inject
   TelemeeAppService tas;

   @POST
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON})
   public Response create(TelemeeApp entity) {
      em.persist(entity);
      return Response.ok(entity).status(Status.CREATED).build();
   }

   @POST
   @Path("/{idTelemeeApp:[0-9][0-9]*}/channel/{idChannel:[0-9][0-9]*}")
   public Response addChannel(@PathParam("idTelemeeApp") Long idTelemeeApp, @PathParam("idChannel") Long idChannel) {
      tas.addChannelToApp(idChannel, idTelemeeApp);
      return Response.status(Status.CREATED).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id) {
      TelemeeApp entity = em.find(TelemeeApp.class, id);
      if (entity == null) {
         return Response.status(Status.NOT_FOUND).build();
      }
      em.remove(entity);
      return Response.noContent().build();
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   @Produces({MediaType.APPLICATION_JSON})
   public Response findById(@PathParam("id") Long id) {
      TypedQuery<TelemeeApp> findByIdQuery = em.createQuery("SELECT DISTINCT t FROM TelemeeApp t LEFT JOIN FETCH t.channels WHERE t.id = :entityId ORDER BY t.id", TelemeeApp.class);
      findByIdQuery.setParameter("entityId", id);
      TelemeeApp entity;
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
      TypedQuery<TelemeeApp> findByNameQuery = em.createQuery("SELECT DISTINCT t FROM TelemeeApp t WHERE t.name = :entityName", TelemeeApp.class);
      findByNameQuery.setParameter("entityName", name);
      TelemeeApp entity;
      try {
         entity = findByNameQuery.getSingleResult();
      } catch (NoResultException nre) {
         return Response.status(Status.NOT_FOUND).build();
      }
      return Response.ok(entity).build();
   }

   @GET
   @Produces({MediaType.APPLICATION_JSON})
   public List<TelemeeApp> listAll() {
      return em.createQuery("SELECT DISTINCT t FROM TelemeeApp t LEFT JOIN FETCH t.channels ORDER BY t.id", TelemeeApp.class).getResultList();
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes({MediaType.APPLICATION_JSON})
   public Response update(TelemeeApp entity) {
      em.merge(entity);
      return Response.noContent().build();
   }
}
