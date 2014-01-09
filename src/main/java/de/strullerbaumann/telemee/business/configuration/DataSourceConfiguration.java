package de.strullerbaumann.telemee.business.configuration;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.Singleton;

/**
 *
 * @author Thomas Struller-Baumann <thomas at struller-baumann.de>
 */
@DataSourceDefinition(
        className = "org.apache.derby.jdbc.ClientDataSource",
        serverName = "localhost",
        name = "java:global/jdbc/telemee",
        databaseName = "telemee;create=true",
        portNumber = 1527,
        user = "sa",
        password = ""
)
@Singleton
public class DataSourceConfiguration {

   @PostConstruct
   public void configured() {
      Logger.getLogger(DataSourceConfiguration.class.getName()).log(Level.INFO, "### configured telemee db");
   }
}
