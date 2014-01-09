package de.strullerbaumann.telemee.business.logentry.entity;

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
import de.strullerbaumann.telemee.business.logvalue.entity.LogValue;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Cacheable
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LogEntry implements Serializable {

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE)   //Sequence to enable easy chaching by ID in clients
   @Column(name = "id", updatable = false, nullable = false)
   @XmlElement
   private Long id = null;

   @Column
   @XmlElement
   private String description;

   @Column
   @XmlElement
   private long channelID;

   @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
   @XmlElement
   private Set<LogValue> logValues = new HashSet<>();

   public LogEntry() {
   }

   public LogEntry(String description, long channelID) {
      this.description = description;
      this.channelID = channelID;
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
         return id.equals(((LogEntry) that).id);
      }
      return super.equals(that);
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
      return "LogEntry{" + "id=" + id + ", description=" + description + ", channelID=" + channelID + ", logValues=" + logValues + '}';
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(final String description) {
      this.description = description;
   }

   public long getChannelID() {
      return channelID;
   }

   public void setChannelID(long channelID) {
      this.channelID = channelID;
   }

   public Set<LogValue> getLogValues() {
      return logValues;
   }

   public void setLogValues(Set<LogValue> logValues) {
      this.logValues = logValues;
   }

   public void addLogValue(LogValue logValue) {
      this.logValues.add(logValue);
   }

}
