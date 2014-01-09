package de.strullerbaumann.telemee.rest.configuration;

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
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 *
 * @author Thomas Struller-Baumann <thomas at struller-baumann.de>
 */
public class CrossOriginResourceSharingFilter implements ContainerResponseFilter {

   //Check web.xml if you are changeing something
   @Override
   public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
      response.getHttpHeaders().putSingle("Access-Control-Allow-Origin", "*");
      response.getHttpHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD");
      response.getHttpHeaders().putSingle("Access-Control-Allow-Headers", "content-type");
      return response;
   }

}
