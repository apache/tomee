package org.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;

import org.openejb.OpenEJB;
import org.openejb.core.ivm.naming.IvmContext;

public class Lookup extends Command
{

    javax.naming.Context ctx = OpenEJB.getJNDIContext();

    public static void register()
    {
        Lookup cmd = new Lookup();
        Command.register( "lookup", cmd );

    }

    static String PWD = "";

    public void exec( Arguments args, DataInputStream in, PrintStream out ) throws IOException
    {
        try
        {
            String name = "";
            if ( args == null || args.count() == 0 )
            {
                name = PWD;
            }
            else
            {
                name = args.get();
            }

            Object obj = null;
            try
            {
                obj = ctx.lookup( name );
            }
            catch ( NameNotFoundException e )
            {
                out.print( "lookup: " );
                out.print( name );
                out.println( ": No such object or subcontext" );
                return;
            }
            catch ( Throwable e )
            {
                out.print( "lookup: error: " );
                e.printStackTrace( new PrintStream( out ) );
                return;
            }

            if ( obj instanceof Context )
            {
                list( name, in, out );
                return;
            }

            out.println( "" + obj );
        }
        catch ( Exception e )
        {
            e.printStackTrace( new PrintStream( out ) );
        }
    }

    public void list( String name, DataInputStream in, PrintStream out ) throws IOException
    {
        try
        {
            NamingEnumeration enum = null;
            try
            {

                enum = ctx.list( name );
            }
            catch ( NameNotFoundException e )
            {
                out.print( "lookup: " );
                out.print( name );
                out.println( ": No such object or subcontext" );
                return;
            }
            catch ( Throwable e )
            {
                out.print( "lookup: error: " );
                e.printStackTrace( new PrintStream( out ) );
                return;
            }

            if ( enum == null )
            {
                return;
            }

            while ( enum.hasMore() )
            {

                NameClassPair entry = ( NameClassPair ) enum.next();
                String eName = entry.getName();
                Class eClass = null;

                if ( IvmContext.class.getName().equals( entry.getClassName() ) )
                {
                    eClass = IvmContext.class;
                }
                else
                {
                    try
                    {
                        ClassLoader cl = OpenEJB.getContextClassLoader();
                        eClass = Class.forName( entry.getClassName(), true, cl );
                    }
                    catch ( Throwable t )
                    {
                        eClass = java.lang.Object.class;
                    }
                }

                if ( Context.class.isAssignableFrom( eClass ) )
                {

                    out.print( TextConsole.TTY_Bright );
                    out.print( TextConsole.TTY_FG_Blue );
                    out.print( entry.getName() );
                    out.print( TextConsole.TTY_Reset );
                }
                else if ( EJBHome.class.isAssignableFrom( eClass ) )
                {

                    out.print( TextConsole.TTY_Bright );
                    out.print( TextConsole.TTY_FG_Green );
                    out.print( entry.getName() );
                    out.print( TextConsole.TTY_Reset );
                }
                else
                {

                    out.print( entry.getName() );
                }
                out.println();
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace( new PrintStream( out ) );
        }
    }
}

