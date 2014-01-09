package de.strullerbaumann.telemee.business.channel.entity;

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
import de.strullerbaumann.telemee.business.channelattribute.entity.ChannelAttribute;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Channel implements Serializable {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "id", updatable = false, nullable = false)
   @XmlElement
   private Long id = null;

   @Column
   @XmlElement
   private String name;

   @ManyToMany
   @XmlElement
   private Set<ChannelAttribute> channelAttributes;

   public Channel() {
   }

   public Channel(String name) {
      this.name = name;
   }

   public Long getId() {
      return this.id;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   @Override
   public boolean equals(Object that) {
      if (this == that) {
         return true;
      }
      if (that == null) {
         return false;
      }
      if (getClass() != that.getClass()) {
         return false;
      }
      if (id != null) {
         return id.equals(((Channel) that).id);
      }
      return super.equals(that);
   }

   public String getName() {
      return this.name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public Set<ChannelAttribute> getChannelAttributes() {
      return channelAttributes;
   }

   public void setChannelAttributes(Set<ChannelAttribute> channelAttributes) {
      this.channelAttributes = channelAttributes;
   }

   @Override
   public int hashCode() {
      if (id != null) {
         return id.hashCode();
      }
      return super.hashCode();
   }

   @Override
   public String toString() {
      return "Channel{" + "id=" + id + ", name=" + name + '}';
   }

}
