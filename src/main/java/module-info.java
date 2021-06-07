module ide.services {
    requires jakarta.websocket.api;
    requires jakarta.enterprise.cdi.api;
    requires org.slf4j;
    requires jakarta.inject.api;
    requires java.ws.rs;
    requires quarkus.mongodb.client;
    requires microprofile.config.api;
    requires io.smallrye.mutiny;
    requires microprofile.context.propagation.api;
    requires java.xml.bind;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires net.dontcode.core;
}