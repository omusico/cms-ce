/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.itest.portal.datasource.expressionfunctions;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import com.enonic.cms.core.RequestParameters;
import com.enonic.cms.core.portal.datasource.el.ExpressionContext;
import com.enonic.cms.core.portal.datasource.el.ExpressionFunctionsExecutor;
import com.enonic.cms.core.portal.datasource.el.ExpressionFunctionsFactory;
import com.enonic.cms.core.security.user.UserEntity;
import com.enonic.cms.core.structure.SiteEntity;
import com.enonic.cms.core.time.MockTimeService;
import com.enonic.cms.itest.AbstractSpringTest;
import com.enonic.cms.itest.util.DomainFixture;

import static org.junit.Assert.*;

public class ExpressionFunctionsExecutorTest
    extends AbstractSpringTest
{
    @Autowired
    private DomainFixture fixture;

    private MockTimeService timeService;

    private UserEntity defaultUser;

    private ExpressionContext expressionContext;

    private ExpressionFunctionsFactory efFactory;

    private ExpressionFunctionsExecutor efExecutor;


    @Before
    public void before()
    {
        fixture.initSystemData();

        defaultUser = fixture.createAndStoreNormalUserWithUserGroup( "testuser", "testuser", "testuserstore" );
        defaultUser.setEmail( "email@email.com" );

        timeService = new MockTimeService();

        expressionContext = new ExpressionContext();
        expressionContext.setUser( defaultUser );
        SiteEntity site = new SiteEntity();
        site.setKey( 0 );
        expressionContext.setSite( site );

        efFactory = new ExpressionFunctionsFactory();
        efFactory.setTimeService( timeService );
        efFactory.setContext( expressionContext );

        efExecutor = new ExpressionFunctionsExecutor();
        efExecutor.setExpressionContext( expressionContext );
    }

    @Test
    public void testUserGetEmailReturnsLoggedInUserEmail()
        throws Exception
    {
        String evaluted = efExecutor.evaluate( "${user.email}" );
        assertEquals( "email@email.com", evaluted );
    }

    @Test
    public void testSingleValue()
        throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter( "brands", "bmw" );

        efExecutor.setHttpRequest( request );
        efExecutor.setRequestParameters( new RequestParameters( request.getParameterMap() ) );

        assertEquals( "bmw", efExecutor.evaluate( "${param.brands}" ) );
        assertEquals( "bmw", efExecutor.evaluate( "${param['brands']}" ) );
        assertEquals( "true", efExecutor.evaluate( "${param.brands == 'bmw'}" ) );
    }

    @Test
    public void testMultipleValue()
        throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter( "brands", "bmw" );

        request.addParameter( "cars", "skoda" );
        request.addParameter( "cars", "lexus" );
        request.addParameter( "cars", "volvo" );

        request.addParameter( "third", new String[]{"audi", "kia", "opel"} );

        efExecutor.setHttpRequest( request );
        efExecutor.setRequestParameters( new RequestParameters( request.getParameterMap() ) );

        assertEquals( "skoda,lexus,volvo", efExecutor.evaluate( "${params.cars}" ) );

        assertEquals( "3", efExecutor.evaluate( "${params.cars.length}" ) );
        assertEquals( "3", efExecutor.evaluate( "${params['cars'].length}" ) );

        assertEquals( "skoda", efExecutor.evaluate( "${params.cars[0]}" ) );
        assertEquals( "skoda", efExecutor.evaluate( "${params['cars'][0]}" ) );

        assertEquals( "volvo", efExecutor.evaluate( "${params.cars[2]}" ) );
        assertEquals( "volvo", efExecutor.evaluate( "${params['cars'][2]}" ) );

        assertEquals( "false", efExecutor.evaluate( "${params.cars == 'skoda'}" ) );
        assertEquals( "true", efExecutor.evaluate( "${params.cars[0] == 'skoda'}" ) );
        assertEquals( "true", efExecutor.evaluate( "${params['cars'][2] == 'volvo'}" ) );

        assertEquals( "3", efExecutor.evaluate( "${params.third.length}" ) );
        assertEquals( "3", efExecutor.evaluate( "${params['third'].length}" ) );

        assertEquals( "audi", efExecutor.evaluate( "${params.third[0]}" ) );
        assertEquals( "audi", efExecutor.evaluate( "${params['third'][0]}" ) );
        assertEquals( "opel", efExecutor.evaluate( "${params.third[2]}" ) );
        assertEquals( "opel", efExecutor.evaluate( "${params['third'][2]}" ) );

        assertEquals( "false", efExecutor.evaluate( "${params.third == 'audi'}" ) );
        assertEquals( "true", efExecutor.evaluate( "${params.third != 'audi'}" ) );
        assertEquals( "true", efExecutor.evaluate( "${params.third[0] == 'audi'}" ) );
        assertEquals( "true", efExecutor.evaluate( "${params.third[2] == 'opel'}" ) );
    }

    @Test
    public void testSingleValue_asParams()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter( "brands", "bmw" );
        efExecutor.setHttpRequest( request );
        efExecutor.setRequestParameters( new RequestParameters( request.getParameterMap() ) );

        assertEquals( "1", efExecutor.evaluate( "${params.brands.length}" ) );
        assertEquals( "1", efExecutor.evaluate( "${params['brands'].length}" ) );
        assertEquals( "bmw", efExecutor.evaluate( "${params.brands[0]}" ) );
        assertEquals( "bmw", efExecutor.evaluate( "${params['brands'][0]}" ) );
        assertEquals( "true", efExecutor.evaluate( "${params.brands[0] == 'bmw'}" ) );
    }

    @Test
    public void testSingleParameterEvaulation()
        throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter( "subCat", "18" );
        request.addParameter( "sub-cat", "27" );
        efExecutor.setHttpRequest( request );
        efExecutor.setRequestParameters( new RequestParameters( request.getParameterMap() ) );

        assertEquals( "18", efExecutor.evaluate( "${param.subCat}" ) );
        assertEquals( "27", efExecutor.evaluate( "${param['sub-cat']}" ) );
    }

    @Test
    public void testArrayOfParametersWithTheSameNameEvaulation()
        throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter( "brands", "bmw" );
        request.addParameter( "brands", "volvo" );
        request.addParameter( "brands", "skoda" );
        efExecutor.setHttpRequest( request );
        efExecutor.setRequestParameters( new RequestParameters( request.getParameterMap() ) );

        String param = efExecutor.evaluate( "${params.brands}" );
        assertEquals( "bmw,volvo,skoda", param );

        param = efExecutor.evaluate( "${params.brands[0]}" );
        assertEquals( "bmw", param );

        param = efExecutor.evaluate( "${params.brands[1]}" );
        assertEquals( "volvo", param );

        param = efExecutor.evaluate( "${params.brands[2]}" );
        assertEquals( "skoda", param );

        String length = efExecutor.evaluate( "${params.brands.length}" );
        assertEquals( "3", length );
    }

    @Test
    public void testArrayOfParametersWithTheSameNameEvaulation_variableWithDash()
        throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter( "my-brands", "bmw" );
        request.addParameter( "my-brands", "volvo" );
        request.addParameter( "my-brands", "skoda" );
        efExecutor.setHttpRequest( request );
        efExecutor.setRequestParameters( new RequestParameters( request.getParameterMap() ) );

        String param = efExecutor.evaluate( "${params['my-brands']}" );
        assertEquals( "bmw,volvo,skoda", param );

        param = efExecutor.evaluate( "${params['my-brands'][0]}" );
        assertEquals( "bmw", param );

        param = efExecutor.evaluate( "${params['my-brands'][1]}" );
        assertEquals( "volvo", param );

        param = efExecutor.evaluate( "${params['my-brands'][2]}" );
        assertEquals( "skoda", param );

        String length = efExecutor.evaluate( "${params['my-brands'].length}" );
        assertEquals( "3", length );
    }


    @Test
    public void testEvaluateCurrentDateWithTime()
        throws Exception
    {
        timeService.setTimeNow( new DateTime( 2010, 5, 28, 12, 30, 4, 2 ) );

        String evaluted = efExecutor.evaluate( "@publishfrom >= ${currentDate( 'yyyy.MM.dd HH:mm' )}" );
        assertEquals( "@publishfrom >= 2010.05.28 12:30", evaluted );
    }

    @Test
    public void testEvaluateCurrentDateWithoutTime()
        throws Exception
    {
        timeService.setTimeNow( new DateTime( 2010, 5, 28, 12, 30, 4, 2 ) );

        String evaluted = efExecutor.evaluate( "@publishfrom >= ${currentDate( 'yyyy.MM.dd' )}" );
        assertEquals( "@publishfrom >= 2010.05.28", evaluted );
    }

    @Test
    public void testEvaluateCurrentDateMinusOffset()
        throws Exception
    {
        timeService.setTimeNow( new DateTime( 2010, 5, 28, 12, 30, 4, 2 ) );

        String evaluted =
            efExecutor.evaluate( "@publishfrom >= ${currentDateMinusOffset( 'yyyy.MM.dd HH:mm', periodHoursMinutes( 2, 35 ) )}" );
        assertEquals( "@publishfrom >= 2010.05.28 09:55", evaluted );

        evaluted = efExecutor.evaluate( "@publishfrom >= ${currentDateMinusOffset( 'yyyy.MM.dd HH:mm', 'PT2H35M' )}" );
        assertEquals( "@publishfrom >= 2010.05.28 09:55", evaluted );

        // .. and with negative periods

        evaluted = efExecutor.evaluate( "@publishfrom >= ${currentDateMinusOffset( 'yyyy.MM.dd HH:mm', periodHoursMinutes( -2, -35 ) )}" );
        assertEquals( "@publishfrom >= 2010.05.28 15:05", evaluted );

        evaluted = efExecutor.evaluate( "@publishfrom >= ${currentDateMinusOffset( 'yyyy.MM.dd HH:mm', 'PT-2H-35M' )}" );
        assertEquals( "@publishfrom >= 2010.05.28 15:05", evaluted );
    }

    @Test
    public void testEvaluateCurrentDatePlusOffset()
        throws Exception
    {
        timeService.setTimeNow( new DateTime( 2010, 5, 28, 12, 30, 4, 2 ) );

        String evaluted =
            efExecutor.evaluate( "@publishfrom >= ${currentDatePlusOffset( 'yyyy.MM.dd HH:mm', periodHoursMinutes( 2, 5 ) )}" );
        assertEquals( "@publishfrom >= 2010.05.28 14:35", evaluted );

        evaluted = efExecutor.evaluate( "@publishfrom >= ${currentDatePlusOffset( 'yyyy.MM.dd HH:mm', 'PT2H5M' )}" );
        assertEquals( "@publishfrom >= 2010.05.28 14:35", evaluted );

        // .. and with negative periods

        evaluted = efExecutor.evaluate( "@publishfrom >= ${currentDatePlusOffset( 'yyyy.MM.dd HH:mm', periodHoursMinutes( -2, -5 ) )}" );
        assertEquals( "@publishfrom >= 2010.05.28 10:25", evaluted );

        evaluted = efExecutor.evaluate( "@publishfrom >= ${currentDatePlusOffset( 'yyyy.MM.dd HH:mm', 'PT-2H-5M' )}" );
        assertEquals( "@publishfrom >= 2010.05.28 10:25", evaluted );
    }

    @Test
    public void testEvaluatePositiveDurationHoursMinutes()
        throws Exception
    {
        timeService.setTimeNow( new DateTime( 2010, 5, 28, 12, 30, 4, 2 ) );

        String evaluted = efExecutor.evaluate( "${periodHoursMinutes( 2, 5 )}" );
        assertEquals( "PT2H5M", evaluted );
    }

    @Test
    public void testEvaluateNegativeDurationHoursMinutes()
        throws Exception
    {
        timeService.setTimeNow( new DateTime( 2010, 5, 28, 12, 30, 4, 2 ) );

        String evaluted = efExecutor.evaluate( "${periodHoursMinutes( -2, -5 )}" );
        assertEquals( "PT-2H-5M", evaluted );
    }

    @Test
    public void testPortalSiteKey()
        throws Exception
    {
        String evaluted = efExecutor.evaluate( "${portal.siteKey}" );
        assertEquals( "0", evaluted );
    }

    @Test(expected = NullPointerException.class)
    public void testPortalSiteKeyValueDoesNotExists()
        throws Exception
    {
        String evaluted = efExecutor.evaluate( "${portal.siteKey1233}" );
        assertEquals( "0", evaluted );
    }


    @Test
    public void testEvaluateNegativeDurationHoursMinutesComplex()
        throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter( "subCat", "-5" );
        request.addParameter( "sub-cat", "-2" );
        efExecutor.setHttpRequest( request );
        efExecutor.setRequestParameters( new RequestParameters( request.getParameterMap() ) );

        timeService.setTimeNow( new DateTime( 2010, 5, 28, 12, 30, 4, 2 ) );

        String evaluted = efExecutor.evaluate( "${periodHoursMinutes( param['sub-cat'], param.subCat )}" );
        assertEquals( "PT-2H-5M", evaluted );
    }

}
