package com.enonic.cms.core.xslt.functions.portal;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import com.enonic.cms.core.xslt.functions.AbstractXsltFunctionCall;

final class CreatePageUrlFunction
    extends AbstractPortalFunction
{
    private final class Call
        extends AbstractXsltFunctionCall
    {
        @Override
        public SequenceIterator<? extends Item> call( final SequenceIterator<? extends Item>[] args, final XPathContext context )
            throws XPathException
        {
            String menuItemKey = null;
            String[] params = new String[0];

            if ( args.length == 1 )
            {
                params = toStringArray( args[0] );
            }
            else if ( args.length == 2 )
            {
                menuItemKey = toSingleString( args[0] );
                params = toStringArray( args[1] );
            }

            final String result = getPortalFunctions().createPageUrl( menuItemKey, params );
            return createValue( result );
        }
    }

    public CreatePageUrlFunction()
    {
        super( "createPageUrl" );
        setMinimumNumberOfArguments( 0 );
        setMaximumNumberOfArguments( 2 );
        setResultType( SequenceType.SINGLE_STRING );
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new Call();
    }
}
