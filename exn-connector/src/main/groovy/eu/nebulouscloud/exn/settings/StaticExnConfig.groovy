package eu.nebulouscloud.exn.settings

import org.aeonbits.owner.Config
import org.aeonbits.owner.Config.DefaultValue
import org.aeonbits.owner.Config.Key
import org.aeonbits.owner.Config.Sources

/**
 * This class extends {@link ExnConfig} and allows you
 * to explicitly and statically define the configuration
 * properties, in order to handle the  configuration
 * of your component in the way you choose.
 *
 *
 */
public class StaticExnConfig implements ExnConfig {

    private final String baseName
    private final Integer port
    private final String url
    private final Integer healthTimeout
    private final String username
    private final String password

    def StaticExnConfig(
        String url,
        Integer port,
        String username,
        String password,
        Integer healthTimeout=15,
        String baseName='eu.nebulouscloud'
    ){

        this.url = url
        this.port = port
        this.username = username
        this.password = password
        this.baseName = baseName
        this.healthTimeout = healthTimeout


    }

    public String baseName(){
        return this.baseName
    }

    public Integer healthTimeout(){
        return this.healthTimeout

    }

    public String url(){
        return this.url

    }

    public int port(){
        return this.port

    }

    public String username(){
        return this.username
    }

    public String password(){
        return this.password
    }


}

