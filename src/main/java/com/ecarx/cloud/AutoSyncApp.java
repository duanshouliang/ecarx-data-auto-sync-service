package com.ecarx.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * data sync service
 *
 */
@SpringBootApplication
public class AutoSyncApp
{
    public static void main( String[] args )
    {
        ConfigurableApplicationContext ca = SpringApplication.run(AutoSyncApp.class,args);

    }
}
