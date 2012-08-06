package com.enonic.cms.core.xslt.functions.admin;

import java.net.URLEncoder;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import com.enonic.cms.core.xslt.functions.AbstractXsltFunctionCall;

final class UrlEncodeFunction
    extends AbstractAdminFunction
{
    private final class Call
        extends AbstractXsltFunctionCall
    {
        @Override
        public SequenceIterator<? extends Item> call( final SequenceIterator<? extends Item>[] args, final XPathContext context )
            throws XPathException
        {
            final String uri = toSingleString( args[0] );

            try {
                final String encoded = URLEncoder.encode( uri, "UTF-8" );
                return createValue( encoded );
            } catch (Exception e) {
                throw new XPathException( e );
            }
        }
    }

    public UrlEncodeFunction()
    {
        super( "urlEncode" );
        setResultType( SequenceType.SINGLE_STRING );
        setArgumentTypes( SequenceType.SINGLE_ATOMIC );
        setMinimumNumberOfArguments( 1 );
    }

    @Override
    public ExtensionFunctionCall makeCallExpression()
    {
        return new Call();
    }
}
