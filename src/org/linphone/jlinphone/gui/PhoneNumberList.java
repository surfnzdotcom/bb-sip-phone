/*
PhoneNumberList.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.linphone.jlinphone.gui;

import net.rim.device.api.ui.component .*;
import net.rim.device.api.collection.util.*; 
import net.rim.device.api.util.*;
import java.util.*;

public class PhoneNumberList extends SortedReadableList implements KeywordProvider
{
    public PhoneNumberList(Vector phoneNumbers)
    {
        super(new PhoneNumberListComparator());    
                   
        loadFrom(phoneNumbers.elements());      
    } 
   
     /**
      * Adds a new element to the list.
      * @param element The element to be added.
      */
    void addElement(Object element)
    {
        doAdd(element);        
    }    
    
   /**
    * @see net.rim.device.api.ui.component.KeywordProvider#getKeywords(Object element) 
    */
    public String[] getKeywords( Object element )
    {        
        if(element instanceof String )
        {            
            return StringUtilities.stringToWords(element.toString());
        }        
        return null;
    }  
    
    final static class PhoneNumberListComparator implements Comparator
    {   
 
        public int compare(Object o1, Object o2)
        {
            if (o1 == null || o2 == null)
              throw new IllegalArgumentException("Cannot compare null countries");
              
            return o1.toString().compareTo(o2.toString());
        }        
    }    
}
