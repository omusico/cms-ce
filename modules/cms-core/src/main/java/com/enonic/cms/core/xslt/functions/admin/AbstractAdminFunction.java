package com.enonic.cms.core.xslt.functions.admin;

import com.enonic.cms.core.xslt.functions.AbstractXsltFunction;

abstract class AbstractAdminFunction
    extends AbstractXsltFunction
{
    public AbstractAdminFunction( final String localName )
    {
        super( "admin", "http://www.enonic.com/cms/admin", localName );
    }
}
