/**
 * Copyright (C) 2012 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.valves;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public class LatencyValve extends ValveBase
{
   protected static Logger log = Logger.getLogger(LatencyValve.class.getName());
   
   private String latencyRange;
   private String latencyMode;
   private String files;   

   @Override
   public void invoke(Request request, Response response) throws IOException, ServletException
   {      
      Range latency = null;
      try 
      {                 
         if (latencyRange != null)
         {
            String[] tmp = latencyRange.replaceAll("\\s*", "").split(",");            
            latency = new Range(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]));                              
         }
         else 
         {
            latency = Range.decode(latencyMode);
         }
      } 
      catch (Exception e) 
      {         
      }
                 
      if (latency == null)
      {
         latency = new Range(0, 0);
      }      
      
      files = files == null ? "" : files;                  
      
      for (String file : files.replaceAll("\\s*", "").split(","))
      {
         String uri = ((HttpServletRequest)request).getRequestURI(); 
         if (uri.endsWith(file))
         {
            try
            {
               int tmp = latency.getLatency();
               log.log(Level.INFO, "Latency for file {0} is  :  {1}", new Object[] {uri, tmp});               
               Thread.sleep(tmp);
            }
            catch (InterruptedException e)
            {
               log.log(Level.SEVERE, e.getMessage());
            }
            break;
         }
      }            
      
      getNext().invoke(request, response);
   }
   
   public String getLatencyRange()
   {
      return latencyRange;
   }

   public void setLatencyRange(String latencyRange)
   {
      this.latencyRange = latencyRange;
   }

   public String getLatencyMode()
   {
      return latencyMode;
   }

   public void setLatencyMode(String latencyMode)
   {
      this.latencyMode = latencyMode;
   }

   public String getFiles()
   {
      return files;
   }

   public void setFiles(String files)
   {
      this.files = files;
   }
   
   private static class Range 
   {
      static Range LOW = new Range(100, 300);
      
      static Range AVERAGE = new Range(400, 1000);
      
      static Range HIGHT = new Range(1000, 3000);   
      
      int min;
      int max;
      
      public Range(int min, int max) 
      {
         this.min = min;
         this.max = max;
      }
      
      public int getLatency()
      {
         return new Random().nextInt(max - min + 1) + min;
      }
      
      public static Range decode(String mode)
      {
         mode = mode == null ? "" : mode.toLowerCase();
         
         if ("low".equals(mode))
         {
            return LOW;
         }
         else if ("average".equals(mode)) 
         {
            return AVERAGE;
         }
         else if ("hight".equals(mode))
         {
            return HIGHT;
         }
         return null;
      }
           
   }
}
