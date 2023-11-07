package eu.nebulouscloud.exn.settings

import org.aeonbits.owner.Config
import org.aeonbits.owner.Config.Key
import org.aeonbits.owner.Config.Sources
import org.aeonbits.owner.Config.DefaultValue


@Sources([
    "file:./exn.properties",
    "classpath:exn.properties",
    "system:properties",
    "system:env"
])
public interface ExnConfig extends Config {

    @Key("exn.basename")
    @DefaultValue("eu.nebulouscloud")
    String baseName()

    @Key("exn.health.timeout")
    @DefaultValue("15")
    Integer healthTimeout()

    @Key("broker.url")
    String url()

    @Key("broker.port")
    int port();

    @Key("broker.username")
    String username()

    @Key("broker.password")
    String password()
}

