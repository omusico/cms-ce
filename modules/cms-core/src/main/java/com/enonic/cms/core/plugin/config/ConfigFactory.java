package com.enonic.cms.core.plugin.config;

import org.osgi.framework.Bundle;

import com.enonic.cms.api.plugin.PluginConfig;

public interface ConfigFactory
{
    public PluginConfig create( Bundle bundle );
}
