package com.enonic.cms.core.portal.livetrace;

public interface Trace
{
    Duration getDuration();

    void setContainer( Traces container );
}
