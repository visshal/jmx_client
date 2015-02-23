/*
 * Client.java - JMX client that interacts with the JMX agent.
 * It can be used to keep on polling jmx and send data to some graphing agent i.e. graphite
 */

package com.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Client {

    /**
     * Inner class that will handle the notifications.
     */
    public static class ClientListener implements NotificationListener {
        public void handleNotification(Notification notification,
                                       Object handback) {
            echo("\nReceived notification:");
            echo("\tClassName: " + notification.getClass().getName());
            echo("\tSource: " + notification.getSource());
            echo("\tType: " + notification.getType());
            echo("\tMessage: " + notification.getMessage());
            if (notification instanceof AttributeChangeNotification) {
                AttributeChangeNotification acn =
                    (AttributeChangeNotification) notification;
                echo("\tAttributeName: " + acn.getAttributeName());
                echo("\tAttributeType: " + acn.getAttributeType());
                echo("\tNewValue: " + acn.getNewValue());
                echo("\tOldValue: " + acn.getOldValue());
            }
        }
    }

    /* For simplicity, we declare "throws Exception".
       Real programs will usually want finer-grained exception handling. */
    public static void main(String[] args) throws Exception {
        // Create an RMI connector client and
        // connect it to the RMI connector server
        //
        echo("\nCreate an RMI connector client and " +
             "connect it to the RMI connector server");
        JMXServiceURL url =
            new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:34548/jmxrmi");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

        // Get an MBeanServerConnection
        //
        echo("\nGet an MBeanServerConnection");
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        waitForEnterPressed();

        // Get domains from MBeanServer
        //
        echo("\nDomains:");
        String domains[] = mbsc.getDomains();
        Arrays.sort(domains);
        for (String domain : domains) {
            echo("\tDomain = " + domain);
        }
        waitForEnterPressed();

        // Get MBeanServer's default domain
        //
        echo("\nMBeanServer default domain = " + mbsc.getDefaultDomain());

        // Get MBean count
        //
        echo("\nMBean count = " + mbsc.getMBeanCount());

        // Query MBean names
        //
        echo("\nQuery MBeanServer MBeans:");
        //@TODO: Fix tabular data support for GC MS and Scavenge.
        Set<ObjectName> names =
            new TreeSet<ObjectName>(mbsc.queryNames(null, null));
        for (ObjectName name : names) {
            echo("ObjectName = " + name);
        	MBeanInfo mbi = mbsc.getMBeanInfo(name);
        	MBeanAttributeInfo[] mbarr = mbi.getAttributes();
        	for(MBeanAttributeInfo mb : mbarr) {
        		try {
            		Object oo = mbsc.getAttribute(name, mb.getName());
        			//System.out.println(mb.getName() + ":::: " + oo.getClass());
        			if(oo instanceof java.lang.Boolean) {
        				echo("\t"+ mb.getName() + "=>" + oo);
        			} else if (oo instanceof javax.management.openmbean.CompositeDataSupport) {
        				echo("\t"+ mb.getName() + "=>");
        				CompositeData cData = (CompositeData) oo;
        				Set< String > keys = cData.getCompositeType().keySet();
        				for(String s : keys) {
        					echo("\t\t"+ s + ":" + cData.get(s));
        				}
        			} else if (oo instanceof java.lang.Integer) {
        				echo("\t"+ mb.getName() + "=>" + oo);
        			} else if (oo instanceof java.lang.Long) {
        				echo("\t"+ mb.getName() + "=>" + oo);
        			} else if (oo instanceof java.lang.String[]) {
        				echo("\t"+ mb.getName() + "=>" + oo);
        				for(String s : (String[]) oo) {
        					echo("\t\t"+s);
        				}
        			}
        		} catch (Exception e) { 
        			
        		}
        	}

        }
        waitForEnterPressed();
        echo("\nClose the connection to the server");
        jmxc.close();
        echo("\nBye! Bye!");
    }

    private static void echo(String msg) {
        System.out.println(msg);
    }

    private static void waitForEnterPressed() {
        try {
            echo("\nPress <Enter> to continue...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
