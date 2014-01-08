/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.recur;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;

@XmlRootElement
public class QualifiedDayOfMonth implements ITimeRecurrence {

  public QualifiedDayOfMonth() {
  }

  public QualifiedDayOfMonth( boolean last, boolean weekday, Integer day ) {
    this.last = last;
    this.weekday = weekday;
    this.day = day;
  }

  public boolean isLast() {
    return last;
  }

  public void setLast( boolean last ) {
    this.last = last;
  }

  public boolean isWeekday() {
    return weekday;
  }

  public void setWeekday( boolean weekday ) {
    this.weekday = weekday;
  }

  public Integer getDay() {
    return day;
  }

  public void setDay( Integer day ) {
    this.day = day;
  }

  public String toString() {
    String result = ""; //$NON-NLS-1$
    if ( day != null ) {
      result += day;
    }
    if ( last ) {
      result += "L"; //$NON-NLS-1$
    }
    if ( weekday ) {
      result += "W"; //$NON-NLS-1$
    }
    return result;
  }
}
